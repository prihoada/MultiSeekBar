package cz.prihoda.android.widget.multiseekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.prihoda.android.widget.multiseekbar.model.Axis;
import cz.prihoda.android.widget.multiseekbar.model.Grid;
import cz.prihoda.android.widget.multiseekbar.model.Thumb;
import cz.prihoda.android.widget.multiseekbar.model.ThumbPair;
import cz.prihoda.android.widget.multiseekbar.utils.DimensionUtils;

/**
 * 2 dimensional interactive graph.
 * Created by Adam Příhoda on 14.09.2015.
 */
public class MultiSeekBar extends View {

    @SuppressWarnings("unused")
    private static final String TAG = MultiSeekBar.class.getSimpleName();

    private ArrayList<ThumbPair> thumbs;

    private Axis xAxis;
    private Axis yAxis;
    private Grid grid;

    private int currentThumbIndex;
    private Point currentTouchDown;

    private OnValueChangedListener onValueChangedListener;

    private float textMargin;
    private Rect textBounds;

    private boolean saveInstanceState;

    /**
     * Method of drawing the lines leading to the edges of the graph.
     */
    public enum CapType{
        HORIZONTAL,
        CORNER,
        NONE
    }
    private CapType capTypeStart;
    private CapType capTypeEnd;

    public MultiSeekBar(Context context) {
        super(context);
        construct(context, null);
    }
    public MultiSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct(context, attrs);
    }
    public MultiSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        construct(context, attrs);
    }


    private void construct(Context context, AttributeSet attrs) {

        xAxis = new Axis(context);
        yAxis = new Axis(context);
        yAxis.setInverted(true);

        textBounds = new Rect();

        //load xml attributes
        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MultiSeekBar, 0, 0);
            try {

                xAxis.setMinValue(typedArray.getInteger(R.styleable.MultiSeekBar_minValueX, 0));
                xAxis.setMaxValue(typedArray.getInteger(R.styleable.MultiSeekBar_maxValueX, 100));
                yAxis.setMinValue(typedArray.getInteger(R.styleable.MultiSeekBar_minValueY, 0));
                yAxis.setMaxValue(typedArray.getInteger(R.styleable.MultiSeekBar_maxValueY, 100));

                Drawable drawableX = typedArray.getDrawable(R.styleable.MultiSeekBar_thumbXDrawable);
                if(drawableX == null) drawableX = context.getResources().getDrawable(R.drawable.ic_thumb_x);
                if (drawableX != null) xAxis.setThumbDrawable(drawableX);

                Drawable drawableY = typedArray.getDrawable(R.styleable.MultiSeekBar_thumbYDrawable);
                if(drawableY == null) drawableY = context.getResources().getDrawable(R.drawable.ic_thumb_y);
                if (drawableY != null) yAxis.setThumbDrawable(drawableY);

                xAxis.setGridSpacing(typedArray.getInteger(R.styleable.MultiSeekBar_xGridSpacing, 20));
                yAxis.setGridSpacing(typedArray.getInteger(R.styleable.MultiSeekBar_yGridSpacing, 20));

                capTypeStart = CapType.values()[typedArray.getInt(R.styleable.MultiSeekBar_capTypeStart, 0)];
                capTypeEnd = CapType.values()[typedArray.getInt(R.styleable.MultiSeekBar_capTypeEnd, 0)];



                textMargin = typedArray.getDimension(R.styleable.MultiSeekBar_textMargin, DimensionUtils.dpToPixels(context, 5));
                xAxis.setDrawLimitText(typedArray.getBoolean(R.styleable.MultiSeekBar_drawLimitTextX, true));
                xAxis.setDrawGridLineText(typedArray.getBoolean(R.styleable.MultiSeekBar_drawGridLineTextX, false));
                yAxis.setDrawLimitText(typedArray.getBoolean(R.styleable.MultiSeekBar_drawLimitTextY, true));
                yAxis.setDrawGridLineText(typedArray.getBoolean(R.styleable.MultiSeekBar_drawGridLineTextY, false));

                xAxis.setDrawLineHelpers(typedArray.getBoolean(R.styleable.MultiSeekBar_drawLineHelpersX, true));
                yAxis.setDrawLineHelpers(typedArray.getBoolean(R.styleable.MultiSeekBar_drawLineHelpersY, true));

                saveInstanceState = typedArray.getBoolean(R.styleable.MultiSeekBar_saveInstanceState, true);

            } finally {
                typedArray.recycle();
            }
        }

        grid = new Grid(context);
        thumbs = new ArrayList<>();

        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        //add some thumbs to the android studio preview
        if(isInEditMode()){
            addThumbPair(new Point(xAxis.getValueRange()/2 + xAxis.getMinValue(), yAxis.getValueRange()/2 + yAxis.getMinValue()));
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        if(!saveInstanceState) return super.onSaveInstanceState();

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putSerializable("thumbs", new InstanceState(this.getThumbs()));
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!saveInstanceState){
            super.onRestoreInstanceState(state);
            return;
        }

        if(state instanceof Bundle){
            Bundle bundle = (Bundle) state;

            InstanceState instanceState = (InstanceState) bundle.getSerializable("thumbs");
            if(instanceState != null){
                this.thumbs = instanceState.thumbs;
                for (ThumbPair thumbPair : thumbs) {
                    thumbPair.getxAxisThumb().setAxis(xAxis);
                    thumbPair.getyAxisThumb().setAxis(yAxis);
                }
            }

            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }

    private class InstanceState implements Serializable {
        public ArrayList<ThumbPair> thumbs;
        public InstanceState(ArrayList<ThumbPair> thumbs) {
            this.thumbs = thumbs;
        }
    }

    /**
     * Adds thumbs to the given position.
     * @param value value of the new thumb pair.
     */
    public void addThumbPair(Point value){
        Thumb x = new Thumb(xAxis, xAxis.getThumbDrawable());
        x.setValue(value.x);

        Thumb y = new Thumb(yAxis, yAxis.getThumbDrawable());
        y.setValue(value.y);

        ThumbPair pair = new ThumbPair(x, y);
        thumbs.add(pair);
        xAxis.getThumbs().add(pair.getxAxisThumb());
        yAxis.getThumbs().add(pair.getyAxisThumb());

        Collections.sort(thumbs, new Comparator<ThumbPair>() {
            @Override
            public int compare(ThumbPair lhs, ThumbPair rhs) {
                if (lhs.getxAxisThumb().getValue() == rhs.getxAxisThumb().getValue()) return 0;
                return (lhs.getxAxisThumb().getValue() < rhs.getxAxisThumb().getValue()) ? -1 : 1;
            }
        });

        invalidate();
    }

    /**
     * Finds the largest segment on the X axis and creates a thumb in the middle of it.
     */
    public void splitLargestXSegment(){

        Thumb previous = null;
        Thumb next;
        Thumb current;

        int largestSegment = 0;
        int splitValue = 0;

        for (int i = 0; i < thumbs.size(); i++) {
            if(i > 0) previous = thumbs.get(i-1).getxAxisThumb();
            if(i < thumbs.size()-1) next = thumbs.get(i+1).getxAxisThumb();
            else next = null;
            current = thumbs.get(i).getxAxisThumb();

            if(previous == null){
                largestSegment = current.getValue() - xAxis.getMinValue();
                splitValue = xAxis.getMinValue() + largestSegment/2;
            }
            if(next == null){
                if(xAxis.getMaxValue() - current.getValue() > largestSegment){
                    largestSegment = xAxis.getMaxValue() - current.getValue();
                    splitValue = current.getValue() + largestSegment/2;
                }
            }
            else{
                if(next.getValue() - current.getValue() > largestSegment){
                    largestSegment = next.getValue() - current.getValue();
                    splitValue = current.getValue() + largestSegment/2;
                }
            }
        }
        addThumbPair(new Point(splitValue, yAxis.getValueRange() / 2 + yAxis.getMinValue()));


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width = 0;
        if(widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height = 0;
        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }

        yAxis.getBounds().left = getPaddingLeft();
        yAxis.getBounds().right = getPaddingLeft() + yAxis.getThumbWidth();
        yAxis.getBounds().top = getPaddingTop();
        yAxis.getBounds().bottom = height - getPaddingBottom() - xAxis.getThumbHeight() + yAxis.getThumbHeight()/2;

        yAxis.setMaxPixels(yAxis.getBounds().bottom - yAxis.getThumbHeight() / 2);
        yAxis.setMinPixels(yAxis.getBounds().top + yAxis.getThumbHeight() / 2);

        xAxis.getBounds().left = yAxis.getBounds().right - xAxis.getThumbWidth()/2;
        xAxis.getBounds().right = width - getPaddingRight();
        xAxis.getBounds().top = height - getPaddingBottom() - xAxis.getThumbHeight();
        xAxis.getBounds().bottom = height - getPaddingBottom();

        xAxis.setMinPixels(xAxis.getBounds().left + xAxis.getThumbWidth()/2);
        xAxis.setMaxPixels(xAxis.getBounds().right - xAxis.getThumbWidth()/2);

        grid.getBounds().left = yAxis.getBounds().right;
        grid.getBounds().right = (int) xAxis.getMaxPixels();
        grid.getBounds().top = (int) yAxis.getMinPixels();
        grid.getBounds().bottom = xAxis.getBounds().top;

        for (ThumbPair thumbPair : thumbs) {
            thumbPair.getxAxisThumb().onMeasure();
            thumbPair.getyAxisThumb().onMeasure();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawGrid(canvas);
        drawLine(canvas);
        drawThumbs(canvas);
        drawText(canvas);

    }


    /**
     * Draws the thumbs.
     * @param canvas view canvas.
     */
    private void drawThumbs(Canvas canvas) {
        for (ThumbPair thumbPair : thumbs) {

            Rect xBounds = new Rect();
            xBounds.left = (int) thumbPair.getxAxisThumb().getPixelPosition() - xAxis.getThumbWidth()/2;
            xBounds.bottom = getMeasuredHeight();
            xBounds.right = (int) thumbPair.getxAxisThumb().getPixelPosition() + xAxis.getThumbWidth()/2;
            xBounds.top = getMeasuredHeight() - xAxis.getThumbHeight();

            thumbPair.getxAxisThumb().getDrawable().setBounds(xBounds);
            thumbPair.getxAxisThumb().getDrawable().draw(canvas);


            Rect yBounds = new Rect();
            yBounds.left = yAxis.getBounds().left;
            yBounds.bottom = (int) (thumbPair.getyAxisThumb().getPixelPosition() + yAxis.getThumbHeight()/2);
            yBounds.right = yAxis.getThumbWidth();
            yBounds.top = (int) (thumbPair.getyAxisThumb().getPixelPosition() - yAxis.getThumbHeight()/2);

            thumbPair.getyAxisThumb().getDrawable().setBounds(yBounds);
            thumbPair.getyAxisThumb().getDrawable().draw(canvas);
        }
    }

    /**
     * Draws thumb helper lines and the main curve line.
     * @param canvas view canvas.
     */
    private void drawLine(Canvas canvas) {

        //draw helper line
        for (ThumbPair thumbPair : thumbs) {
            //draw x line
            if(xAxis.isDrawLineHelpers()){
                canvas.drawLine(thumbPair.getxAxisThumb().getPixelPosition(), xAxis.getBounds().top,
                        thumbPair.getxAxisThumb().getPixelPosition(), thumbPair.getyAxisThumb().getPixelPosition(), xAxis.getLineHelperPaint());
            }
            //draw y line
            if(yAxis.isDrawLineHelpers()){
                canvas.drawLine(yAxis.getBounds().right, thumbPair.getyAxisThumb().getPixelPosition(),
                        thumbPair.getxAxisThumb().getPixelPosition(), thumbPair.getyAxisThumb().getPixelPosition(), yAxis.getLineHelperPaint());
            }
        }

        //draw starting line
        if(!thumbs.isEmpty()){
            switch (capTypeStart){
                case CORNER:
                    canvas.drawLine(grid.getBounds().left, grid.getBounds().bottom, thumbs.get(0).getxAxisThumb().getPixelPosition(), thumbs.get(0).getyAxisThumb().getPixelPosition(), grid.getMainLinePaint());
                    break;
                case HORIZONTAL:
                    canvas.drawLine(grid.getBounds().left, thumbs.get(0).getyAxisThumb().getPixelPosition(), thumbs.get(0).getxAxisThumb().getPixelPosition(), thumbs.get(0).getyAxisThumb().getPixelPosition(), grid.getMainLinePaint());
                    break;
            }
        }

        //draw connecting lines
        for (int i = 0; i < thumbs.size() - 1; i++) {
            ThumbPair thumbPair = thumbs.get(i);
            ThumbPair nextThumbPair = thumbs.get(i+1);

            canvas.drawLine(thumbPair.getxAxisThumb().getPixelPosition(), thumbPair.getyAxisThumb().getPixelPosition(),
                            nextThumbPair.getxAxisThumb().getPixelPosition(), nextThumbPair.getyAxisThumb().getPixelPosition(), grid.getMainLinePaint());
        }

        //draw ending line
        if(!thumbs.isEmpty()) {
            switch (capTypeEnd) {
                case HORIZONTAL:
                    canvas.drawLine(thumbs.get(thumbs.size() - 1).getxAxisThumb().getPixelPosition(), thumbs.get(thumbs.size() - 1).getyAxisThumb().getPixelPosition(), grid.getBounds().right, thumbs.get(thumbs.size() - 1).getyAxisThumb().getPixelPosition(), grid.getMainLinePaint());
                    break;
                case CORNER:
                    canvas.drawLine(thumbs.get(thumbs.size() - 1).getxAxisThumb().getPixelPosition(), thumbs.get(thumbs.size() - 1).getyAxisThumb().getPixelPosition(), grid.getBounds().right, grid.getBounds().top, grid.getMainLinePaint());
                    break;
            }
        }

    }

    /**
     * Draws the background grid.
     * @param canvas view canvas.
     */
    private void drawGrid(Canvas canvas) {

        canvas.drawRect(grid.getBounds(), grid.getBorderPaint());
        canvas.drawRect(grid.getBounds(), grid.getBackgroundPaint());


        //x axis grid lines
        if(xAxis.getGridSpacing() > 0){
            int numLines = (int) Math.ceil((xAxis.getValueRange() / (float) xAxis.getGridSpacing()) - 1);


            for (int i = 1; i <= numLines; i++) {
                canvas.drawLine(xAxis.valueToPixels(i*xAxis.getGridSpacing() + xAxis.getMinValue()), grid.getBounds().bottom, xAxis.valueToPixels(i*xAxis.getGridSpacing() + xAxis.getMinValue()), grid.getBounds().top, xAxis.getGridPaint());

                if(xAxis.isDrawGridLineText()){
                    String text = String.valueOf(i*xAxis.getGridSpacing() + xAxis.getMinValue());
                    xAxis.getTextPaint().getTextBounds(text, 0, text.length(), textBounds);
                    canvas.drawText(text, xAxis.valueToPixels(i*xAxis.getGridSpacing() + xAxis.getMinValue()) - textBounds.exactCenterX(), xAxis.getBounds().top - textBounds.top + textMargin, xAxis.getTextPaint());
                }
            }
        }
        //y axis grid lines
        if(yAxis.getGridSpacing() > 0){
            int numLines = (int) Math.ceil((yAxis.getValueRange() / (float) yAxis.getGridSpacing()) - 1);
            for (int i = 1; i <= numLines; i++) {
                canvas.drawLine(grid.getBounds().left, yAxis.valueToPixels(i*yAxis.getGridSpacing() + yAxis.getMinValue()), grid.getBounds().right, yAxis.valueToPixels(i*yAxis.getGridSpacing() + yAxis.getMinValue()), yAxis.getGridPaint());

                if(yAxis.isDrawGridLineText()){
                    String text = String.valueOf(i*yAxis.getGridSpacing() + yAxis.getMinValue());
                    yAxis.getTextPaint().getTextBounds(text, 0, text.length(), textBounds);
                    canvas.drawText(text, yAxis.getBounds().right - textBounds.right - textMargin, yAxis.valueToPixels(i*yAxis.getGridSpacing() + yAxis.getMinValue()) - textBounds.exactCenterY(), xAxis.getTextPaint());
                }
            }

        }



    }

    /**
     * Draws the corner text labels.
     * @param canvas view canvas.
     */
    private void drawText(Canvas canvas) {

        String text;
        if(xAxis.isDrawLimitText()){
            text = String.valueOf(xAxis.getMinValue());
            xAxis.getTextPaint().getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, xAxis.getMinPixels(), xAxis.getBounds().top - textBounds.top + textMargin, xAxis.getTextPaint());

            text = String.valueOf(xAxis.getMaxValue());
            xAxis.getTextPaint().getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, xAxis.getMaxPixels() - textBounds.right, xAxis.getBounds().top - textBounds.top + textMargin, xAxis.getTextPaint());
        }
        if(yAxis.isDrawLimitText()){
            text = String.valueOf(yAxis.getMinValue());
            yAxis.getTextPaint().getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, yAxis.getBounds().right - textBounds.right - textMargin, yAxis.getMaxPixels(), yAxis.getTextPaint());

            text = String.valueOf(yAxis.getMaxValue());
            yAxis.getTextPaint().getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, yAxis.getBounds().right - textBounds.right - textMargin, yAxis.getMinPixels() - textBounds.top, yAxis.getTextPaint());
        }

    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        if(!isEnabled()) return false;
        if(thumbs.isEmpty()) return false;

        int action = event.getAction();
        Point touch = new Point((int)event.getX(), (int)event.getY());

        switch (action){
            case MotionEvent.ACTION_DOWN:

                currentTouchDown = touch;
                //x axis touch
                if(xAxis.getBounds().contains(touch.x, touch.y)){

                    //find closest x thumb
                    float shortestDistance = xAxis.getPixelRange();
                    for (int i = 0; i < thumbs.size(); i++) {
                        ThumbPair thumbPair = thumbs.get(i);
                        Thumb thumb = thumbPair.getxAxisThumb();

                        float distance = Math.abs(thumb.getPixelPosition() - touch.x);
                        if (distance <= shortestDistance) {
                            shortestDistance = distance;
                            currentThumbIndex = i;
                        }
                    }
                }
                //y axis touch
                else if(yAxis.getBounds().contains(touch.x, touch.y)){

                    //find closest y thumb
                    float shortestDistance = yAxis.getPixelRange();
                    for (int i = 0; i < thumbs.size(); i++) {
                        ThumbPair thumbPair = thumbs.get(i);
                        Thumb thumb = thumbPair.getyAxisThumb();

                        float distance = Math.abs(thumb.getPixelPosition() - touch.y);
                        if (distance <= shortestDistance) {
                            shortestDistance = distance;
                            currentThumbIndex = i;
                        }
                    }
                }
                //grid touch
                else if(grid.getBounds().contains(touch.x, touch.y)){

                    float shortestDistance = (float) Math.hypot(grid.getBounds().left - grid.getBounds().right, grid.getBounds().bottom - grid.getBounds().top);
                    for (int i = 0; i < thumbs.size(); i++) {
                        ThumbPair thumbPair = thumbs.get(i);
                        float distance = (float) Math.hypot(thumbPair.getxAxisThumb().getPixelPosition() - touch.x, thumbPair.getyAxisThumb().getPixelPosition() - touch.y);
                        if (distance <= shortestDistance) {
                            shortestDistance = distance;
                            currentThumbIndex = i;
                        }
                    }
                }
                handleMove(touch);
                break;


            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:


                break;

            case MotionEvent.ACTION_MOVE:
                handleMove(touch);
                break;
        }

        invalidate();
        return true;
    }

    /**
     * Repositions the thumbs on touch move.
     * @param touch local touch point.
     */
    private void handleMove(Point touch){

        if(thumbs.isEmpty()) return;

        if (xAxis.getBounds().contains(currentTouchDown.x, currentTouchDown.y)){

            Thumb previousThumb = (currentThumbIndex > 0) ? thumbs.get(currentThumbIndex-1).getxAxisThumb() : null;
            Thumb nextThumb = (currentThumbIndex < thumbs.size()-1) ? thumbs.get(currentThumbIndex+1).getxAxisThumb() : null;

            if(previousThumb != null && touch.x < previousThumb.getPixelPosition()){
                currentThumbIndex--;
            }
            if(nextThumb != null && touch.x > nextThumb.getPixelPosition()){
                currentThumbIndex++;
            }
            thumbs.get(currentThumbIndex).getxAxisThumb().setPixelPosition(limitByBounds(touch.x, xAxis.getMinPixels(), xAxis.getMaxPixels()));

            if(onValueChangedListener != null){
                onValueChangedListener.onValueChanged(thumbs.get(currentThumbIndex).getValue(), currentThumbIndex);
                onValueChangedListener.onXAxisSeek(thumbs.get(currentThumbIndex).getxAxisThumb().getValue(), thumbs.get(currentThumbIndex).getValue(), currentThumbIndex);
            }

        }
        else if(yAxis.getBounds().contains(currentTouchDown.x, currentTouchDown.y)){
            thumbs.get(currentThumbIndex).getyAxisThumb().setPixelPosition(limitByBounds(touch.y, yAxis.getMinPixels(), yAxis.getMaxPixels()));
            if(onValueChangedListener != null){
                onValueChangedListener.onValueChanged(thumbs.get(currentThumbIndex).getValue(), currentThumbIndex);
                onValueChangedListener.onYAxisSeek(thumbs.get(currentThumbIndex).getyAxisThumb().getValue(), thumbs.get(currentThumbIndex).getValue(), currentThumbIndex);
            }
        }
        else if(grid.getBounds().contains(currentTouchDown.x, currentTouchDown.y)) {
            Thumb previousThumb = (currentThumbIndex > 0) ? thumbs.get(currentThumbIndex-1).getxAxisThumb() : null;
            Thumb nextThumb = (currentThumbIndex < thumbs.size()-1) ? thumbs.get(currentThumbIndex+1).getxAxisThumb() : null;

            if(previousThumb != null && touch.x < previousThumb.getPixelPosition()) thumbs.get(currentThumbIndex).getxAxisThumb().setPixelPosition(previousThumb.getPixelPosition());
            else if(nextThumb != null && touch.x > nextThumb.getPixelPosition()) thumbs.get(currentThumbIndex).getxAxisThumb().setPixelPosition(nextThumb.getPixelPosition());
            else {
                thumbs.get(currentThumbIndex).getxAxisThumb().setPixelPosition(limitByBounds(touch.x, xAxis.getMinPixels(), xAxis.getMaxPixels()));
                thumbs.get(currentThumbIndex).getyAxisThumb().setPixelPosition(limitByBounds(touch.y, yAxis.getMinPixels(), yAxis.getMaxPixels()));
            }

            if(onValueChangedListener != null){
                onValueChangedListener.onValueChanged(thumbs.get(currentThumbIndex).getValue(), currentThumbIndex);
            }

        }
    }

    /**
     * Clips a value by the given bounds.
     * @param actualValue value to be clipped.
     * @param lowLimit the bottom limit.
     * @param highLimit the top limit.
     * @return clipped value that is always between the lowLimit and highLimit inclusive.
     */
    private float limitByBounds(float actualValue, float lowLimit, float highLimit){
        float finalValue = actualValue;
        finalValue = Math.max(finalValue, lowLimit);
        finalValue = Math.min(finalValue, highLimit);
        return finalValue;
    }

    /**
     * Sets the listener that is notified when the seek bar values are changed.
     * @param onValueChangedListener listener.
     */
    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    public interface OnValueChangedListener{
        void onValueChanged(Point value2D, int thumbIndex);
        void onXAxisSeek(int xValue, Point value2D, int thumbIndex);
        void onYAxisSeek(int yValue, Point value2D, int thumbIndex);
    }


    /**
     * Returns true if the view is saving thumb state on acitivity recreation.
     * @return true if saving state.
     */
    public boolean isSaveInstanceState() {
        return saveInstanceState;
    }

    /**
     * Configures whether the view is saving the thumb state on activity recreation.
     * @param saveInstanceState true if the view should be saving the state.
     */
    public void setSaveInstanceState(boolean saveInstanceState) {
        this.saveInstanceState = saveInstanceState;
    }

    /**
     * Returns the starting cap type.
     * @return cap type on the bottom left side.
     */
    public CapType getCapTypeStart() {
        return capTypeStart;
    }

    /**
     * Returns sets how the main line is drawn on the bottom left corner.
     * @param capTypeStart type of the cap.
     */
    public void setCapTypeStart(CapType capTypeStart) {
        this.capTypeStart = capTypeStart;
    }

    /**
     * Returns the ending cap type.
     * @return cap type on the top right side.
     */
    public CapType getCapTypeEnd() {
        return capTypeEnd;
    }

    /**
     * Returns sets how the main line is drawn on the top right corner.
     * @param capTypeEnd type of the cap.
     */
    public void setCapTypeEnd(CapType capTypeEnd) {
        this.capTypeEnd = capTypeEnd;
    }

    /**
     * Returns the thumb pairs. Ordered by the x value.
     * @return list of thumb pairs.
     */
    public ArrayList<ThumbPair> getThumbs() {
        return thumbs;
    }

    /**
     * Returns the x axis.
     * @return x axis.
     */
    public Axis getxAxis() {
        return xAxis;
    }

    /**
     * Returns the y axis.
     * @return y axis.
     */
    public Axis getyAxis() {
        return yAxis;
    }

    /**
     * Returns the grid.
     * @return grid.
     */
    public Grid getGrid() {
        return grid;
    }
}
