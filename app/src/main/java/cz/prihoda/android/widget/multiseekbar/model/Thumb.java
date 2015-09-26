package cz.prihoda.android.widget.multiseekbar.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Movable thumb.
 * Created by Adam Příhoda on 14.09.2015.
 */
public class Thumb  implements Serializable{

    private float pixelPosition;
    private int value;

    private Axis axis;
    private Drawable drawable;

    public Thumb(Axis axis, Drawable drawable) {
        this.axis = axis;
        this.drawable = drawable;
    }

    /**
     * Returns the value of the thumb.
     * @return thumb value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value of the thumb. Invalidate the view to refresh the visual position.
     * @param value new thumb value.
     */
    public void setValue(int value) {
        this.value = value;
        this.pixelPosition = axis.valueToPixels(value);
    }

    /**
     * Returns the axis this thumb is on.
     * @return axis.
     */
    public Axis getAxis() {
        return axis;
    }

    /**
     * Sets the axis this thumb is on.
     * @param axis axis.
     */
    public void setAxis(Axis axis) {
        this.axis = axis;
        this.onMeasure();
    }

    /**
     * Returns the position of the thumb in view pixels.
     * @return pixel position.
     */
    public float getPixelPosition() {
        return pixelPosition;
    }

    /**
     * Sets the pixel position of the thumb. Updates the value automatically. Invalidate the view to refresh the visual position.
     * @param pixelPosition new pixel position.
     */
    public void setPixelPosition(float pixelPosition) {
        this.pixelPosition = pixelPosition;
        this.value = axis.pixelsToValue(pixelPosition);
    }

    /**
     * Returns the thumb drawable.
     * @return thumb drawable.
     */
    public Drawable getDrawable() {
        return drawable;
    }

    /**
     * Sets the thumb drawable.
     * @param drawable new drawable.
     */
    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    /**
     * Recalculates the pixel position after the size of the view changed.
     */
    public void onMeasure(){
        this.setValue(getValue());
    }

}
