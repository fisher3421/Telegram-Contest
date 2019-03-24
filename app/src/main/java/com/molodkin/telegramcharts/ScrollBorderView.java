package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

class ScrollBorderView extends View {

    private final int scrollWindowMinWidth = Utils.dpToPx(this, 40);
    private final int scrollTouchBorderPadding = Utils.dpToPx(this, 10);
    private final int scrollBorderTopBottomWidth = Utils.dpToPx(this, 2);
    private final int scrollBorderLeftRightWidth = Utils.dpToPx(this, 5);
    private final LineChartView lineChartView;

    private int scrollWindowMinWidthInSteps;

    private final Paint scrollCoverPaint = new Paint();
    private final Paint scrollBorderPaint = new Paint();

    private boolean isScrollLeftBorderGrabbed = false;
    private boolean isScrollRightBorderGrabbed = false;
    private boolean isScrollWindowGrabbed = false;

    private int prevScrollXPoint = 0;

    ScrollBorderView(Context context, LineChartView lineChartView) {
        super(context);
        this.lineChartView = lineChartView;

        scrollCoverPaint.setStyle(Paint.Style.FILL);

        scrollBorderPaint.setStyle(Paint.Style.FILL);
        scrollBorderPaint.setColor(Utils.getColor(context, R.color.scroll_border));
        initTheme();
    }

    private void initTheme() {
        scrollCoverPaint.setColor(Utils.getColor(getContext(), Utils.SCROLL_COVER_COLOR));
    }

    void cancelMoving() {
        isScrollLeftBorderGrabbed = false;
        isScrollRightBorderGrabbed = false;
        isScrollWindowGrabbed = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scrollWindowMinWidthInSteps = Math.round(scrollWindowMinWidth / lineChartView.stepX);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleScrollTouch(event);

        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            cancelMoving();
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        } else {
            return false;
        }
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
        lineChartView.invalidate();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        float left = lineChartView.start * lineChartView.stepX;
        float right = (lineChartView.end - 1) * lineChartView.stepX;

        if (lineChartView.start != 0) {
            canvas.drawRect(0, 0, left, getHeight(), scrollCoverPaint);
        }

        if (lineChartView.end != lineChartView.xPoints.length) {
            canvas.drawRect(right, 0, lineChartView.getWidth(), getHeight(), scrollCoverPaint);
        }

        //draw left right borders
        canvas.drawRect(left, 0, left + scrollBorderLeftRightWidth, getHeight(), scrollBorderPaint);
        canvas.drawRect(right - scrollBorderLeftRightWidth, 0, right, getHeight(), scrollBorderPaint);

        //draw top bottom borders
        canvas.drawRect(left + scrollBorderLeftRightWidth, 0, right - scrollBorderLeftRightWidth, scrollBorderTopBottomWidth, scrollBorderPaint);
        canvas.drawRect(left + scrollBorderLeftRightWidth, getHeight() - scrollBorderTopBottomWidth, right - scrollBorderLeftRightWidth, getHeight(), scrollBorderPaint);
    }

    public void updateTheme() {
        initTheme();
        invalidate();
    }
}
