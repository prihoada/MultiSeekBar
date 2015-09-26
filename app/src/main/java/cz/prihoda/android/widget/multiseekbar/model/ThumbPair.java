package cz.prihoda.android.widget.multiseekbar.model;

import android.graphics.Point;

import java.io.Serializable;

/**
 * Pair of thumbs representing a 2D value.
 * Created by Adam Příhoda on 14.09.2015.
 */
public class ThumbPair implements Serializable {

    private Thumb yAxisThumb;
    private Thumb xAxisThumb;

    public ThumbPair(Thumb xAxisThumb, Thumb yAxisThumb) {
        this.yAxisThumb = yAxisThumb;
        this.xAxisThumb = xAxisThumb;
    }

    /**
     * Returns the combined value from both the thumbs.
     * @return 2D value.
     */
    public Point getValue(){
        return new Point(xAxisThumb.getValue(), yAxisThumb.getValue());
    }

    /**
     * Returns the thumb on the x axis.
     * @return x axis thumb.
     */
    public Thumb getyAxisThumb() {
        return yAxisThumb;
    }
    /**
     * Returns the thumb on the y axis.
     * @return y axis thumb.
     */
    public Thumb getxAxisThumb() {
        return xAxisThumb;
    }
}
