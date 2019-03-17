package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;

class ScrollChartView extends ContexHolder {

    private final Matrix scrollMatrix = new Matrix();

    final int scrollHeight = Utils.dpToPx(context, 40);
    private final int scrollWindowMinWidth = Utils.dpToPx(context, 40);
    private final int scrollTouchBorderPadding = Utils.dpToPx(context, 10);
    private final int scrollBorderTopBottomWidth = Utils.dpToPx(context, 2);
    private final int scrollBorderLeftRightWidth = Utils.dpToPx(context, 5);
    private final LineChart lineChart;

    private int scrollWindowMinWidthInSteps;

    private final Paint scrollCoverPaint = new Paint();
    private final Paint scrollBorderPaint = new Paint();

    private boolean isScrollLeftBorderGrabbed = false;
    private boolean isScrollRightBorderGrabbed = false;
    private boolean isScrollWindowGrabbed = false;

    private int prevScrollXPoint = 0;

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
        scrollMatrix.postScale(1, scrollHeight / lineChart.availableChartHeight, 0, 0);
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
        lineChart.invalidate();
    }

    void draw(Canvas canvas) {
        canvas.save();

        canvas.translate(0, lineChart.availableChartHeight + lineChart.xAxisHeight);

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

        canvas.restore();
    }
}
