package cz.prihoda.android.widget.multiseekbar.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Adam Příhoda on 17.09.2015.
 */
public class DimensionUtils {

    public static float dpToPixels(Context context, float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
    public static float pixelsToDp(Context context, float value){
        return value / context.getResources().getDisplayMetrics().density;
    }

}
