package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

abstract class BaseChartGraph {

    float[] linePoints;
    float[] tempLinePoints;

    final int[] values;

    public final String name;

    float alpha = 1;

    Paint linePaint;

    Paint scrollLinePaint;

    boolean isEnable = true;

    public final int color;

    protected BaseChartGraph(int[] values, String name, int color) {
        this.values = values;
        this.name = name;
        this.color = color;

        linePoints = new float[(values.length - 1) * 4];
        tempLinePoints = new float[(values.length - 1) * 4];
    }

    abstract int getMax(int start, int end);

    abstract int getMin(int start, int end);

    abstract void draw(Canvas canvas, Matrix matrix, int start, int end);

    abstract  void drawScroll(Canvas canvas, Matrix matrix);

    void drawLines(Canvas canvas, Matrix matrix, Paint linePaint, int start, int end) {
        int startLineIndex = start * 4;
        int countLineIndex = (end - start - 1) * 2;

        matrix.mapPoints(tempLinePoints, startLineIndex, linePoints, startLineIndex, countLineIndex);

        canvas.drawLines(tempLinePoints, start * 4, (end - start - 1) * 4, linePaint);
    }
}
