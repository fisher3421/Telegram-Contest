package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.text.TextPaint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import static com.molodkin.telegramcharts.BaseChart.FADE_ANIMATION_DURATION;

class XAxis {

    private final BaseChart chart;

    private ArrayList<XAxisPoint> pointToHideAnimated = new ArrayList<>();
    private final ArrayList<XAxisPoint> xAxisPoints = new ArrayList<>();

    private float xAxisTextHeight;

    private int xTextMargin;
    private int xAxisHalfOfTextWidth;

    private int xAxisTextWidth;
    private int xAxisTextWidthWithMargins;

    private TextPaint axisTextPaint = new TextPaint();

    private Date tempDate = new Date();
    private DateFormat xAxisDateFormat;

    XAxis(BaseChart baseChart) {
        this.chart = baseChart;

        int axesTextSize = Utils.spToPx(chart, 12);
        xTextMargin = Utils.dpToPx(chart, 4);
        xAxisHalfOfTextWidth = Utils.dpToPx(chart, 27);
        xAxisTextWidth = xAxisHalfOfTextWidth * 2;


        xAxisTextWidthWithMargins = xAxisTextWidth + 2 * Utils.dpToPx(chart, 4);

        axisTextPaint.setTextSize(axesTextSize);
        axisTextPaint.setAntiAlias(true);

        xAxisTextHeight = Utils.getFontHeight(axisTextPaint);

        xAxisDateFormat = new SimpleDateFormat("MMM d", Utils.getLocale(chart.getContext()));
    }

    void init(float scaleX) {
        int endX = (int) ((chart.availableChartWidth) / scaleX);
        int stepXAxis = Math.round(xAxisTextWidthWithMargins / scaleX);

        while (endX > 0) {
            xAxisPoints.add(0, buildXPoint(endX));
            endX-= stepXAxis;
        }
    }

    void updateTheme() {
        axisTextPaint.setColor(Utils.getColor(chart.getContext(), Utils.AXIS_TEXT_COLOR));
    }

