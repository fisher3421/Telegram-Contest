package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

import static com.molodkin.telegramcharts.Utils.log;

abstract class BaseChart extends View {

    static final long SCALE_ANIMATION_DURATION = 250L;
    static final long FADE_ANIMATION_DURATION = 150L;

    public XAxis xAxis = new XAxis(this);

    public int sideMargin = Utils.getDim(this, R.dimen.margin20);
    public int clipMargin = Utils.dpToPx(this, 1);
    public int xAxisHeight = Utils.getDim(this, R.dimen.xAxisHeight);
    public  int xAxisWidth = Utils.getDim(this, R.dimen.xAxisWidth);

    public int graphLineWidth = Utils.dpToPx(this, 2);

    private float[] tempPoint = new float[2];

    long[] xPoints;

    BaseChartGraph[] graphs;

    BaseYAxis yAxis1;
    BaseYAxis yAxis2;

    public final Matrix chartMatrix = new Matrix();
    public final Matrix chartMatrix2 = new Matrix();
    public final Matrix chartInvertMatrix = new Matrix();

    int start = 0;
    int end = 0;

    int drawStart = 0;
    int drawEnd = 0;

    int yAdjustStart = 0;
    int yAdjustEnd = 0;

    float availableChartHeight;
    float availableChartWidth;

    private Paint axisPaint = new Paint();

    boolean secondY = false;

    public ChartData data;

    public BaseChart(Context context) {
        super(context);
        init();
    }

    abstract void initGraphs();

    public void init() {
        initPaints();
        initGraphs();
    }

    public void initPaints() {
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        initTheme();
    }

    public void enableGraph(final int index, boolean enable) {
        graphs[index].isEnable = enable;

        float fromAlpha = enable ? 0 : 1f;
        float toAlpha = enable ? 1f : 0f;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromAlpha, toAlpha);
        valueAnimator.setDuration(FADE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                graphs[index].alpha = (float) animation.getAnimatedValue();
                graphAlphaChanged();
                invalidate();

            }
        });
        valueAnimator.start();

        adjustYAxis();
    }

    protected void graphAlphaChanged() {}

    public void setStart(int start) {
        if (start >= end - 2) return;
        if (start == this.start) return;

        float toScale = availableChartWidth / (xCoordByIndex(end - 1) - xCoordByIndex(start));
        startScaleAnimation(toScale, true);
        this.start = start;

        this.yAdjustStart = xIndexByCoord(0);
        this.yAdjustEnd = xIndexByCoord(getWidth()) + 1;

        adjustYAxis();
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

        if (end == this.end) return;

        float toScale = availableChartWidth / (xCoordByIndex(end - 1) - xCoordByIndex(start));
        startScaleAnimation(toScale, false);

        this.end = end;

        this.yAdjustStart = xIndexByCoord(0);
        this.yAdjustEnd = xIndexByCoord(getWidth()) + 1;

        adjustYAxis();
    }

    public void setStartEnd(int start, int end) {
        if (end > xPoints.length) return;
        if (start >= end - 1) return;

        if (start == this.start && end == this.end) return;

        final float[] prev = new float[1];
        prev[0] = 0f;

        float fromCoordStart = xCoordByIndex(this.start);
        float toCoordStart = xCoordByIndex(start);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, fromCoordStart - toCoordStart);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                chartMatrix.postTranslate(value - prev[0], 0);
                chartMatrix2.postTranslate(value - prev[0], 0);
                drawStart = xIndexByCoord(0);
                drawEnd = xIndexByCoord(getWidth()) + 1;
                prev[0] = value;
                xAxis.adjustXAxis();
                invalidate();

            }
        });
        valueAnimator.start();

        this.start = start;
        this.end = end;

        this.yAdjustStart = xIndexByCoord(0);
        this.yAdjustEnd = xIndexByCoord(getWidth()) + 1;

        adjustYAxis();
    }

    private void startScaleAnimation(float toScale, final boolean isStart) {
        float fromScale = availableChartWidth / (xCoordByIndex(end - 1) - xCoordByIndex(start));

        log("fromScale: " + fromScale);
        log("toScale: " + toScale);

        final float[] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                chartMatrix.postScale(value / prev[0], 1, isStart ? availableChartWidth : 0f, 0f);
                chartMatrix2.postScale(value / prev[0], 1, isStart ? availableChartWidth : 0f, 0f);
                drawStart = xIndexByCoord(0);
                drawEnd = xIndexByCoord(getWidth()) + 1;
                prev[0] = value;
                xAxis.adjustXAxis();
                invalidate();

            }
        });
        valueAnimator.start();
    }

    public void setData(ChartData data) {
        this.data = data;
        if (getWidth() > 0 && getHeight() > 0) {
            initGraphs();
        }
    }

    public void initTheme() {
        if (yAxis1 != null) yAxis1.updateTheme();
        if (yAxis2 != null) yAxis2.updateTheme();
        xAxis.updateTheme();
    }

    public void updateTheme() {
        initTheme();
        invalidate();
    }

    public void adjustYAxis() {
        if (yAxis1 != null) yAxis1.adjustYAxis();
        if (yAxis2 != null) yAxis2.adjustYAxis();
    }

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
        tempPoint[1] = yAxis1.maxValue - y;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[1];
    }

    float yCoordByValue2(float y) {
        tempPoint[1] = yAxis2.maxValue - y;
        chartMatrix2.mapPoints(tempPoint);
        return tempPoint[1];
    }
}
