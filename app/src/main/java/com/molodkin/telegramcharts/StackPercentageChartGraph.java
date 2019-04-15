package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

class StackPercentageChartGraph extends BaseChartGraph {

    final Path path = new Path();

    final int [][] sumValuesMatrix;

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

        piePaint = new Paint(paint);
        piePaint.setAntiAlias(true);

        scrollPaint = new Paint(paint);

        sumValuesMatrix = new int[values.length][values.length];

        for (int i = 0; i < values.length; i++) {
            sumValuesMatrix[i][i] = values[i];
            for (int j = i + 1; j < values.length; j++) {
                sumValuesMatrix[i][j] = sumValuesMatrix[i][j - 1] + values[j];
            }
        }
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
