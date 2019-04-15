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
    void updateTheme() {
        super.updateTheme();
        if (Utils.isDayMode) {
            coverPaint.setColor(Color.WHITE);
            coverPaint.setAlpha((int) (255*0.5));
            coverPaint.setStyle(Paint.Style.FILL);
        } else {
            coverPaint.setColor(Color.parseColor("#242F3E"));
            coverPaint.setAlpha((int) (255*0.5));
            coverPaint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    protected void onActionMove(float x) {
        int newXIndex = chartView.xIndexByCoord(x);
        if (newXIndex != xIndex) {
            measureWindow(newXIndex);
            xCoord = chartView.xCoordByIndex(newXIndex);
            xIndex = newXIndex;
            preWindowLeftMargin = windowLeftMargin;
            calcWindowMargin();
            invalidate();
        }
    }

    @Override
    protected void drawContent(Canvas canvas) {
        float strokeWidth = chartView.graphs[0].paint.getStrokeWidth() / 2;
        canvas.drawRect(0, 0, xCoord - strokeWidth, chartView.availableChartHeight, coverPaint);
        canvas.drawRect(xCoord + strokeWidth, 0, getWidth(), chartView.availableChartHeight, coverPaint);
        StackChartView chartView = (StackChartView) this.chartView;
        canvas.drawRect(xCoord - strokeWidth, 0, xCoord + strokeWidth, chartView.yCoordByPoint(chartView.tops[xIndex]), coverPaint);
    }
}
