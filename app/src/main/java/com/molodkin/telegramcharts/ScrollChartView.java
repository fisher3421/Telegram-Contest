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

    private final LineChartView lineChartView;

    private int maxYValueTemp;

    private float scaleCoeff;

    ScrollChartView(Context context, LineChartView lineChartView) {
        super(context);
        this.lineChartView = lineChartView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scaleCoeff = getHeight() / lineChartView.availableChartHeight;

        scrollMatrix.postScale(1, scaleCoeff, 0, 0);

        maxYValueTemp = lineChartView.maxYValue;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (ChartGraph graph : lineChartView.graphs) {
            if (graph.linePaint.getAlpha() > 0) graph.drawScroll(canvas, scrollMatrix);
        }
    }

    private int getMaxYValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(lineChartView.graphs.length);
        for (ChartGraph graph : lineChartView.graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(0, lineChartView.xPoints.length));
        }

        return Collections.max(maxValues);
    }

    void adjustYAxis() {
        int newTempMaxYValue = getMaxYValue();

        float toScale = scaleCoeff * lineChartView.maxYValue / newTempMaxYValue;

        float fromScale = scaleCoeff * lineChartView.maxYValue / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        lineChartView.log("adjustYAxis_scroll_fromScale: " + fromScale);
        lineChartView.log("adjustYAxis_scroll_toScale: " + toScale);

        final float [] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(LineChartView.SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                scrollMatrix.postScale(1, value / prev[0], 0f, getHeight());
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();
    }
}
