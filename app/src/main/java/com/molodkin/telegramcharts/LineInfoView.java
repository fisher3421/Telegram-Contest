package com.molodkin.telegramcharts;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;

import static com.molodkin.telegramcharts.Utils.log;

@SuppressLint("ViewConstructor")
class LineInfoView extends BaseInfoView {

    private final ArrayList<Float> yCoords = new ArrayList<>();

    private int verticalLineTopMargin = Utils.dpToPx(getContext(), 10);

    private ValueAnimator finisMovementAnimator = ValueAnimator.ofFloat(0 , 0);
    private ValueAnimator.AnimatorUpdateListener finisMovementAnimatorUpdate = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            log("finisMovementAnimatorUpdate" + finisMovementAnimator.hashCode() + " time: " + finisMovementAnimator.getCurrentPlayTime());
            log("finisMovementAnimatorUpdate" + finisMovementAnimator.hashCode() + " fraction: " + finisMovementAnimator.getAnimatedFraction());
            updateXCoord(((float) animation.getAnimatedValue()));
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private int verticalLineWidth = Utils.dpToPx(getContext(), 1);
    @SuppressWarnings("FieldCanBeLocal")
    private int circleStrokeWidth = Utils.dpToPx(getContext(), 2);
    private int circleRadius = Utils.dpToPx(getContext(), 4);

    private AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            finisMovementAnimator = null;
        }
    };

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

            long currentPlayTime = 0;
            if (finisMovementAnimator != null) {
                currentPlayTime = finisMovementAnimator.getCurrentPlayTime();
                finisMovementAnimator.cancel();
            }
            finisMovementAnimator = ValueAnimator.ofFloat(from, to);
            finisMovementAnimator.setCurrentPlayTime(currentPlayTime);
            finisMovementAnimator.setInterpolator(new DecelerateInterpolator());
            finisMovementAnimator.setDuration(MOVE_ANIMATION);
            finisMovementAnimator.addUpdateListener(finisMovementAnimatorUpdate);
            finisMovementAnimator.addListener(listener);
            finisMovementAnimator.start();

            xIndex = newXIndex;
        }
    }

    private void updateXCoord(float xCoord) {
        this.xCoord = xCoord;
        measurePoints(xCoord);
        invalidate();
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
