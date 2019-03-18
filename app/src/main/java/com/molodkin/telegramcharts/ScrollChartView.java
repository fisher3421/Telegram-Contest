package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Collections;

class ScrollChartView extends ContexHolder {

    private final Matrix scrollMatrix = new Matrix();

    final int scrollHeight = Utils.dpToPx(context, 40);
    private final int scrollWindowMinWidth = Utils.dpToPx(context, 40);
    private final int scrollTouchBorderPadding = Utils.dpToPx(context, 10);
    private final int scrollBorderTopBottomWidth = Utils.dpToPx(context, 2);
    private final int scrollBorderLeftRightWidth = Utils.dpToPx(context, 5);
    private final LineChart lineChart;

    private int scrollWindowMinWidthInSteps;

    private final Paint dummyPaint = new Paint();
    private final Paint scrollCoverPaint = new Paint();
    private final Paint scrollBorderPaint = new Paint();

    private boolean isScrollLeftBorderGrabbed = false;
    private boolean isScrollRightBorderGrabbed = false;
    private boolean isScrollWindowGrabbed = false;

    private int prevScrollXPoint = 0;

    private int maxYValueTemp;

    private float scaleCoeff;

    private Canvas canvas;
    private Bitmap bitmap;

    ScrollChartView(Context context, LineChart lineChart) {
        super(context);
        this.lineChart = lineChart;

        scrollCoverPaint.setStyle(Paint.Style.FILL);
        scrollCoverPaint.setColor(Utils.getColor(context, R.color.scroll_cover));

        scrollBorderPaint.setStyle(Paint.Style.FILL);
        scrollBorderPaint.setColor(Utils.getColor(context, R.color.scroll_border));
    }

    void cancelMoving() {
        isScrollLeftBorderGrabbed = false;
        isScrollRightBorderGrabbed = false;
        isScrollWindowGrabbed = false;
    }

    boolean isMoving(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_MOVE &&
                (isScrollLeftBorderGrabbed || isScrollRightBorderGrabbed || isScrollWindowGrabbed);
    }

    void sizeChanged() {
        scrollWindowMinWidthInSteps = Math.round(scrollWindowMinWidth / lineChart.stepX);

        scaleCoeff = scrollHeight / lineChart.availableChartHeight;

        scrollMatrix.postScale(1, scaleCoeff, 0, 0);

        maxYValueTemp = lineChart.maxYValue;

        bitmap = Bitmap.createBitmap(lineChart.getWidth(), scrollHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        redraw();
    }

    void handleScrollTouch(MotionEvent event) {
        float x = event.getX();

        int start = lineChart.start;
        int end = lineChart.end;
        float stepX = lineChart.stepX;

        float left = start * stepX;
        float right = (end - 1) * stepX;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (Math.abs(left + scrollBorderLeftRightWidth / 2f - x) < scrollTouchBorderPadding) {
                    isScrollLeftBorderGrabbed = true;
                } else if (Math.abs(right - scrollBorderLeftRightWidth / 2f - x) < scrollTouchBorderPadding) {
                    isScrollRightBorderGrabbed = true;
                } else if (x > left && x < right) {
                    isScrollWindowGrabbed = true;
                    prevScrollXPoint = Math.round(x / stepX);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int newPoint = Math.round(x / stepX);

                int scrollDistanceInSteps = newPoint - prevScrollXPoint;

                prevScrollXPoint = newPoint;

                if (isScrollLeftBorderGrabbed) {
                    if (x < scrollBorderLeftRightWidth) {
                        lineChart.setStart(0);
                    } else if (newPoint > end - scrollWindowMinWidthInSteps - 1) {
                        lineChart.setStart(end - scrollWindowMinWidthInSteps - 1);
                    } else {
                        lineChart.setStart(newPoint);
                    }
                } else if (isScrollRightBorderGrabbed) {
                    if (x + scrollBorderLeftRightWidth > lineChart.getWidth()) {
                        lineChart.setEnd(lineChart.xPoints.length);
                    } else if (newPoint < start + scrollWindowMinWidthInSteps) {
                        lineChart.setEnd(start + scrollWindowMinWidthInSteps);
                    } else {
                        lineChart.setEnd(newPoint);
                    }
                } else if (isScrollWindowGrabbed) {
                    int range = end - start;
                    if (start + scrollDistanceInSteps <= 0) {
                        lineChart.setStartEnd(0, range);
                    } else if (end + scrollDistanceInSteps >= lineChart.xPoints.length) {
                        lineChart.setStartEnd(lineChart.xPoints.length - range, lineChart.xPoints.length);
                    } else {
                        lineChart.setStartEnd(lineChart.start + scrollDistanceInSteps, lineChart.end + scrollDistanceInSteps);
                    }
                }

                break;
            }
            default:
                lineChart.adjustYAxis();
        }
        redraw();
        lineChart.invalidate();
    }

    private void redraw() {
        bitmap.eraseColor(Color.TRANSPARENT);

        for (ChartGraph graph : lineChart.graphs) {
            graph.drawScroll(canvas, scrollMatrix);
        }

        float left = lineChart.start * lineChart.stepX;
        float right = (lineChart.end - 1) * lineChart.stepX;

        if (lineChart.start != 0) {
            canvas.drawRect(0, 0, left, scrollHeight, scrollCoverPaint);
        }

        if (lineChart.end != lineChart.xPoints.length) {
            canvas.drawRect(right, 0, lineChart.getWidth(), scrollHeight, scrollCoverPaint);
        }

        //draw left right borders
        canvas.drawRect(left, 0, left + scrollBorderLeftRightWidth, scrollHeight, scrollBorderPaint);
        canvas.drawRect(right - scrollBorderLeftRightWidth, 0, right, scrollHeight, scrollBorderPaint);

        //draw top bottom borders
        canvas.drawRect(left + scrollBorderLeftRightWidth, 0, right - scrollBorderLeftRightWidth, scrollBorderTopBottomWidth, scrollBorderPaint);
        canvas.drawRect(left + scrollBorderLeftRightWidth, scrollHeight - scrollBorderTopBottomWidth, right - scrollBorderLeftRightWidth, scrollHeight, scrollBorderPaint);
    }

    void draw(Canvas canvas) {
        if (bitmap == null) return;

        canvas.save();

        canvas.translate(0, lineChart.availableChartHeight + lineChart.xAxisHeight);

        canvas.drawBitmap(bitmap, 0, 0, dummyPaint);

        canvas.restore();
    }

    private int getMaxYValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(lineChart.graphs.length);
        for (ChartGraph graph : lineChart.graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(0, lineChart.xPoints.length));
        }

        return Collections.max(maxValues);
    }

    public void adjustYAxis() {
        int newTempMaxYValue = getMaxYValue();

        float toScale = scaleCoeff * lineChart.maxYValue / newTempMaxYValue;

        float fromScale = scaleCoeff * lineChart.maxYValue / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        lineChart.log("adjustYAxis_scroll_fromScale: " + fromScale);
        lineChart.log("adjustYAxis_scroll_toScale: " + toScale);

        final float [] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(LineChart.SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                scrollMatrix.postScale(1, value / prev[0], 0f, scrollHeight);
                prev[0] = value;
                redraw();
                lineChart.invalidate();

            }
        });
        valueAnimator.start();
    }
}
