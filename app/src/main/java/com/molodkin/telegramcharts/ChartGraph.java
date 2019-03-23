package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class ChartGraph {

    public final int [] values;

    private final int [][] maxValuesMatrix;

    public final Paint linePaint;
    public final Paint pointPaint;

    public final Path path = new Path();

    public final Paint scrollLinePaint;
    public final Paint scrollPointPaint;

    public boolean isEnable = true;

    public float[] linePoints;
    public float[] points;

    public final String name;

    private float[] tempLinePoints;
    private float[] tempPoints;

    private final float width;

    private final float [] chartMatrixValues = new float[9];

    public int getMax(int start, int end) {
        return maxValuesMatrix[start][end - 1];
    }

    public ChartGraph(int[] values, int color, float width, String name) {
        this.values = values;

        maxValuesMatrix = new int[values.length][values.length];
        this.name = name;

        for (int i = 0; i < values.length; i++) {
            maxValuesMatrix[i][i] = values[i];
            for (int j = i + 1; j < values.length; j++) {
                maxValuesMatrix[i][j] = Math.max(maxValuesMatrix[i][j - 1], values[j]);
            }
        }

        this.width = width;

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(width);
        linePaint.setColor(color);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(color);
        pointPaint.setStrokeWidth(width);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);

        scrollLinePaint = new Paint(linePaint);
        scrollLinePaint.setStrokeWidth(width / 2);

        scrollPointPaint = new Paint(pointPaint);
        scrollPointPaint.setStrokeWidth(width / 2);

        linePoints = new float[values.length * 4 - 4];
        tempLinePoints = new float[values.length * 4 - 4];
        points = new float[values.length * 2];
        tempPoints = new float[values.length * 2];
    }

    public void draw(Canvas canvas, Matrix matrix, int start, int end) {
        draw(canvas, matrix, linePaint, pointPaint, start, end);
//        drawPath(canvas, matrix);
    }

    public void drawScroll(Canvas canvas, Matrix matrix) {
        draw(canvas, matrix, scrollLinePaint, scrollPointPaint, 0, values.length);
    }

    private void drawPath(Canvas canvas, Matrix matrix) {
        matrix.getValues(chartMatrixValues);
        linePaint.setStrokeWidth(width / chartMatrixValues[0]);
        canvas.drawPath(path, linePaint);
    }

    private void draw(Canvas canvas, Matrix matrix, Paint linePaint, Paint pointPaint, int start, int end) {
        int startLineIndex = start * 4;
        int countLineIndex = (end - start - 1) * 2;

        int startPointIndex = start * 2;
        int countPointIndex = (end - start);

        matrix.mapPoints(tempLinePoints, startLineIndex, linePoints, startLineIndex, countLineIndex);
        matrix.mapPoints(tempPoints, startPointIndex, points, startPointIndex, countPointIndex);

        canvas.drawLines(tempLinePoints, start * 4, (end - start - 1) * 4, linePaint);
        canvas.drawPoints(tempPoints, start * 2, (end - start) * 2, pointPaint);
    }
}
