package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

class RangeBorderView extends View {

    private final int scrollWindowMinWidth = Utils.dpToPx(this, 40);
    private final int scrollTouchBorderPadding = Utils.dpToPx(this, 10);
    private final int scrollBorderTopBottomWidth = Utils.getDim(this, R.dimen.range_top_bottom_padding);
    private final int scrollBorderLeftRightWidth = Utils.dpToPx(this, 10);
    private final int lineWidth = Utils.dpToPx(this, 2);
    private final int lineHeight = Utils.dpToPx(this, 10);
    private final int roundCorner = Utils.getDim(this, R.dimen.range_round);

    private final BaseChart chartView;

    private final Path holePath = new Path();

    private int scrollWindowMinWidthInSteps;

    private final Paint scrollCoverPaint = new Paint();
    private final Paint scrollBorderPaint = new Paint();
    private final Paint linePaint = new Paint();

    private boolean isScrollLeftBorderGrabbed = false;
    private boolean isScrollRightBorderGrabbed = false;
    private boolean isScrollWindowGrabbed = false;

    private int prevScrollXPoint = 0;

    private float scaleX;

    private OnRangeChanged onRangeChanged;

    RangeBorderView(Context context, BaseChart chartView) {
        super(context);
        this.chartView = chartView;

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        scrollCoverPaint.setStyle(Paint.Style.FILL);

        scrollBorderPaint.setStyle(Paint.Style.FILL);
        initTheme();
    }

    private void initTheme() {
        scrollCoverPaint.setColor(Utils.getColor(getContext(), Utils.SCROLL_COVER_COLOR));
        scrollBorderPaint.setColor(Utils.getColor(getContext(), Utils.SCROLL_BORDER_COLOR));
    }

    public void setOnRangeChanged(OnRangeChanged onRangeChanged) {
        this.onRangeChanged = onRangeChanged;
    }

    void cancelMoving() {
        isScrollLeftBorderGrabbed = false;
        isScrollRightBorderGrabbed = false;
        isScrollWindowGrabbed = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scaleX = getWidth() * 1f / (chartView.xPoints.length - 1);
        scrollWindowMinWidthInSteps = Math.round(scrollWindowMinWidth / scaleX);
        notifyListener();
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

        int start = chartView.start;
        int end = chartView.end;

        float left = start * scaleX;
        float right = (end - 1) * scaleX;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (Math.abs(left + scrollBorderLeftRightWidth / 2f - x) < scrollTouchBorderPadding) {
                    isScrollLeftBorderGrabbed = true;
                } else if (Math.abs(right - scrollBorderLeftRightWidth / 2f - x) < scrollTouchBorderPadding) {
                    isScrollRightBorderGrabbed = true;
                } else if (x > left && x < right) {
                    isScrollWindowGrabbed = true;
                    prevScrollXPoint = Math.round(x / scaleX);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int newPoint = Math.round(x / scaleX);

                int scrollDistanceInSteps = newPoint - prevScrollXPoint;

                prevScrollXPoint = newPoint;

                if (isScrollLeftBorderGrabbed) {
                    if (x < scrollBorderLeftRightWidth) {
                        chartView.setStart(0);
                    } else if (newPoint > end - scrollWindowMinWidthInSteps - 1) {
                        chartView.setStart(end - scrollWindowMinWidthInSteps - 1);
                    } else {
                        chartView.setStart(newPoint);
                    }
                } else if (isScrollRightBorderGrabbed) {
                    if (x + scrollBorderLeftRightWidth > chartView.getWidth()) {
                        chartView.setEnd(chartView.xPoints.length);
                    } else if (newPoint < start + scrollWindowMinWidthInSteps) {
                        chartView.setEnd(start + scrollWindowMinWidthInSteps);
                    } else {
                        chartView.setEnd(newPoint);
                    }
                } else if (isScrollWindowGrabbed) {
                    int range = end - start;
                    if (start + scrollDistanceInSteps <= 0) {
                        chartView.setStartEnd(0, range);
                    } else if (end + scrollDistanceInSteps >= chartView.xPoints.length) {
                        chartView.setStartEnd(chartView.xPoints.length - range, chartView.xPoints.length);
                    } else {
                        chartView.setStartEnd(start + scrollDistanceInSteps, end + scrollDistanceInSteps);
                    }
                }
                notifyListener();

                break;
            }
            default:
                chartView.adjustYAxis();
        }
        chartView.invalidate();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        float left = chartView.start * scaleX;
        float right = (chartView.end - 1) * scaleX;
        holePath.reset();
        holePath.addRect(
                left + scrollBorderLeftRightWidth,
                scrollBorderTopBottomWidth,
                right - scrollBorderLeftRightWidth,
                getHeight() - scrollBorderTopBottomWidth,
                Path.Direction.CW
        );

        canvas.clipPath(holePath, Region.Op.DIFFERENCE);

        canvas.drawRoundRect(
                0,
                scrollBorderTopBottomWidth,
                getWidth(),
                getHeight() - scrollBorderTopBottomWidth,
                roundCorner, roundCorner, scrollCoverPaint);

        canvas.drawRoundRect(left, 0, right, getHeight(), roundCorner, roundCorner, scrollBorderPaint);

        float lineTop = (getHeight() - lineHeight) / 2f;
        float line1Left = left + scrollBorderLeftRightWidth / 2f;
        float line2Left = right - scrollBorderLeftRightWidth / 2f;
        canvas.drawLine(line1Left, lineTop, line1Left, lineTop + lineHeight, linePaint);
        canvas.drawLine(line2Left, lineTop, line2Left, lineTop + lineHeight, linePaint);
    }

    public void updateTheme() {
        initTheme();
        invalidate();
    }

    public void notifyListener() {
        if (onRangeChanged != null) {
            onRangeChanged.onChanged(chartView.start, chartView.end);
        }
    }

    public interface OnRangeChanged {
        void onChanged(int start, int end);
    }
}
