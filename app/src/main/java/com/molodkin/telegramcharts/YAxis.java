package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.Collections;

import static com.molodkin.telegramcharts.BaseChart.SCALE_ANIMATION_DURATION;
import static com.molodkin.telegramcharts.Utils.log;

class YAxis {

    private final BaseChart chart;
    private final Matrix matrix;
    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();

    private int xTextMargin;
    private int axesTextSize;
    private int xAxisWidth;

    private static final float ROW_AXIS_ALPHA = 0.1f;

    private int [] availableYSteps = {5, 10, 20, 25, 40, 50, 100, 500, 1_000, 1_500, 2_000, 2_500, 3_000, 4_000, 5000, 10_000, 20_000, 30_000, 40_000, 50_000, 100_000, 200_000, 300_000, 400_000, 500_000, 1000_000};

    private int rowNumber = 6;
    private int [] rowYValues = new int[rowNumber];
    private int [] rowYValuesToHide = new int[rowNumber];

    private String [] rowYTextsValues = new String[rowNumber];
    private String [] rowYTextsValuesToHide = new String[rowNumber];

    private float [] rowYTextsValuesWidth = new float[rowNumber];
    private float [] rowYTextsValuesToHideWidth = new float[rowNumber];

    private int rowYValuesAlpha = 255;

    boolean isRight;
    boolean isHalfLine= false;

    private int maxYValueTemp = -1;
    int maxYValue;

    YAxis(BaseChart chart, Matrix matrix) {
        this.chart = chart;
        this.matrix = matrix;

        xTextMargin = Utils.dpToPx(chart, 4);
        axesTextSize = Utils.spToPx(chart, 12);
        xAxisWidth = Utils.getDim(chart, R.dimen.xAxisWidth);

    }

    void init() {
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        axisTextPaint.setTextSize(axesTextSize);
        axisTextPaint.setAntiAlias(true);

        adjustYAxis(true);

        updateTheme();
    }

    void draw(Canvas canvas) {
        canvas.save();

        drawLines(canvas, rowYValuesAlpha, rowYValues, rowYTextsValues, rowYTextsValuesWidth);

        if (rowYValuesAlpha < 255) {
            drawLines(canvas, 255 -rowYValuesAlpha, rowYValuesToHide, rowYTextsValuesToHide, rowYTextsValuesToHideWidth);
        }

        canvas.restore();
    }

    private void drawLines(Canvas canvas, int alpha, int [] rowYValues, String [] rowYTextsValues, float [] rowYTextsValuesWidth) {
        for (int i = 0; i < rowYValues.length; i++) {
            int y = rowYValues[i];
            canvas.save();
            canvas.translate(0, !isRight ? chart.yCoordByValue(y) : chart.yCoordByValue2(y));
            axisPaint.setAlpha((int) (alpha * ROW_AXIS_ALPHA));

            if (isHalfLine) {
                if (!isRight) {
                    canvas.drawLine(0f, 0f, chart.availableChartWidth / 2, 0f, axisPaint);
                } else {
                    canvas.drawLine(chart.availableChartWidth / 2, 0f, chart.availableChartWidth, 0f, axisPaint);
                }
            } else {
                canvas.drawLine(0, 0f, chart.availableChartWidth, 0f, axisPaint);
            }

            canvas.save();

            canvas.translate(0f, -xTextMargin);

            axisTextPaint.setAlpha(alpha);
            if (!isRight) {
                canvas.drawText(rowYTextsValues[i], 0, 0, axisTextPaint);
            } else {
                canvas.drawText(rowYTextsValues[i], chart.availableChartWidth - rowYTextsValuesWidth[i], 0, axisTextPaint);
            }

            canvas.restore();

            canvas.restore();
        }
    }

    void updateTheme() {
        axisPaint.setColor(Utils.getColor(chart.getContext(), Utils.AXIS_COLOR));
        if (!isHalfLine) {
            axisTextPaint.setColor(Utils.getColor(chart.getContext(), Utils.AXIS_TEXT_COLOR));
        } else {
            if (!isRight) {
                axisTextPaint.setColor(chart.graphs[0].color);
            } else {
                axisTextPaint.setColor(chart.graphs[1].color);
            }
        }

    }

    void adjustYAxis() {
        adjustYAxis(false);
    }

    private void adjustYAxis(boolean init) {
        int newTempMaxYValue = getAdjustedMaxYValue();
        if (init) {
            maxYValue = newTempMaxYValue;
        }

        if (newTempMaxYValue == this.maxYValueTemp) return;

        System.arraycopy(rowYValues, 0, rowYValuesToHide, 0, rowNumber);
        System.arraycopy(rowYTextsValues, 0, rowYTextsValuesToHide, 0, rowNumber);
        if (isRight) {
            System.arraycopy(rowYTextsValuesWidth, 0, rowYTextsValuesToHideWidth, 0, rowNumber);
        }

        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = (int) (i * newTempMaxYValue * 1f / rowNumber);
            rowYTextsValues[i] = String.valueOf(rowYValues[i]);
            if (isRight) {
                rowYTextsValuesWidth[i] = axisTextPaint.measureText(rowYTextsValues[i]);
            }
        }

        float toScale = this.maxYValue * 1f / newTempMaxYValue;

        float fromScale = this.maxYValue * 1f / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        if (init){
            matrix.postScale(1, chart.availableChartHeight / maxYValue, 0, 0);
            return;
        }

        log("adjustYAxis_fromScale: " + fromScale);
        log("adjustYAxis_toScale: " + toScale);

        final float[] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                matrix.postScale(1, value / prev[0], 0f, chart.availableChartHeight);
                prev[0] = value;
                chart.invalidate();
            }
        });
        valueAnimator.start();

        rowYValuesAlpha = 0;

        ValueAnimator valueAnimator2 = ValueAnimator.ofInt(0, 255);
        valueAnimator2.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rowYValuesAlpha = (int) animation.getAnimatedValue();
                chart.invalidate();

            }
        });
        valueAnimator2.start();
    }

    private int getAdjustedMaxYValue() {
        if (isHalfLine) {
            if (!isRight) {
                return getAdjustedMaxYValue(0);
            } else {
                return getAdjustedMaxYValue(1);
            }
        } else {
            return getAdjustedMaxYValueAll();
        }
    }

    private int getAdjustedMaxYValueAll() {
        int value = getMaxRangeValue();
        int tempStep = (int) Math.ceil(value * 1f / rowNumber);

        return findAvailableValue(tempStep);
    }

    private int getAdjustedMaxYValue(int graphIndex) {
        if (!chart.graphs[graphIndex].isEnable) {
            return graphIndex == 0 ? maxYValueTemp : maxYValueTemp;
        }
        int value = chart.graphs[graphIndex].getMax(chart.start, chart.end);
        int tempStep = (int) Math.ceil(value * 1f / rowNumber);

        return findAvailableValue(tempStep);
    }

    private int findAvailableValue(int value) {
        for (int i = 0; i < availableYSteps.length - 1; i++) {
            if (availableYSteps[i] < value && value <= availableYSteps[i + 1]) {
                value = rowNumber * availableYSteps[i + 1];
                break;
            }
        }

        return value;
    }

    private int getMaxRangeValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(chart.graphs.length);
        for (ChartGraph graph : chart.graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(chart.start, chart.end));
        }

        if (maxValues.size() == 0) {
            return maxYValueTemp;
        } else {
            return Collections.max(maxValues);
        }
    }

}
