package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

class ChartGraph {

    final int [] values;

    private final int [][] maxValuesMatrix;

    final Paint linePaint;

    final Paint scrollLinePaint;

    boolean isEnable = true;

    float[] linePoints;

    final String name;

    private float[] tempLinePoints;

    int color;

    int getMax(int start, int end) {
        return maxValuesMatrix[start][end - 1];
    }

    ChartGraph(int[] values, int color, float width, String name) {
        this.values = values;

        maxValuesMatrix = new int[values.length][values.length];
        this.name = name;

        for (int i = 0; i < values.length; i++) {
            maxValuesMatrix[i][i] = values[i];
            for (int j = i + 1; j < values.length; j++) {
                maxValuesMatrix[i][j] = Math.max(maxValuesMatrix[i][j - 1], values[j]);
            }
        }

        this.color = color;

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(width);
        linePaint.setColor(color);
        linePaint.setStrokeCap(Paint.Cap.SQUARE);

        scrollLinePaint = new Paint(linePaint);
        scrollLinePaint.setStrokeWidth(width / 2);

        linePoints = new float[values.length * 4 - 4];
        tempLinePoints = new float[values.length * 4 - 4];
    }

    void draw(Canvas canvas, Matrix matrix, int start, int end) {
        draw(canvas, matrix, linePaint, start, end);
    }

    void drawScroll(Canvas canvas, Matrix matrix) {
        draw(canvas, matrix, scrollLinePaint, 0, values.length);
    }

    private void draw(Canvas canvas, Matrix matrix, Paint linePaint, int start, int end) {
        int startLineIndex = start * 4;
        int countLineIndex = (end - start - 1) * 2;

        matrix.mapPoints(tempLinePoints, startLineIndex, linePoints, startLineIndex, countLineIndex);

        canvas.drawLines(tempLinePoints, start * 4, (end - start - 1) * 4, linePaint);
    }
}
