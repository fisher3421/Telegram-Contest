package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class RangeChartView extends View {

    private final Matrix scrollMatrix = new Matrix();

    private final BaseChart chartView;

    private int maxYValueTemp;

    private float scaleY;

    private float chartsTopMargin = Utils.dpToPx(this, 4);

    RangeChartView(Context context, BaseChart lineChartView) {
        super(context);
        this.chartView = lineChartView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int maxYValue = chartView.yAxis1.maxYValue;

        float availableHeight = getHeight() - chartsTopMargin;
        scaleY = availableHeight / maxYValue;

        float scaleX = getWidth() * 1f / (chartView.xPoints.length - 1);

        scrollMatrix.setScale(scaleX, scaleY, 0, 0);

        maxYValueTemp = maxYValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(0, chartsTopMargin);
        for (ChartGraph graph : chartView.graphs) {
            if (graph.linePaint.getAlpha() > 0) graph.drawScroll(canvas, scrollMatrix);
        }
    }

    private int getMaxYValue() {
        ChartGraph[] graphs = chartView.graphs;
        ArrayList<Integer> maxValues = new ArrayList<>(graphs.length);
        for (ChartGraph graph : graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(0, chartView.xPoints.length));
        }

        if (maxValues.size() == 0) {
            return maxYValueTemp;
        } else {
            return Collections.max(maxValues);
        }
    }

    void adjustYAxis() {
        int newTempMaxYValue = getMaxYValue();

        int maxYValue = chartView.yAxis1.maxYValue;

        float toScale = scaleY * maxYValue / newTempMaxYValue;

        float fromScale = scaleY * maxYValue / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        Utils.log("adjustYAxis_scroll_fromScale: " + fromScale);
        Utils.log("adjustYAxis_scroll_toScale: " + toScale);

        final float [] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(LineChartView.SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                scrollMatrix.postScale(1, value / prev[0], 0f, getHeight() - chartsTopMargin);
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();
    }
}
