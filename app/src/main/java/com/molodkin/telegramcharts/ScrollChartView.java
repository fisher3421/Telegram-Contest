package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class ScrollChartView extends View {

    private final Matrix scrollMatrix = new Matrix();

    private final LineChartView chartView;

    private int maxYValueTemp;

    private float scaleCoeff;

    private float chartsTopMargin = Utils.dpToPx(this, 4);

    ScrollChartView(Context context, LineChartView lineChartView) {
        super(context);
        this.chartView = lineChartView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scaleCoeff = (getHeight() - chartsTopMargin) / chartView.availableChartHeight;

        scrollMatrix.postScale(1, scaleCoeff, 0, 0);

        maxYValueTemp = chartView.maxYValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(0, chartsTopMargin);
        for (ChartGraph graph : chartView.graphs) {
            if (graph.linePaint.getAlpha() > 0) graph.drawScroll(canvas, scrollMatrix);
        }
    }

    private int getMaxYValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(chartView.graphs.length);
        for (ChartGraph graph : chartView.graphs) {
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

        float toScale = scaleCoeff * chartView.maxYValue / newTempMaxYValue;

        float fromScale = scaleCoeff * chartView.maxYValue / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        chartView.log("adjustYAxis_scroll_fromScale: " + fromScale);
        chartView.log("adjustYAxis_scroll_toScale: " + toScale);

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
