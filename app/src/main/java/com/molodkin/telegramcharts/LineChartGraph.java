package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

class LineChartGraph extends BaseChartGraph {


    private final int [][] maxValuesMatrix;
    private final int [][] minValuesMatrix;

    private Paint alphaLinePaint;
    private Paint alphaScrollPaint;

    @Override
    int getMax(int start, int end) {
        return maxValuesMatrix[start][end - 1];
    }

    @Override
    int getMin(int start, int end) {
        return minValuesMatrix[start][end - 1];
    }

    LineChartGraph(int[] values, int color, float width, String name) {
        super(values, name, color);

        maxValuesMatrix = new int[values.length][values.length];
        minValuesMatrix = new int[values.length][values.length];

        for (int i = 0; i < values.length; i++) {
            maxValuesMatrix[i][i] = values[i];
            minValuesMatrix[i][i] = values[i];
            for (int j = i + 1; j < values.length; j++) {
                maxValuesMatrix[i][j] = Math.max(maxValuesMatrix[i][j - 1], values[j]);
                minValuesMatrix[i][j] = Math.min(minValuesMatrix[i][j - 1], values[j]);
            }
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(width);
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.SQUARE);

        alphaLinePaint = new Paint(paint);

        scrollPaint = new Paint(paint);
        scrollPaint.setStrokeWidth(width / 2);

        alphaScrollPaint = new Paint(scrollPaint);
    }

    @Override
    void draw(Canvas canvas, Matrix matrix, int start, int end) {
        alphaLinePaint.setAlpha((int) (alpha * 255));
        drawLines(canvas, matrix, alphaLinePaint, start, end);
    }

    @Override
    void drawScroll(Canvas canvas, Matrix matrix) {
        alphaScrollPaint.setAlpha((int) (alpha * 255));
        drawLines(canvas, matrix, alphaScrollPaint, 0, values.length);
    }


}
