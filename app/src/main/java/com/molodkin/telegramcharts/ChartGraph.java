package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Random;

public class ChartGraph {

    public final int [] values;

    private final int [][] maxValuesMatrix;

    public final Paint paint;

    public final Paint scrollPaint;

    public boolean isEnable = true;

    public Path path = new Path();

    public final String name;

    public int getMax(int start, int end) {
        return maxValuesMatrix[start][end - 1];
    }

    public ChartGraph(int[] values, int color, float width) {
        this.values = values;

        maxValuesMatrix = new int[values.length][values.length];

        for (int i = 0; i < values.length; i++) {
            maxValuesMatrix[i][i] = values[i];
            for (int j = i + 1; j < values.length; j++) {
                maxValuesMatrix[i][j] = Math.max(maxValuesMatrix[i][j - 1], values[j]);
            }
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(width);
        paint.setColor(color);

        scrollPaint = new Paint(paint);
        scrollPaint.setStrokeWidth(width / 2);

        name = String.valueOf(new Random().nextInt());
    }

    public void draw(Canvas canvas, Matrix matrix) {
        Path transform = new Path(path);

        transform.transform(matrix);

        canvas.drawPath(transform, paint);
    }

    public void drawScroll(Canvas canvas, Matrix matrix) {
        Path transform = new Path(path);

        transform.transform(matrix);

        canvas.drawPath(transform, scrollPaint);
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
