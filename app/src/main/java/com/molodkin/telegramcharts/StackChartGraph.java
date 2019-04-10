package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

class StackChartGraph extends BaseChartGraph {

    @Override
    int getMax(int start, int end) {
        return 0;
    }

    @Override
    int getMin(int start, int end) {
        return 0;
    }

    StackChartGraph(int[] values, int color, float width, String name) {
        super(values, name, color);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(width);
        linePaint.setColor(color);

        scrollLinePaint = new Paint(linePaint);
        scrollLinePaint.setStrokeWidth(width / 2);

        linePoints = new float[values.length * 4];
        tempLinePoints = new float[values.length * 4];
    }

    @Override
    void draw(Canvas canvas, Matrix matrix, int start, int end) {
        drawLines(canvas, matrix, linePaint, start, end + 1);
    }

    @Override
    void drawScroll(Canvas canvas, Matrix matrix) {
        drawLines(canvas, matrix, scrollLinePaint, 0, values.length);
    }
}