    void adjustXAxis() {
        final ArrayList<XAxisPoint> pointsToShow = new ArrayList<>();
        final ArrayList<XAxisPoint> pointsToHide = new ArrayList<>();
        XAxisPoint first = xAxisPoints.get(0);
        XAxisPoint second = xAxisPoints.get(1);
        int currentStepX = second.x - first.x;

        float currentDistance = chart.xCoordByIndex(second.x) - chart.xCoordByIndex(first.x);

        if (currentDistance > xAxisTextWidthWithMargins) {
            int numberToAdd = (int) (currentDistance / xAxisTextWidthWithMargins) - 1;
            currentStepX = currentStepX / (numberToAdd + 1);
            for (int i = 0; i < xAxisPoints.size() - numberToAdd; i+= numberToAdd + 1) {
                for (int j = 1; j <= numberToAdd; j++) {
                    int newX = xAxisPoints.get(i).x + currentStepX * j;
                    if (newX >= chart.xPoints.length) break;
                    XAxisPoint point = buildXPointTransparent(newX);
                    xAxisPoints.add(i + j, point);
                    pointsToShow.add(point);
                }
            }
        } else if (currentDistance < xAxisTextWidth ) {
            int numberToRemove = (int) (xAxisTextWidth / currentDistance);
            ListIterator<XAxisPoint> iterator = xAxisPoints.listIterator(xAxisPoints.size());
            iterator.previous();
            while (iterator.hasPrevious()) {
                for (int i = 0; i < numberToRemove; i++) {
                    if (iterator.hasPrevious()){
                        pointsToHide.add(iterator.previous());
                        iterator.remove();
                    } else break;
                }
                if (iterator.hasPrevious()) iterator.previous();
                else break;
            }
        }

        first = xAxisPoints.get(0);
        second = xAxisPoints.get(1);
        currentStepX = second.x - first.x;


        int leftPointX = first.x - currentStepX;
        while (leftPointX >= 0) {
            if (isXTextVisible(leftPointX)) {
                XAxisPoint point = buildXPoint(leftPointX);
                xAxisPoints.add(0, point);
            } else break;
            leftPointX -= currentStepX;
        }

        int rightPointX = xAxisPoints.get(xAxisPoints.size() - 1).x + currentStepX;
        while (rightPointX < chart.xPoints.length) {
            if (isXTextVisible(rightPointX)) {
                XAxisPoint point = buildXPoint(rightPointX);
                xAxisPoints.add(point);
            } else break;
            rightPointX += currentStepX;
        }

        //remove left points
        Iterator<XAxisPoint> iterator = xAxisPoints.iterator();
        while (iterator.hasNext()) {
            XAxisPoint point = iterator.next();
            if (isXTextGone(point)) iterator.remove();
            else break;
        }

        //remove right points
        ListIterator<XAxisPoint> reverse = xAxisPoints.listIterator(xAxisPoints.size());
        while (reverse.hasPrevious()) {
            XAxisPoint point = reverse.previous();
            if (isXTextGone(point)) reverse.remove();
            else break;
        }

        if (pointsToShow.size() > 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 255);
            valueAnimator.setDuration(FADE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (XAxisPoint point : pointsToShow) {
                        point.alpha = (int) animation.getAnimatedValue();
                    }
                    chart.invalidate();

                }
            });
            valueAnimator.start();
        }

        if (pointsToHide.size() > 0) {
            pointToHideAnimated = new ArrayList<>(pointsToHide);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(255, 0);
            valueAnimator.setDuration(FADE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (XAxisPoint point : pointToHideAnimated) {
                        point.alpha = (int) animation.getAnimatedValue();
                    }
                    chart.invalidate();

                }
            });
            valueAnimator.start();
        }
    }

    private XAxisPoint buildXPoint(int x) {
        return buildXPoint(x, 255);
    }

    private XAxisPoint buildXPointTransparent(int x) {
        return buildXPoint(x, 0);
    }

    private XAxisPoint buildXPoint(int x, int alpha) {
        long millsec = chart.xPoints[x];
        tempDate.setTime(millsec);
        String dateSring = xAxisDateFormat.format(tempDate);
        return new XAxisPoint(x, dateSring, alpha, axisTextPaint.measureText(dateSring));
    }

    private boolean isXTextGone(XAxisPoint point) {
        float xCoord = chart.xCoordByIndex(point.x) + chart.sideMargin;
        return !(xCoord + point.width / 2 > 0) || !(xCoord - point.width / 2 < chart.getWidth());
    }

    private boolean isXTextVisible(int x) {
        float xCoord = chart.xCoordByIndex(x) + chart.sideMargin;
        return xCoord + xAxisHalfOfTextWidth > 0 && xCoord - xAxisHalfOfTextWidth < chart.getWidth();
    }

    void draw(Canvas canvas) {
        if (xAxisPoints.size() == 0) return;

        canvas.save();

        canvas.translate(0, chart.availableChartHeight + xTextMargin + xAxisTextHeight);

        for (XAxisPoint point : xAxisPoints) {
            float xCoord = chart.xCoordByIndex(point.x);
            axisTextPaint.setAlpha(point.alpha);
            canvas.drawText(point.date, xCoord - point.width / 2f, 0, axisTextPaint);
        }

        Iterator<XAxisPoint> iterator = pointToHideAnimated.iterator();

        while (iterator.hasNext()) {
            XAxisPoint next = iterator.next();
            if (next.alpha == 0) iterator.remove();
        }

        for (XAxisPoint point : pointToHideAnimated) {
            float xCoord = chart.xCoordByIndex(point.x);
            axisTextPaint.setAlpha(point.alpha);
            canvas.drawText(point.date, xCoord - point.width / 2f, 0, axisTextPaint);
        }

        canvas.restore();

    }
}
