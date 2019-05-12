package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;

import java.text.DecimalFormat;

import static com.molodkin.telegramcharts.BaseChart.SCALE_ANIMATION_DURATION;
import static com.molodkin.telegramcharts.Utils.log;

abstract class BaseYAxis extends View {
    private static final float ROW_AXIS_ALPHA = 0.1f;
    private static String [] NUMBER_SUFFIXES = {"", "K", "M", "B", "T"};

    final BaseChart chart;
    private final Matrix matrix;
    private int xTextMargin = Utils.dpToPx(this, 4);
    private int axesTextSize = Utils.spToPx(this, 12);
    private int xAxisWidth = Utils.getDim(this, R.dimen.xAxisWidth);
    boolean isRight;
    boolean isHalfLine = false;
    int maxValue;
    private int range;
    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();
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
    boolean adjustValues = true;

    private static DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private ValueAnimator.AnimatorUpdateListener alphaAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            rowYValuesAlpha = (int) animation.getAnimatedValue();
            invalidate();
            chart.invalidate();

        }
    };
    private ValueAnimator alphaAnimator;

    public BaseYAxis(Context context, BaseChart chart, Matrix matrix) {
        super(context);
        this.chart = chart;
        this.matrix = matrix;

        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        axisTextPaint.setTextSize(axesTextSize);
        axisTextPaint.setAntiAlias(true);
    }

     void init() {
        adjustYAxis(true);
        updateTheme();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rowYTextsValues[0] == null) return;
        if (chart.zoomAnimator != null && chart.zoomAnimator.isRunning()) {
            drawZoom(canvas, chart.zoomAnimator.getAnimatedFraction(), chart.isZoomed, chart.isOpening);
        } else {
            drawInner(canvas);
        }
    }

    void drawInner(Canvas canvas) {
        canvas.save();

        drawLines(canvas, rowYValuesAlpha, rowYValues, rowYTextsValues, rowYTextsValuesWidth);

        if (rowYValuesAlpha < 255) {
            drawLines(canvas, 255 - rowYValuesAlpha, rowYValuesToHide, rowYTextsValuesToHide, rowYTextsValuesToHideWidth);
        }

        canvas.restore();
    }

    void drawZoom(Canvas canvas, float fraction, boolean big, boolean opening) {
        float availableChartHeight = chart.availableChartHeight;
        canvas.save();

        float yScale = opening ? fraction : 1 - fraction;
        canvas.scale(1, yScale, 0, big ? 0 : availableChartHeight);

        drawInner(canvas);
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
            if (chart.graphs == null) return;

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

    public void adjustYAxis(boolean init) {
        int dirtyMin = getMinValue();
        int dirtyMax = getMaxValue();
        int delta = dirtyMax - dirtyMin;

        int newTempMinYValue = dirtyMin;
        int newRange = delta;
        int newTempMaxYValue = dirtyMax;

        if (adjustValues) {
            int margin = (int) (chart.graphTopMargin / chart.availableChartHeight * delta);
            delta += margin;
            double e = Math.pow(10, Math.floor(Math.log10(delta)) - 1);
            double c = ((int) (delta / (e * rowNumber) + 1)) * e * rowNumber;

            newTempMinYValue = (int) ((int)(dirtyMin / e) * e);
            if (delta > c) {
                c = ((int)(delta / e * rowNumber) + 2) * e * rowNumber;
            }
            newRange = (int) c;
            newTempMaxYValue = newTempMinYValue + newRange;
        }



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
            rowYTextsValues[i] = formatValue(rowYValues[i]);
            if (isRight) {
                rowYTextsValuesWidth[i] = axisTextPaint.measureText(rowYTextsValues[i]);
            }
        }

        this.maxYValueTemp = newTempMaxYValue;
        this.minYValueTemp = newTempMinYValue;

        if (init){
            matrix.postScale(1, chart.availableChartHeight / range, 0, 0);
            return;
        }

        final float toScale = range * 1f / newRange;

//        log("adjustYAxis_fromScale: " + fromScale);
//        log("adjustYAxis_toScale: " + toScale);

        final float[] prev = new float[1];
        prev[0] = fromScale;

        long playTime = 0;

        if (scaleAnimator != null) {
            if (scaleAnimator.isRunning()) {
                playTime = scaleAnimator.getCurrentPlayTime();
            }
            scaleAnimator.cancel();
        }

        scaleAnimator = ValueAnimator.ofFloat(fromScale, toScale);
//        scaleAnimator.setCurrentPlayTime(playTime);
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
                invalidate();
                chart.yAxisAdjusted();
            }
        });

        scaleAnimator.start();

        if (alphaAnimator != null) {
            alphaAnimator.cancel();
        }

        alphaAnimator = ValueAnimator.ofInt(0, 255);
        alphaAnimator.setDuration(SCALE_ANIMATION_DURATION);
//        alphaAnimator.setCurrentPlayTime(playTime);
        alphaAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rowYValuesAlpha = (int) animation.getAnimatedValue();
                invalidate();
                chart.invalidate();

            }
        };
        alphaAnimator.addUpdateListener(alphaAnimatorUpdateListener);
        alphaAnimator.start();

        invalidate();
    }

    String formatValue(int value) {
        if (value > 1_000) {
            int s = (int) (Math.log10(value) / 3);
            double newValue = value / (Math.pow(10, 3 * s));
            return String.format("%s%s", decimalFormat.format(newValue), NUMBER_SUFFIXES[s]);
        }
        return String.valueOf(value);
    }

    abstract int getMaxValue();

    abstract int getMaxValueFullRange();

    abstract int getMinValue();
}
