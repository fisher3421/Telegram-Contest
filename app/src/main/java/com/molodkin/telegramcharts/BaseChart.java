package com.molodkin.telegramcharts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.view.View;
import java.util.ArrayList;

import static com.molodkin.telegramcharts.Utils.log;

abstract class BaseChart extends View {

    protected boolean enablingWithAlphaAnimation = true;

    static final long ZOOM_ANIMATION_DURATION = 250L;
    static final long SCALE_ANIMATION_DURATION = 250L;
    static final long FADE_ANIMATION_DURATION = 250L;

    public XAxis xAxis;

    public int sideMargin = Utils.getDim(this, R.dimen.margin20);
    public int clipMargin = Utils.dpToPx(this, 1);
    public  int graphTopMargin = Utils.getDim(this, R.dimen.graphTopMargin);
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

    int visibleStart = 0;
    int visibleEnd = 0;

    float availableChartHeight;
    float availableChartWidth;

    public ChartData data;

    ValueAnimator zoomAnimator;
    boolean isGone = false;
    boolean isZoomed;
    boolean isOpening = false;
    private float lastZoomedX;

    private ChartListener chartListener;

    public BaseChart(Context context, boolean isZoomed) {
        super(context);
        this.isZoomed = isZoomed;
        init();
    }

    public void setChartListener(ChartListener chartListener) {
        this.chartListener = chartListener;
    }

    abstract void initGraphs();

    public void init() {
        initPaints();
        initGraphs();
    }

    public void initPaints() {
        initTheme();
    }

    public void enableAll(boolean enable, int exceptionIndex, boolean exceptionEnable) {
        ArrayList<BaseChartGraph> graphsToChange = new ArrayList<>();

        if (graphs[exceptionIndex].isEnable != exceptionEnable) graphsToChange.add(graphs[exceptionIndex]);

        for (int i = 0; i < graphs.length; i++) {
            if (i == exceptionIndex) continue;
            BaseChartGraph graph = graphs[i];
            if (graph.isEnable != enable) graphsToChange.add(graph);
        }
        enableGraph(graphsToChange.toArray(new BaseChartGraph[graphsToChange.size()]));
    }

    public void enableGraph(final int index) {
        enableGraph(graphs[index]);
    }

    private void enableGraph(final BaseChartGraph... graphs) {
        if (graphs == null || graphs.length == 0) return;

        for (BaseChartGraph graph : graphs) {
            graph.isEnable = !graph.isEnable;
        }

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(enablingWithAlphaAnimation ? FADE_ANIMATION_DURATION : FADE_ANIMATION_DURATION / 2);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (BaseChartGraph graph : graphs) {
                    if (graph.isEnable) {
                        graph.alpha = (float) animation.getAnimatedValue();
                    } else {
                        graph.alpha = 1 - (float) animation.getAnimatedValue();
                    }
                }
                graphAlphaChanged();
                chartListener.updateInfoView();
                invalidate();

            }
        });
        valueAnimator.start();

        if (!enablingWithAlphaAnimation) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    adjustYAxis();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    adjustYAxis();
                }
            });
        } else {
            adjustYAxis();
        }

        graphEnablingChanged();
    }

    protected void graphAlphaChanged() {}
    protected void graphEnablingChanged() {}
    protected void rangeChanged() {}

    public void setStart(int start) {
        if (start >= end - 2) return;
        if (start == this.start) return;

        float toScale = availableChartWidth / (xCoordByIndex(end - 1) - xCoordByIndex(start));
        this.start = start;

        rangeChanged();

        startScaleAnimation(toScale, true);
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

        if (end == this.end) return;

        float toScale = availableChartWidth / (xCoordByIndex(end - 1) - xCoordByIndex(start));
        this.end = end;

        rangeChanged();

        startScaleAnimation(toScale, false);
    }

    public void setStartEnd(int start, int end) {
        if (end > xPoints.length) return;
        if (start >= end - 1) return;

        if (start == this.start && end == this.end) return;

        float fromCoordStart = xCoordByIndex(this.start);
        float toCoordStart = xCoordByIndex(start);

        chartMatrix.postTranslate(fromCoordStart - toCoordStart, 0);
        chartMatrix2.postTranslate(fromCoordStart - toCoordStart, 0);
        visibleStart = xIndexByCoord(0);
        visibleEnd = xIndexByCoord(getWidth()) + 1;


        this.start = start;
        this.end = end;

        xAxis.adjustXAxis();
        adjustYAxis();

        rangeChanged();

        invalidate();
    }

    private void startScaleAnimation(float toScale, final boolean isStart) {
        chartMatrix.postScale(toScale, 1, isStart ? availableChartWidth : 0f, 0f);
        chartMatrix2.postScale(toScale, 1, isStart ? availableChartWidth : 0f, 0f);
        visibleStart = xIndexByCoord(0);
        visibleEnd = xIndexByCoord(getWidth()) + 1;
        xAxis.adjustXAxis();
        adjustYAxis();
        invalidate();
    }

    public void setData(ChartData data) {
        this.data = data;
        if (getWidth() > 0 && getHeight() > 0) {
            initGraphs();
        }
    }

    public void yAxisAdjusted() {
        chartListener.updateInfoView();
        invalidate();
    }

    public void zoom(final float x, final boolean isOpening) {
        this.isOpening = isOpening;
        isGone = false;
        final float[] prev = new float[1];
        float scale = isZoomed ? 20f : 0.01f;
        float fromScale = isOpening ? scale : 1;
        float toScale = isOpening ? 1 : scale;
        prev[0] = fromScale;
        if (isZoomed && !isOpening) {
            lastZoomedX = x;
        } else if (!isZoomed && isOpening){
            chartMatrix.postScale(scale, 1, availableChartWidth / 2, 0);
            chartMatrix2.postScale(scale, 1, availableChartWidth / 2, 0);
        }
        zoomAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        zoomAnimator.setDuration(ZOOM_ANIMATION_DURATION);
        zoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float value = (float) animation.getAnimatedValue();
                for (BaseChartGraph graph : graphs) {
                    graph.alpha = isOpening ? fraction : 1 - fraction;
                }
                float scale = value / prev[0];
                prev[0] = value;
                float newX = x;
                if (isZoomed) {
                    if (isOpening) newX = lastZoomedX;
                } else {
                    newX = availableChartWidth / 2;
                }
                chartMatrix.postScale(scale, 1, newX, 0);
                chartMatrix2.postScale(scale, 1, newX, 0);
                invalidate();

            }
        });
        zoomAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isGone = !isOpening;
                invalidate();
            }
        });
        zoomAnimator.start();
    }

    public void initTheme() {
        if (yAxis1 != null) yAxis1.updateTheme();
        if (yAxis2 != null) yAxis2.updateTheme();
        if (xAxis != null) xAxis.updateTheme();
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

    float yCoordByPoint(float y) {
        tempPoint[1] = y;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[1];
    }

    float yCoordByValue2(float y) {
        tempPoint[1] = yAxis2.maxValue - y;
        chartMatrix2.mapPoints(tempPoint);
        return tempPoint[1];
    }

    interface ChartListener {
        void updateInfoView();
    }

    static abstract class  AbsChartListenr implements ChartListener {
        @Override
        public void updateInfoView() {

        }
    }
}
