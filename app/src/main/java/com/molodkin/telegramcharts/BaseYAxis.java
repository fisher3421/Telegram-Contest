package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextPaint;

import static com.molodkin.telegramcharts.BaseChart.SCALE_ANIMATION_DURATION;
import static com.molodkin.telegramcharts.Utils.log;

abstract class BaseYAxis {
    private static final float ROW_AXIS_ALPHA = 0.1f;
    final BaseChart chart;
    private final Matrix matrix;
    private int xTextMargin;
    private int axesTextSize;
    private int xAxisWidth;
    boolean isRight;
    boolean isHalfLine = false;
    int maxValue;
    private int range;
    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();
    private int [] availableYSteps = {5, 10, 20, 25, 40, 50, 100, 500, 1_000, 1_500, 2_000, 2_500, 3_000, 4_000, 5000, 10_000, 20_000, 30_000, 40_000, 50_000, 100_000, 200_000, 300_000, 400_000, 500_000, 1000_000};
    int rowNumber = 6;
    private int [] rowYValues = new int[rowNumber];
    private int [] rowYValuesToHide = new int[rowNumber];
    private String [] rowYTextsValues = new String[rowNumber];
    private String [] rowYTextsValuesToHide = new String[rowNumber];
    private float [] rowYTextsValuesWidth = new float[rowNumber];
    private float [] rowYTextsValuesToHideWidth = new float[rowNumber];
    private int rowYValuesAlpha = 255;
    int maxYValueTemp = -1;
    int minYValueTemp = -1;
    private ValueAnimator scaleAnimator;
    private float fromScale = 1f;

    BaseYAxis(BaseChart chart, Matrix matrix) {
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
        for (int i = 0; i < rowNumber; i++) {
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
        int dirtyMin = getMinValue();
        int dirtyMax = getMaxValue();

        int newTempMinYValue = findAvailableMinValue(dirtyMin);

        int tempStep = (int) Math.ceil((dirtyMax - newTempMinYValue) * 1f / rowNumber);

        int newRange = findAvailableValue(tempStep);

        int newTempMaxYValue = newTempMinYValue + newRange;

        if (init) {
            maxValue = newTempMaxYValue;
            range = newRange;
        }

        if (newTempMaxYValue == this.maxYValueTemp && newTempMinYValue == this.minYValueTemp) return;

        final boolean isMaxUpdated = newTempMaxYValue != this.maxYValueTemp;

        System.arraycopy(rowYValues, 0, rowYValuesToHide, 0, rowNumber);
        System.arraycopy(rowYTextsValues, 0, rowYTextsValuesToHide, 0, rowNumber);
        if (isRight) {
            System.arraycopy(rowYTextsValuesWidth, 0, rowYTextsValuesToHideWidth, 0, rowNumber);
        }

        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = newTempMinYValue + (int) (i * newRange * 1f / rowNumber);
            rowYTextsValues[i] = String.valueOf(rowYValues[i]);
            if (isRight) {
                rowYTextsValuesWidth[i] = axisTextPaint.measureText(rowYTextsValues[i]);
            }
        }

        final float toScale = range * 1f / newRange;

        this.maxYValueTemp = newTempMaxYValue;
        this.minYValueTemp = newTempMinYValue;

        if (init){
            matrix.postScale(1, chart.availableChartHeight / range, 0, 0);
            return;
        }

        log("adjustYAxis_fromScale: " + fromScale);
        log("adjustYAxis_toScale: " + toScale);

        final float[] prev = new float[1];
        prev[0] = fromScale;

        if (scaleAnimator != null) {
            scaleAnimator.cancel();
        }

        scaleAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        scaleAnimator.setDuration(SCALE_ANIMATION_DURATION);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float portion = animation.getAnimatedFraction();
                fromScale = value;

                if (isMaxUpdated) {
                    float minYValueTempCoord = isRight ? chart.yCoordByValue2(minYValueTemp) : chart.yCoordByValue(minYValueTemp);
                    if (minYValueTempCoord != chart.availableChartHeight) {
                        matrix.postTranslate(0, (chart.availableChartHeight - minYValueTempCoord) * portion);
                    }
                    matrix.postScale(1, value / prev[0], 0f, chart.availableChartHeight);
                } else {
                    float maxYValueTempCoord = isRight ? chart.yCoordByValue2(minYValueTemp) : chart.yCoordByValue(maxYValueTemp);
                    if (maxYValueTempCoord != 0) {
                        matrix.postTranslate(0, -maxYValueTempCoord * portion);
                    }
                    matrix.postScale(1, value / prev[0], 0f, 0);
                }

                prev[0] = value;
                chart.invalidate();
            }
        });

        scaleAnimator.start();

        rowYValuesAlpha = 0;

        ValueAnimator alphaAnimator = ValueAnimator.ofInt(0, 255);
        alphaAnimator.setDuration(SCALE_ANIMATION_DURATION);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rowYValuesAlpha = (int) animation.getAnimatedValue();
                chart.invalidate();

            }
        });
        alphaAnimator.start();
    }

    abstract int getMaxValue();

    abstract int getMaxValueFullRange();


    private int findAvailableValue(int value) {
        for (int i = 0; i < availableYSteps.length - 1; i++) {
            if (availableYSteps[i] < value && value <= availableYSteps[i + 1]) {
                value = rowNumber * availableYSteps[i + 1];
                break;
            }
        }

        return value;
    }

    private int findAvailableMinValue(int value) {
        if (value < 5) {
            return 0;
        } else {
            while (true) {
                if (value % 5 == 0) return value;
                value--;
            }
        }
    }

    abstract int getMinValue();
}
