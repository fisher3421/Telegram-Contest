package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static com.molodkin.telegramcharts.Utils.log;

public final class LineChartView extends BaseChart {

    private XAxis xAxis = new XAxis(this);

    int sideMargin = Utils.getDim(this, R.dimen.margin20);
    int clipMargin = Utils.dpToPx(this, 1);
    int xAxisHeight = Utils.getDim(this, R.dimen.xAxisHeight);
    private int xAxisWidth = Utils.getDim(this, R.dimen.xAxisWidth);

    private int graphLineWidth = Utils.dpToPx(this, 2);

    private Paint axisPaint = new Paint();

    private ChartData data;

    public LineChartView(Context context) {
        super(context);
        init();
    }


    public void setData(ChartData data) {
        this.data = data;
        if (getWidth() > 0 && getHeight() > 0) {
            initGraphs();
        }
    }

    private void init() {
        initPaints();
        initGraphs();
    }

    private void initPaints() {
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        initTheme();
    }

    private void initTheme() {
        if (yAxis1 != null) yAxis1.updateTheme();
        if (yAxis2 != null) yAxis2.updateTheme();
        xAxis.updateTheme();
    }

    private void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;

        start = 0;
        end = data.x.length;

        drawStart = 0;
        drawEnd = data.x.length;

        yAdjustStart = 0;
        yAdjustEnd = data.x.length;

        graphs = new LineChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new LineChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), graphLineWidth, data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - xAxisHeight;
        availableChartWidth = (float) getWidth() - sideMargin * 2;

        float scaleX = availableChartWidth / (xPoints.length - 1);

        if (!secondY) {
            yAxis1 = new LineYAxis(this, chartMatrix);
            yAxis1.isHalfLine = false;
            yAxis1.init();
        } else {
            yAxis1 = new LineYAxis(this, chartMatrix);
            yAxis1.isHalfLine = true;
            yAxis1.init();

            yAxis2 = new LineYAxis(this, chartMatrix2);
            yAxis2.isHalfLine = true;
            yAxis2.isRight = true;
            yAxis2.init();
        }

        chartMatrix.postScale(scaleX, 1, 0, 0);
        chartMatrix2.postScale(scaleX, 1, 0, 0);

        xAxis.init(scaleX);

        for (int i = 0; i < end - 1; i++) {
            for (int j = 0; j < graphs.length; j++) {
                LineChartGraph graph = (LineChartGraph) graphs[j];
                int k = i * 4;

                int maxYValue = yAxis2 != null && j == 1 ? yAxis2.maxValue : yAxis1.maxValue;
                float y1 = maxYValue - graph.values[i];

                graph.linePoints[k] = i;
                graph.linePoints[k + 1] = y1;
                graph.linePoints[k + 2] = (i + 1);
                graph.linePoints[k + 3] = maxYValue - graph.values[i + 1];
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initGraphs();
    }

    public void updateTheme() {
        initTheme();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        canvas.translate(sideMargin, 0);

        canvas.save();

        canvas.clipRect(-sideMargin, 0, availableChartWidth + sideMargin, availableChartHeight + clipMargin);

        drawPoints(canvas);

        if (yAxis1 != null) yAxis1.draw(canvas);
        if (yAxis2 != null) yAxis2.draw(canvas);

        canvas.restore();

        xAxis.draw(canvas);
    }

    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < graphs.length; i++) {
            BaseChartGraph graph = graphs[i];
            if (graph.linePaint.getAlpha() > 0) {
                if (secondY && i == 1) {
                    graph.draw(canvas, chartMatrix2, Math.max(drawStart, 0), Math.min(drawEnd, xPoints.length));
                } else {
                    graph.draw(canvas, chartMatrix, Math.max(drawStart, 0), Math.min(drawEnd, xPoints.length));
                }

            }
        }
    }

    @Override
    public void enableGraph(final int index, boolean enable) {
        graphs[index].isEnable = enable;

        int fromAlpha = enable ? 0 : 255;
        int toAlpha = enable ? 255 : 0;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(fromAlpha, toAlpha);
        valueAnimator.setDuration(FADE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                graphs[index].linePaint.setAlpha(value);
                graphs[index].scrollLinePaint.setAlpha(value);
                invalidate();

            }
        });
        valueAnimator.start();

        adjustYAxis();
    }

    @Override
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

    @Override
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

    @Override
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

}
