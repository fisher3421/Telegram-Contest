package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Arrays;

public class ChartGraph {

    //wrong structure
    private final Point [] sortedValues;

    public final int [] values;

    public final Paint paint;

    public boolean isEnable = true;

    public Path path = new Path();

    public int getMax(int start, int end) {
        for (int i = sortedValues.length - 1; i >= 0; i--) {
            int x = sortedValues[i].x;
            int y = sortedValues[i].y;
            if (x >= start && x < end) {
                return y;
            }
        }
        return 0;
    }

    public ChartGraph(int[] values, int color, float width) {
        this.values = values;

        sortedValues = new Point[values.length];

        for (int i = 0; i < sortedValues.length; i++) {
            sortedValues[i] = new Point(i, values[i]);
        }

        Arrays.sort(sortedValues);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(color);
    }

    public void draw(Canvas canvas, Matrix matrix) {
        Path transform = new Path(path);

        transform.transform(matrix);

        canvas.drawPath(transform, paint);
    }

    private static class Point implements Comparable<Point> {
        private final int x;
        private final int y;

        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Point o) {
            return y - o.y;
        }
    }
}
