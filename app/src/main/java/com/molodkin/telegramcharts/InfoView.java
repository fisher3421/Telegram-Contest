package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.view.MotionEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoView extends ContexHolder {

    private final int dateTextSize = Utils.spToPx(context, 12);
    private final int valueTextSize = Utils.spToPx(context, 16);
    private final int nameTextSize = Utils.spToPx(context, 10);

    private float dateTextHeight;
    private float valueTextHeight;
    private float nameTextHeight;

    private final int topBottomPadding = Utils.dpToPx(context, 8);
    private final int leftRightPadding = Utils.dpToPx(context, 16);
    private final int leftRightDataMargin = Utils.dpToPx(context, 24);
    private final int dateValueMargin = Utils.dpToPx(context, 16);
    private final int valueNameMargin = Utils.dpToPx(context, 8);

    private final int verticalLineWindowLeftMargin = Utils.dpToPx(context, 24);

    private int verticalLineWidth = Utils.dpToPx(context, 1);
    private int verticalLineTopMargin = Utils.dpToPx(context, 10);

    private int circleStrokeWidth = Utils.dpToPx(context, 2);
    private int circleRadius = Utils.dpToPx(context, 4);

    private final Rect backgroundSize = new Rect();
    private final Rect backgroundPadding = new Rect();

    private NinePatchDrawable background;

    private final Paint backgroundPaint = new Paint();
    private final TextPaint dateTextPaint = new TextPaint();
    private final TextPaint valueTextPaint = new TextPaint();
    private final TextPaint nameTextPaint = new TextPaint();

    public final Paint circleFillPaint = new Paint();
    public final Paint circleStrokePaint = new Paint();

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

    private float [] tempPoint = new float[2];

    private boolean isMoving;

    public InfoView(Context c) {
        super(c);
        dateFormat = new SimpleDateFormat("EEE, MMM d", Utils.getLocale(c));

        background = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.bg_info, context.getTheme());
        background.getPadding(backgroundPadding);

        verticalLinePaint.setColor(Utils.getColor(context, R.color.gray));
        verticalLinePaint.setStyle(Paint.Style.STROKE);
        verticalLinePaint.setStrokeWidth(verticalLineWidth);

        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setAntiAlias(true);
        circleFillPaint.setColor(Utils.getColor(context, R.color.white));

        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setAntiAlias(true);
        circleStrokePaint.setStrokeWidth(circleStrokeWidth);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Utils.getColor(c, R.color.white));
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setShadowLayer(40, 40, 40, 0);

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

    public void cancelMoving() {
        isMoving = false;
    }

    public void handleTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoving = true;
                break;

        }
    }

    public void measure(long date, int x, ChartGraph [] graphs) {
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
                textColors[i] = graph.paint.getColor();
                float valueWidth = nameTextPaint.measureText(valueText);
                leftValues[i] = valuesWidth;
                valuesWidth += Math.max(nameWidth, valueWidth);
                if (i + 1 < graphs.length && graphs[i + 1].isEnable) valuesWidth += leftRightDataMargin;
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

    public void draw(Canvas canvas, float chartHeight, Matrix matrix, float stepX, float stepY) {
        if (!isMoving) return;

        tempPoint[0] = x * stepX;

        matrix.mapPoints(tempPoint);
        float xCoord = tempPoint[0];
        canvas.drawLine(xCoord, verticalLineTopMargin, xCoord, chartHeight, verticalLinePaint);

        for (int i = 0; i < graphCount; i++) {
            String textValue = textValues[i];
            if (textValue.length() > 0) {
                tempPoint[1] = chartHeight - graphValues[i] * stepY;
                matrix.mapPoints(tempPoint);

                int color = textColors[i];
                circleStrokePaint.setColor(color);
                canvas.drawCircle(xCoord, tempPoint[1], circleRadius, circleFillPaint);
                canvas.drawCircle(xCoord, tempPoint[1], circleRadius, circleStrokePaint);
            }
        }

        canvas.save();

        float windowLeftMargin = xCoord - verticalLineWindowLeftMargin;

        if (windowLeftMargin < 0) {
            windowLeftMargin = 0;
        } else if (windowLeftMargin + width >= canvas.getWidth()) {
            windowLeftMargin = canvas.getWidth() - width;
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

}
