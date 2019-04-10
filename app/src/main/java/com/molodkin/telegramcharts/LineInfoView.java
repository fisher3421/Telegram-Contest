package com.molodkin.telegramcharts;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
class LineInfoView extends BaseInfoView {

    private final ArrayList<Float> yCoords = new ArrayList<>();

    private int verticalLineTopMargin = Utils.dpToPx(getContext(), 10);

    private ValueAnimator finisMovementAnimator;
    private ValueAnimator.AnimatorUpdateListener finisMovementAnimatorUpdate = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            xCoord = ((float) animation.getAnimatedValue());
            measurePoints(xCoord);
            invalidate();
        }
    };

    private int verticalLineWidth = Utils.dpToPx(getContext(), 1);
    private int circleStrokeWidth = Utils.dpToPx(getContext(), 2);
    private int circleRadius = Utils.dpToPx(getContext(), 4);


    LineInfoView(Context c, BaseChart chartView) {
        super(c, chartView);

        verticalLinePaint.setStyle(Paint.Style.STROKE);
        verticalLinePaint.setStrokeWidth(verticalLineWidth);

        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setAntiAlias(true);

        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setAntiAlias(true);
        circleStrokePaint.setStrokeWidth(circleStrokeWidth);
    }

    @Override
    protected void initTheme() {
        super.initTheme();
        circleFillPaint.setColor(Utils.getColor(getContext(), Utils.INFO_VIEW_CIRCLE_COLOR));
        verticalLinePaint.setColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));
    }

    @Override
    protected void onActionDown(float x) {
        measurePoints(xCoord);
    }

    @Override
    protected void onActionMove(float x) {
        int newXIndex = chartView.xIndexByCoord(x);
        if (newXIndex != xIndex) {
            measureWindow(newXIndex);

            float from = xCoord;
            float to = chartView.xCoordByIndex(newXIndex);

            if (finisMovementAnimator != null) {
                finisMovementAnimator.cancel();
            }

            finisMovementAnimator = ValueAnimator.ofFloat(from, to);
            finisMovementAnimator.setDuration(MOVE_ANIMATION);
            finisMovementAnimator.addUpdateListener(finisMovementAnimatorUpdate);
            finisMovementAnimator.start();

            xIndex = newXIndex;
        }
    }

    void measurePoints(float newXCoord) {
        yCoords.clear();
        maxYCoord = Float.MAX_VALUE;

        float xValue  = chartView.xValueByCoord(newXCoord);

        for (int i = 0; i < chartView.graphs.length; i++) {
            BaseChartGraph graph = chartView.graphs[i];
            if (graph.isEnable) {

                int xBefore = (int) xValue;
                int y1 = graph.values[xBefore];
                int y2 = graph.values[(int) Math.ceil(xValue)];
                float fraction = xValue - xBefore;
                float y = y2 * fraction + y1 * (1 - fraction);

                float yCoord = chartView.secondY && i == 1 ? chartView.yCoordByValue2(y) : chartView.yCoordByValue(y);
                yCoords.add(yCoord);
                if (yCoord < maxYCoord) {
                    maxYCoord = yCoord;
                }
            }
        }
    }

    @Override
    protected void drawContent(Canvas canvas) {
        canvas.drawLine(xCoord, verticalLineTopMargin, xCoord, getHeight(), verticalLinePaint);

        for (int i = 0; i < yCoords.size(); i++) {
            float yCoord = yCoords.get(i);

            int color = textColors.get(i);
            circleStrokePaint.setColor(color);
            canvas.drawCircle(xCoord, yCoord, circleRadius, circleFillPaint);
            canvas.drawCircle(xCoord, yCoord, circleRadius, circleStrokePaint);
        }
    }
}
