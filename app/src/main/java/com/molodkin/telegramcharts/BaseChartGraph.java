package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

abstract class BaseChartGraph {

    public final int[] values;

    public final String name;

    public Paint linePaint;

    public Paint scrollLinePaint;

    public boolean isEnable = true;

    public final int color;

    protected BaseChartGraph(int[] values, String name, int color) {
        this.values = values;
        this.name = name;
        this.color = color;
    }

    abstract int getMax(int start, int end);

    abstract int getMin(int start, int end);

    abstract void draw(Canvas canvas, Matrix matrix, int start, int end);

    abstract  void drawScroll(Canvas canvas, Matrix matrix);
}
