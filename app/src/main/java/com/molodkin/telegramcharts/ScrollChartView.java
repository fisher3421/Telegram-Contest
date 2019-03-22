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
    private final LineChartView lineChartView;

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

//    private Canvas canvas;
//    private Bitmap bitmap;

    ScrollChartView(Context context, LineChartView lineChartView) {
        super(context);
        this.lineChartView = lineChartView;

        scrollCoverPaint.setStyle(Paint.Style.FILL);
        scrollCoverPaint.setColor(Utils.getColor(context, R.color.scroll_cover_day));

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
        scrollWindowMinWidthInSteps = Math.round(scrollWindowMinWidth / lineChartView.stepX);

        scaleCoeff = scrollHeight / lineChartView.availableChartHeight;

        scrollMatrix.postScale(1, scaleCoeff, 0, 0);

        maxYValueTemp = lineChartView.maxYValue;

//        bitmap = Bitmap.createBitmap(lineChartView.getWidth(), scrollHeight, Bitmap.Config.ARGB_8888);
//        canvas = new Canvas(bitmap);
//        redraw();
    }

    void handleScrollTouch(MotionEvent event) {
        float x = event.getX();

        int start = lineChartView.start;
        int end = lineChartView.end;
        float stepX = lineChartView.stepX;

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
                        lineChartView.setStart(0);
                    } else if (newPoint > end - scrollWindowMinWidthInSteps - 1) {
                        lineChartView.setStart(end - scrollWindowMinWidthInSteps - 1);
                    } else {
                        lineChartView.setStart(newPoint);
                    }
                } else if (isScrollRightBorderGrabbed) {
                    if (x + scrollBorderLeftRightWidth > lineChartView.getWidth()) {
                        lineChartView.setEnd(lineChartView.xPoints.length);
                    } else if (newPoint < start + scrollWindowMinWidthInSteps) {
                        lineChartView.setEnd(start + scrollWindowMinWidthInSteps);
                    } else {
                        lineChartView.setEnd(newPoint);
                    }
                } else if (isScrollWindowGrabbed) {
                    int range = end - start;
                    if (start + scrollDistanceInSteps <= 0) {
                        lineChartView.setStartEnd(0, range);
                    } else if (end + scrollDistanceInSteps >= lineChartView.xPoints.length) {
                        lineChartView.setStartEnd(lineChartView.xPoints.length - range, lineChartView.xPoints.length);
                    } else {
                        lineChartView.setStartEnd(lineChartView.start + scrollDistanceInSteps, lineChartView.end + scrollDistanceInSteps);
                    }
                }

                break;
            }
            default:
                lineChartView.adjustYAxis();
        }
//        redraw();
        lineChartView.invalidate();
    }

//    private void redraw() {
//        bitmap.eraseColor(Color.TRANSPARENT);
//
//        for (ChartGraph graph : lineChartView.graphs) {
//            graph.drawScroll(canvas, scrollMatrix);
//        }
//
//        float left = lineChartView.start * lineChartView.stepX;
//        float right = (lineChartView.end - 1) * lineChartView.stepX;
//
//        if (lineChartView.start != 0) {
//            canvas.drawRect(0, 0, left, scrollHeight, scrollCoverPaint);
//        }
//
//        if (lineChartView.end != lineChartView.xPoints.length) {
//            canvas.drawRect(right, 0, lineChartView.getWidth(), scrollHeight, scrollCoverPaint);
//        }
//
//        //draw left right borders
//        canvas.drawRect(left, 0, left + scrollBorderLeftRightWidth, scrollHeight, scrollBorderPaint);
//        canvas.drawRect(right - scrollBorderLeftRightWidth, 0, right, scrollHeight, scrollBorderPaint);
//
//        //draw top bottom borders
//        canvas.drawRect(left + scrollBorderLeftRightWidth, 0, right - scrollBorderLeftRightWidth, scrollBorderTopBottomWidth, scrollBorderPaint);
//        canvas.drawRect(left + scrollBorderLeftRightWidth, scrollHeight - scrollBorderTopBottomWidth, right - scrollBorderLeftRightWidth, scrollHeight, scrollBorderPaint);
//    }

    void draw(Canvas canvas) {
//        if (bitmap == null) return;

        canvas.save();

        canvas.translate(0, lineChartView.availableChartHeight + lineChartView.xAxisHeight);

        for (ChartGraph graph : lineChartView.graphs) {
            if (graph.linePaint.getAlpha() > 0) graph.drawScroll(canvas, scrollMatrix);
        }

        float left = lineChartView.start * lineChartView.stepX;
        float right = (lineChartView.end - 1) * lineChartView.stepX;

        if (lineChartView.start != 0) {
            canvas.drawRect(0, 0, left, scrollHeight, scrollCoverPaint);
        }

        if (lineChartView.end != lineChartView.xPoints.length) {
            canvas.drawRect(right, 0, lineChartView.getWidth(), scrollHeight, scrollCoverPaint);
        }

        //draw left right borders
        canvas.drawRect(left, 0, left + scrollBorderLeftRightWidth, scrollHeight, scrollBorderPaint);
        canvas.drawRect(right - scrollBorderLeftRightWidth, 0, right, scrollHeight, scrollBorderPaint);

        //draw top bottom borders
        canvas.drawRect(left + scrollBorderLeftRightWidth, 0, right - scrollBorderLeftRightWidth, scrollBorderTopBottomWidth, scrollBorderPaint);
        canvas.drawRect(left + scrollBorderLeftRightWidth, scrollHeight - scrollBorderTopBottomWidth, right - scrollBorderLeftRightWidth, scrollHeight, scrollBorderPaint);

        canvas.restore();
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
                scrollMatrix.postScale(1, value / prev[0], 0f, scrollHeight);
                prev[0] = value;
//                redraw();
                lineChartView.invalidate();

            }
        });
        valueAnimator.start();
    }
}
