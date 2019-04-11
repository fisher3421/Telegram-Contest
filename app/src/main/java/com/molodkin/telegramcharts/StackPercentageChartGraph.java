package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

class StackPercentageChartGraph extends BaseChartGraph {

    final Path path = new Path();

    @Override
    int getMax(int start, int end) {
        return 0;
    }

    @Override
    int getMin(int start, int end) {
        return 0;
    }

    StackPercentageChartGraph(int[] values, int color, String name) {
        super(values, name, color);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        scrollPaint = new Paint(paint);
    }

    @Override
    void draw(Canvas canvas, Matrix matrix, int start, int end) {
        drawPath(canvas, matrix, path, paint);
    }

    @Override
    void drawScroll(Canvas canvas, Matrix matrix) {
        drawPath(canvas, matrix, path, scrollPaint);
    }

    private void drawPath(Canvas canvas, Matrix matrix, Path path, Paint paint) {
        canvas.save();
        canvas.concat(matrix);

        canvas.drawPath(path, paint);

        canvas.restore();
    }
}
