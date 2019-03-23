package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class InfoView extends View {

    private final int dateTextSize = Utils.spToPx(getContext(), 12);
    private final int valueTextSize = Utils.spToPx(getContext(), 16);
    private final int nameTextSize = Utils.spToPx(getContext(), 10);

    private float dateTextHeight;
    private float valueTextHeight;
    private float nameTextHeight;

    private final int topBottomPadding = Utils.dpToPx(getContext(), 8);
    private final int leftRightPadding = Utils.dpToPx(getContext(), 16);
    private final int leftRightDataMargin = Utils.dpToPx(getContext(), 24);
    private final int dateValueMargin = Utils.dpToPx(getContext(), 16);
    private final int valueNameMargin = Utils.dpToPx(getContext(), 8);

    private final int verticalLineWindowLeftMargin = Utils.dpToPx(getContext(), 24);

    private int verticalLineWidth = Utils.dpToPx(getContext(), 1);
    private int verticalLineTopMargin = Utils.dpToPx(getContext(), 10);

    private int circleStrokeWidth = Utils.dpToPx(getContext(), 2);
    private int circleRadius = Utils.dpToPx(getContext(), 4);

    private final Rect backgroundSize = new Rect();
    private final Rect backgroundPadding = new Rect();

    private NinePatchDrawable background;

    private final TextPaint dateTextPaint = new TextPaint();
    private final TextPaint valueTextPaint = new TextPaint();
    private final TextPaint nameTextPaint = new TextPaint();

    private final Paint circleFillPaint = new Paint();
    private final Paint circleStrokePaint = new Paint();

    private final Paint verticalLinePaint = new Paint();

    private DateFormat dateFormat;

    private final Date tempDate = new Date();

    private final float left;
    private final float top;
    private final float height;
    private float width;

    private final float dateTextY;
    private final float dateValueY;
    private final float dateNameY;
    private String dateText;

    private final float[] leftValues = new float[20];
    private final String[] textValues = new String[20];
    private final int[] graphValues = new int[20];
    private final String[] textNames = new String[20];
    private final int[] textColors = new int[20];
    private int x;
    private int graphCount;

    private boolean isMoving;

    private final LineChartView chartView;

    InfoView(Context c, LineChartView chartView) {
        super(c);
        dateFormat = new SimpleDateFormat("EEE, MMM d", Utils.getLocale(c));

        background = (NinePatchDrawable) c.getResources().getDrawable(R.drawable.bg_info, c.getTheme());
        this.chartView = chartView;
        background.getPadding(backgroundPadding);

        verticalLinePaint.setColor(Utils.getColor(c, R.color.axis_day));
        verticalLinePaint.setStyle(Paint.Style.STROKE);
        verticalLinePaint.setStrokeWidth(verticalLineWidth);

        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setAntiAlias(true);
        circleFillPaint.setColor(Utils.getColor(c, R.color.white));

        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setAntiAlias(true);
        circleStrokePaint.setStrokeWidth(circleStrokeWidth);

        dateTextPaint.setTextSize(dateTextSize);
        dateTextHeight = Utils.getFontHeight(dateTextPaint);

        valueTextPaint.setTextSize(valueTextSize);
        valueTextHeight = Utils.getFontHeight(valueTextPaint);

        nameTextPaint.setTextSize(nameTextSize);
        nameTextHeight = Utils.getFontHeight(nameTextPaint);

        left = backgroundPadding.left + leftRightPadding;
        top = backgroundPadding.top + topBottomPadding;

        dateTextY = top + dateTextHeight;
        dateValueY = dateTextY + dateValueMargin + valueTextHeight;
        dateNameY = dateValueY + valueNameMargin + nameTextHeight;

        height = dateNameY + topBottomPadding + backgroundPadding.bottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoving = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isMoving = false;
                break;
        }

        if (isMoving) {
            float x = event.getX();
            int chartLineXPoint = chartView.xIndexByCoord(x);
            measure(chartView.xPoints[chartLineXPoint], chartLineXPoint, chartView.graphs);
        }

        invalidate();
        return event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE;
    }

    void measure(long date, int x, ChartGraph[] graphs) {
        tempDate.setTime(date);

        dateText = dateFormat.format(tempDate);

        float dateTextWidth = dateTextPaint.measureText(dateText);

        float valuesWidth = 0;

        this.x = x;

        graphCount = graphs.length;

        for (int i = 0; i < graphs.length; i++) {
            ChartGraph graph = graphs[i];
            if (graph.isEnable) {
                textNames[i] = graph.name;
                float nameWidth = nameTextPaint.measureText(graph.name);
                graphValues[i] = graph.values[x];
                String valueText = String.valueOf(graph.values[x]);
                textValues[i] = valueText;
                textColors[i] = graph.linePaint.getColor();
                float valueWidth = valueTextPaint.measureText(valueText);
                leftValues[i] = valuesWidth;
                valuesWidth += Math.max(nameWidth, valueWidth);
                if (i + 1 < graphs.length && graphs[i + 1].isEnable)
                    valuesWidth += leftRightDataMargin;
            } else {
                textValues[i] = "";
            }
        }

        width = left + Math.max(dateTextWidth, valuesWidth) + backgroundPadding.right + leftRightPadding;

        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = (int) width;
        backgroundSize.bottom = (int) height;

        background.setBounds(backgroundSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isMoving) return;
        float xCoord = chartView.xCoordByIndex(x);

        canvas.drawLine(xCoord, verticalLineTopMargin, xCoord, getHeight(), verticalLinePaint);

        for (int i = 0; i < graphCount; i++) {
            String textValue = textValues[i];
            if (textValue.length() > 0) {
                float yCoord = chartView.yCoordByIndex(graphValues[i]);

                int color = textColors[i];
                circleStrokePaint.setColor(color);
                canvas.drawCircle(xCoord, yCoord, circleRadius, circleFillPaint);
                canvas.drawCircle(xCoord, yCoord, circleRadius, circleStrokePaint);
            }
        }

        canvas.save();

        float windowLeftMargin = xCoord - verticalLineWindowLeftMargin;

        if (windowLeftMargin < 0) {
            windowLeftMargin = 0;
        } else if (windowLeftMargin + width >= getWidth()) {
            windowLeftMargin = getWidth() - width;
        }

        canvas.translate(windowLeftMargin, 0);

        background.draw(canvas);

        canvas.translate(left, 0);

        canvas.drawText(dateText, 0, dateTextY, dateTextPaint);

        for (int i = 0; i < graphCount; i++) {
            String textValue = textValues[i];
            if (textValue.length() > 0) {
                int color = textColors[i];
                valueTextPaint.setColor(color);
                nameTextPaint.setColor(color);
                canvas.drawText(textValue, leftValues[i], dateValueY, valueTextPaint);
                canvas.drawText(textNames[i], leftValues[i], dateNameY, nameTextPaint);
            }
        }

        canvas.restore();

    }

    void setDayMode(boolean dayMode) {
        if (dayMode) {
            circleFillPaint.setColor(Utils.getColor(getContext(), R.color.chart_background_day));
            background = (NinePatchDrawable) getContext().getResources().getDrawable(R.drawable.bg_info, getContext().getTheme());
        } else {
            circleFillPaint.setColor(Utils.getColor(getContext(), R.color.chart_background_night));
            background = (NinePatchDrawable) getContext().getResources().getDrawable(R.drawable.bg_info_dark, getContext().getTheme());
        }
    }
}
