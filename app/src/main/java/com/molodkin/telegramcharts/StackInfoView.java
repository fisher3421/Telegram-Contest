package com.molodkin.telegramcharts;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

@SuppressLint("ViewConstructor")
class StackInfoView extends BaseInfoView {

    private final Paint coverPaint = new Paint();

    StackInfoView(Context c, BaseChart chartView) {
        super(c, chartView);
        coverPaint.setColor(Color.WHITE);
        coverPaint.setAlpha((int) (255*0.5));
        coverPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onActionMove(float x) {
        super.onActionMove(x);
        xIndex = chartView.xIndexByCoord(x);
        xCoord = chartView.xCoordByIndex(xIndex);
        measureWindow(xIndex);
        invalidate();
    }

    @Override
    protected void drawContent(Canvas canvas) {
        float strokeWidth = chartView.graphs[0].paint.getStrokeWidth() / 2;
        canvas.drawRect(0, 0, xCoord - strokeWidth, chartView.availableChartHeight, coverPaint);
        canvas.drawRect(xCoord + strokeWidth, 0, getWidth(), chartView.availableChartHeight, coverPaint);
    }
}
