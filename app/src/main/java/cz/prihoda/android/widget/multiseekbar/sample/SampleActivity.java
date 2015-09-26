package cz.prihoda.android.widget.multiseekbar.sample;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import cz.prihoda.android.widget.multiseekbar.MultiSeekBar;
import cz.prihoda.android.widget.multiseekbar.R;

public class SampleActivity extends AppCompatActivity {

    private static final String TAG = SampleActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);


        MultiSeekBar seekBar = (MultiSeekBar) findViewById(R.id.seek_bar);
        seekBar.addThumbPair(new Point(20,20));
        seekBar.addThumbPair(new Point(50,50));
        seekBar.setOnValueChangedListener(new MultiSeekBar.OnValueChangedListener() {
            @Override
            public void onValueChanged(Point value2D, int thumbIndex) {
                Log.e(TAG, "Value changed: " + value2D);
            }

            @Override
            public void onXAxisSeek(int xValue, Point value2D, int thumbIndex) {
                Log.e(TAG, "X axis thumb dragged: " + xValue);
            }

            @Override
            public void onYAxisSeek(int yValue, Point value2D, int thumbIndex) {
                Log.e(TAG, "Y axis thumb dragged: " + yValue);
            }
        });

    }
}
