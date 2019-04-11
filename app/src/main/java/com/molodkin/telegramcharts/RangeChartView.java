package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

import static com.molodkin.telegramcharts.BaseChart.SCALE_ANIMATION_DURATION;

@SuppressLint("ViewConstructor")
public class RangeChartView extends View {

    private final Matrix scrollMatrix = new Matrix();
    private final Matrix scrollMatrix2 = new Matrix();

    private final BaseChart chartView;

    private int maxYValueTemp;

    private float scaleY;



    private float chartsTopMargin = Utils.dpToPx(this, 4);

    float yScale = 1f;
    float translateY;
    boolean addTopMargin = true;

    RangeChartView(Context context, BaseChart lineChartView) {
        super(context);
        this.chartView = lineChartView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float availableHeight = getHeight();
        if (addTopMargin)  availableHeight -= chartsTopMargin;

        maxYValueTemp = chartView.yAxis1.maxValue;
        scaleY = availableHeight / maxYValueTemp;

        scrollMatrix.set(chartView.chartMatrix);
        scrollMatrix.postScale(1, yScale * availableHeight / chartView.availableChartHeight);

        if (translateY != 0) {
            float [] p = new float[2];
            p[1] = translateY;
            scrollMatrix.mapPoints(p);
            scrollMatrix.postTranslate(0, -p[1]);
        }



        if (chartView.yAxis2 != null) {
            scrollMatrix2.set(chartView.chartMatrix2);
            scrollMatrix2.postScale(1, availableHeight / chartView.availableChartHeight);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (addTopMargin) canvas.translate(0, chartsTopMargin);
        for (int i = chartView.graphs.length - 1; i >= 0; i--) {
            BaseChartGraph graph = chartView.graphs[i];
            if (graph.alpha > 0) {
                if (chartView.yAxis2 != null && i == 1) {
                    graph.drawScroll(canvas, scrollMatrix2);
                } else {
                    graph.drawScroll(canvas, scrollMatrix);
                }

            }
        }
    }

    private int getMaxYValue() {
        int value = chartView.yAxis1.getMaxValueFullRange();
        if (value == -1) {
            return maxYValueTemp;
        } else {
            return value;
        }
    }

    void adjustYAxis() {
        if (chartView.yAxis2 != null) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
            valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();

                }
            });
            valueAnimator.start();
            return;
        }

        int newTempMaxYValue = getMaxYValue();

        int maxYValue = chartView.yAxis1.maxValue;

        float toScale = scaleY * maxYValue / newTempMaxYValue;

        float fromScale = scaleY * maxYValue / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        Utils.log("adjustYAxis_scroll_fromScale: " + fromScale);
        Utils.log("adjustYAxis_scroll_toScale: " + toScale);

        final float [] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
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
