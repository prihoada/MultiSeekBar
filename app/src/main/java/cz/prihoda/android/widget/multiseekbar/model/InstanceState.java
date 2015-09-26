package cz.prihoda.android.widget.multiseekbar.model;

import android.graphics.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Simple data set holder for use in serialization.
 * Created by Adam Příhoda on 26.09.2015.
 */
public class InstanceState implements Serializable {
    private ArrayList<SerializablePoint> values;
    public InstanceState(ArrayList<Point> values) {
        this.values = new ArrayList<>();
        for (Point point : values) {
            this.values.add(new SerializablePoint(point));
        }
    }

    public ArrayList<Point> getValues() {
        ArrayList<Point> points = new ArrayList<>();
        for (SerializablePoint serializablePoint : this.values) {
            points.add(serializablePoint.getPoint());
        }
        return points;
    }

    private class SerializablePoint implements Serializable
    {
        public int x;
        public int y;

        public SerializablePoint(Point point) {
            this.x = point.x;
            this.y = point.y;
        }
        public Point getPoint(){
            return new Point(this.x, this.y);
        }
    }

}
