package cz.prihoda.android.widget.multiseekbar.model;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import cz.prihoda.android.widget.multiseekbar.R;
import cz.prihoda.android.widget.multiseekbar.utils.DimensionUtils;

/**
 * Background grid.
 * Created by Adam Příhoda on 16.09.2015.
 */
public class Grid {

    private Rect bounds;

    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint mainLinePaint;

    public Grid(Context context) {
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(context.getResources().getColor(R.color.grid_background));

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(DimensionUtils.dpToPixels(context, 1));
        borderPaint.setColor(context.getResources().getColor(R.color.grid_border));

        mainLinePaint = new Paint();
        mainLinePaint.setColor(context.getResources().getColor(R.color.grid_main_line));
        mainLinePaint.setStrokeWidth(DimensionUtils.dpToPixels(context, 2));
        mainLinePaint.setStyle(Paint.Style.STROKE);
    }

    public Rect getBounds() {
        if(bounds == null) bounds = new Rect();
        return bounds;
    }

    public int getWidth(){
        return getBounds().right - getBounds().left;
    }
    public int getHeight(){
        return getBounds().bottom - getBounds().top;
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    public Paint getBorderPaint() {
        return borderPaint;
    }

    public Paint getMainLinePaint() {
        return mainLinePaint;
    }
}
