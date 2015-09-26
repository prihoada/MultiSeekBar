package cz.prihoda.android.widget.multiseekbar.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;

import cz.prihoda.android.widget.multiseekbar.R;
import cz.prihoda.android.widget.multiseekbar.utils.DimensionUtils;

/**
 * View axis.
 * Created by Adam Příhoda on 14.09.2015.
 */
public class Axis {


    private int minValue;
    private int maxValue;
    private int valueRange;

    private int thumbWidth;
    private int thumbHeight;
    private Drawable thumbDrawable;
    private List<Thumb> thumbs;

    private float minPixels;
    private float maxPixels;
    private float pixelRange;

    private Rect bounds;

    private boolean drawLineHelpers;
    private int lineHelperColor;
    private float lineHelperWidth;
    private final Paint lineHelperPaint;
    private final Paint gridPaint;
    private final Paint textPaint;
    private boolean drawLimitText;
    private boolean drawGridLineText;

    private int gridSpacing;


    private boolean inverted;

    private Context context;

    public Axis(Context context) {
        this.context = context;
        thumbs = new ArrayList<>();

        drawLineHelpers = true;
        lineHelperPaint = new Paint();
        lineHelperColor = Color.BLACK;
        lineHelperPaint.setColor(lineHelperColor);
        lineHelperPaint.setStyle(Paint.Style.STROKE);
        lineHelperPaint.setStrokeWidth(0);

        gridPaint = new Paint();
        gridPaint.setColor(context.getResources().getColor(R.color.grid_grid));
        gridPaint.setStrokeWidth(0);
        gridPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(context.getResources().getColor(R.color.grid_text));
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics()));
        textPaint.setAntiAlias(true);
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        valueRange = maxValue - minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        valueRange = maxValue - minValue;
    }

    public int getValueRange() {
        return valueRange;
    }

    public float getMinPixels() {
        return minPixels;
    }

    public void setMinPixels(float minPixels) {
        this.minPixels = minPixels;
        this.pixelRange = maxPixels - minPixels;
    }

    public float getMaxPixels() {
        return maxPixels;
    }

    public void setMaxPixels(float maxPixels) {
        this.maxPixels = maxPixels;
        this.pixelRange = maxPixels - minPixels;
    }

    public float getPixelRange() {
        return pixelRange;
    }

    public int pixelsToValue(float pixels){
        pixels -= minPixels;
        float pixelRatio = pixels/pixelRange;

        int value = Math.round(valueRange * pixelRatio);
        value += minValue;

        if(inverted) value = maxValue - value;
        return value;
    }

    public float getWidth() {
        return getBounds().right - getBounds().left;
    }

    public float getHeight() {
        return getBounds().bottom - getBounds().top;
    }

    public Rect getBounds() {
        if(bounds == null) bounds = new Rect();
        return bounds;
    }

    public float valueToPixels(int value){
        if(inverted) value = maxValue - value;

        value -= minValue;
        float valueRatio = value/(float)valueRange;

        float pixels = pixelRange * valueRatio;
        pixels += minPixels;

        return pixels;
    }

    public void setDrawLineHelpers(boolean drawLineHelpers) {
        this.drawLineHelpers = drawLineHelpers;
    }
    public boolean isDrawLineHelpers() {
        return drawLineHelpers;
    }
    public int getLineHelperColor() {
        return lineHelperColor;
    }
    public void setLineHelperColor(int lineHelperColor) {
        this.lineHelperColor = lineHelperColor;
        this.lineHelperPaint.setColor(lineHelperColor);
    }
    public float getLineHelperWidth() {
        return lineHelperWidth;
    }
    public void setLineHelperWidth(float lineHelperWidthDp) {
        this.lineHelperWidth = lineHelperWidthDp;
        this.lineHelperPaint.setStrokeWidth(DimensionUtils.dpToPixels(context, lineHelperWidthDp));
    }
    public Paint getLineHelperPaint() {
        return lineHelperPaint;
    }

    public Paint getGridPaint() {
        return gridPaint;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public int getGridSpacing() {
        return gridSpacing;
    }
    public void setGridSpacing(int gridSpacing) {
        this.gridSpacing = gridSpacing;
    }


    public int getThumbWidth() {
        return thumbWidth;
    }
    public int getThumbHeight() {
        return thumbHeight;
    }
    public Drawable getThumbDrawable() {
        return thumbDrawable.getConstantState().newDrawable();
    }
    public void setThumbDrawable(@NonNull Drawable thumbDrawable) {
        this.thumbDrawable = thumbDrawable;
        for (Thumb thumb : thumbs) {
            thumb.setDrawable(this.thumbDrawable.getConstantState().newDrawable(context.getResources()));
        }
        this.thumbHeight = thumbDrawable.getIntrinsicHeight();
        this.thumbWidth = thumbDrawable.getIntrinsicWidth();
    }

    public List<Thumb> getThumbs() {
        return thumbs;
    }

    public boolean isDrawLimitText() {
        return drawLimitText;
    }

    public void setDrawLimitText(boolean drawLimitText) {
        this.drawLimitText = drawLimitText;
    }

    public boolean isDrawGridLineText() {
        return drawGridLineText;
    }

    public void setDrawGridLineText(boolean drawGridLineText) {
        this.drawGridLineText = drawGridLineText;
    }

    @Override
    public String toString() {
        return "Axis{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", valueRange=" + valueRange +
                ", minPixels=" + minPixels +
                ", maxPixels=" + maxPixels +
                ", pixelRange=" + pixelRange +
                ", bounds=" + bounds +
                '}';
    }
}
