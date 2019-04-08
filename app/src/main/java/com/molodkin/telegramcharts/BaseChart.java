package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Matrix;
import android.view.View;

abstract class BaseChart extends View {

    static final long SCALE_ANIMATION_DURATION = 250L;
    static final long FADE_ANIMATION_DURATION = 150L;

    private float[] tempPoint = new float[2];

    long[] xPoints;

    ChartGraph[] graphs;

    public final Matrix chartMatrix = new Matrix();
    public final Matrix chartInvertMatrix = new Matrix();

    int start = 0;
    int end = 0;

    int drawStart = 0;
    int drawEnd = 0;

    int yAdjustStart = 0;
    int yAdjustEnd = 0;

    int maxYValueTemp;
    int maxYValue;

    int sideMargin = Utils.getDim(this, R.dimen.margin20);

    public BaseChart(Context context) {
        super(context);
    }

    abstract void setStart(int start);
    abstract void setEnd(int end);
    abstract void setStartEnd(int start, int end);

    abstract int getMaxRangeValue();

    abstract void adjustYAxis();

    float xCoordByIndex(int x) {
        tempPoint[0] = x;
        chartMatrix.mapPoints(tempPoint);
        return sideMargin + tempPoint[0];
    }

    int xIndexByCoord(float x) {
        return Math.round(xValueByCoord(x));
    }

    float xValueByCoord(float x) {
        tempPoint[0] = x - sideMargin;

        chartMatrix.invert(chartInvertMatrix);
        chartInvertMatrix.mapPoints(tempPoint);

        float value = tempPoint[0];
        return Math.max(Math.min(value, xPoints.length - 1), 0);
    }

    float yCoordByValue(float y) {
        tempPoint[1] = maxYValue - y;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[1];
    }

}
