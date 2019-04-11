package com.molodkin.telegramcharts;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;


@SuppressLint("ViewConstructor")
class StackPercentageInfoView extends BaseInfoView {

    private float verticalLineTopMargin;

    @SuppressWarnings("FieldCanBeLocal")
    private int verticalLineWidth = Utils.dpToPx(getContext(), 1);


    StackPercentageInfoView(Context c, BaseChart chartView) {
        super(c, chartView);

        verticalLinePaint.setStyle(Paint.Style.STROKE);
        verticalLinePaint.setStrokeWidth(verticalLineWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        verticalLineTopMargin = chartView.availableChartHeight * 1f / 5;
        windowTopMargin = verticalLineTopMargin;
    }

    @Override
    protected void initTheme() {
        super.initTheme();
        circleFillPaint.setColor(Utils.getColor(getContext(), Utils.INFO_VIEW_CIRCLE_COLOR));
        verticalLinePaint.setColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));
    }

    @Override
    protected void onActionMove(float x) {
        int newXIndex = chartView.xIndexByCoord(x);
        if (newXIndex != xIndex) {
            measureWindow(newXIndex);
            xCoord = chartView.xCoordByIndex(newXIndex);
            xIndex = newXIndex;
            invalidate();
        }
    }

    @Override
    protected void drawContent(Canvas canvas) {
        canvas.drawLine(xCoord, verticalLineTopMargin, xCoord, getHeight(), verticalLinePaint);
    }
}
