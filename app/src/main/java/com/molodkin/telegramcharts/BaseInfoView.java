package com.molodkin.telegramcharts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

abstract class BaseInfoView extends View {

    protected final static long MOVE_ANIMATION = 200L;

    private final int dateTextSize = Utils.spToPx(getContext(), 12);
    private final int valueTextSize = Utils.spToPx(getContext(), 12);
    private final int nameTextSize = Utils.spToPx(getContext(), 12);

    private float dateTextHeight;
    private float nameTextHeight;

    private final int topBottomPadding = Utils.dpToPx(getContext(), 8);
    private final int leftRightPadding = Utils.dpToPx(getContext(), 16);
    private final int rowMargin = Utils.dpToPx(getContext(), 8);
    private final int windowLineMargin = Utils.dpToPx(getContext(), 8);

    private final Rect backgroundSize = new Rect();
    private final Rect backgroundPadding = new Rect();

    private NinePatchDrawable background;

    protected final Paint circleFillPaint = new Paint();
    protected final Paint circleStrokePaint = new Paint();
    protected final Paint verticalLinePaint = new Paint();

    private final TextPaint dateTextPaint = new TextPaint();
    private final TextPaint valueTextPaint = new TextPaint();
    private final TextPaint nameTextPaint = new TextPaint();

    private DateFormat dateFormat;

    private final Date tempDate = new Date();

    private final float left;
    private final int windowWidth = Utils.spToPx(getContext(), 160);
    private float windowHeight;
    private float contentWidth;

    private final float dateTextY;
    private String dateText;

    private final ArrayList<Float> leftValues = new ArrayList<>();
    private final ArrayList<Float> topValues = new ArrayList<>();
    private final ArrayList<String> textValues = new ArrayList<>();
    private final ArrayList<String> textNames = new ArrayList<>();
    protected final ArrayList<Integer> textColors = new ArrayList<>();

    protected float maxYCoord;

    private float windowTopMargin = 0f;

    private boolean isMoving;

    protected final BaseChart chartView;

    protected float xCoord = 0f;
    protected int xIndex = -1;

    private float minX = 0;
    private float maxX = 0;



    BaseInfoView(Context c, BaseChart chartView) {
        super(c);
        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Utils.getLocale(c));

        this.chartView = chartView;



        dateTextPaint.setTextSize(dateTextSize);
        dateTextPaint.setAntiAlias(true);
        dateTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        dateTextHeight = Utils.getFontHeight(dateTextPaint);

        valueTextPaint.setTextSize(valueTextSize);
        valueTextPaint.setAntiAlias(true);
        valueTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        nameTextPaint.setTextSize(nameTextSize);
        nameTextPaint.setAntiAlias(true);
        nameTextHeight = Utils.getFontHeight(nameTextPaint);

        left = backgroundPadding.left + leftRightPadding;
        float top = backgroundPadding.top + topBottomPadding;

        dateTextY = top + dateTextHeight;

        contentWidth = windowWidth - leftRightPadding * 2 - backgroundPadding.left - backgroundPadding.right;

        initTheme();

        background.getPadding(backgroundPadding);
    }

    protected void initTheme() {
        background = (NinePatchDrawable) getContext().getResources().getDrawable(Utils.getResId(Utils.INFO_VIEW_BACKGROUND), getContext().getTheme());
        dateTextPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        nameTextPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        minX = chartView.sideMargin;
        maxX = getWidth() - chartView.sideMargin;
    }

    float downX = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        if (x > maxX) {
            x = maxX;
        }

        if (x < minX) {
            x = minX;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoving = true;
                downX = event.getX();

                xIndex = chartView.xIndexByCoord(x);
                xCoord = chartView.xCoordByIndex(xIndex);

                measureWindow(xIndex);

                onActionDown(x);

                invalidate();

                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(x);

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                windowTopMargin = 0;
                isMoving = false;
                invalidate();
                break;
        }

        if (Math.abs(downX - event.getX()) > 50) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return isMoving;
    }

    protected void onActionDown(float x) {
    }

    protected void onActionMove(float x) {
    }

    void measureWindow(int newXIndex) {
        long date = chartView.xPoints[newXIndex];
        tempDate.setTime(date);

        dateText = dateFormat.format(tempDate);

        float top = dateTextY + dateTextHeight + rowMargin;

        textNames.clear();
        textValues.clear();
        textColors.clear();
        topValues.clear();
        leftValues.clear();

        for (int i = 0; i < chartView.graphs.length; i++) {
            BaseChartGraph graph = chartView.graphs[i];
            if (graph.isEnable) {
                textNames.add(graph.name);

                String valueText = String.valueOf(graph.values[newXIndex]);
                textValues.add(valueText);
                textColors.add(graph.linePaint.getColor());
                float valueWidth = valueTextPaint.measureText(valueText);
                leftValues.add(contentWidth - valueWidth);
                topValues.add(top);
                top += nameTextHeight;
                if (i + 1 < chartView.graphs.length && chartView.graphs[i + 1].isEnable) {
                    top += rowMargin;
                }
            }
        }

        windowHeight = top + backgroundPadding.bottom;

        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = windowWidth;
        backgroundSize.bottom = (int) windowHeight;

        background.setBounds(backgroundSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isMoving || textValues.isEmpty()) return;

        drawContent(canvas);

        canvas.save();

        float windowLeftMargin;

        if (maxYCoord < windowHeight) {
            windowTopMargin = 0;
            if (xCoord > getWidth() / 2) {
                windowLeftMargin = xCoord - windowLineMargin - windowWidth;
            } else {
                windowLeftMargin = xCoord + windowLineMargin;
            }
        } else {
            windowTopMargin = 0;
            windowLeftMargin = xCoord - windowWidth / 2f;

            if (windowLeftMargin < 0) {
                windowLeftMargin = 0;
            } else if (windowLeftMargin + windowWidth >= getWidth()) {
                windowLeftMargin = getWidth() - windowWidth;
            }
        }



        canvas.translate(0 , windowTopMargin);

        canvas.translate(windowLeftMargin, 0);

        background.draw(canvas);

        canvas.translate(left, 0);

        canvas.drawText(dateText, 0, dateTextY, dateTextPaint);

        for (int i = 0; i < textValues.size(); i++) {
            canvas.drawText(textNames.get(i), 0, topValues.get(i), nameTextPaint);

            String textValue = textValues.get(i);
            int color = textColors.get(i);
            valueTextPaint.setColor(color);

            canvas.drawText(textValue, leftValues.get(i), topValues.get(i), valueTextPaint);

        }

        canvas.restore();
    }

    protected void drawContent(Canvas canvas){
    }

    void updateTheme() {
        initTheme();
        invalidate();
    }
}
