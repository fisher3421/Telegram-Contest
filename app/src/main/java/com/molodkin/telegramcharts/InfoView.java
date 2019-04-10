package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
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

class InfoView extends View {

    private final static long MOVE_ANIMATION = 200L;

    private final int dateTextSize = Utils.spToPx(getContext(), 12);
    private final int valueTextSize = Utils.spToPx(getContext(), 12);
    private final int nameTextSize = Utils.spToPx(getContext(), 12);

    private float dateTextHeight;
    private float nameTextHeight;

    private final int topBottomPadding = Utils.dpToPx(getContext(), 8);
    private final int leftRightPadding = Utils.dpToPx(getContext(), 16);
    private final int rowMargin = Utils.dpToPx(getContext(), 8);
    private final int windowLineMargin = Utils.dpToPx(getContext(), 8);

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
    private final int windowWidth = Utils.spToPx(getContext(), 160);
    private float windowHeight;
    private float contentWidth;

    private final float dateTextY;
    private String dateText;

    private final ArrayList<Float> leftValues = new ArrayList<>();
    private final ArrayList<Float> topValues = new ArrayList<>();
    private final ArrayList<Float> valuesWidths = new ArrayList<>();
    private final ArrayList<String> textValues = new ArrayList<>();
    private final ArrayList<String> textNames = new ArrayList<>();
    private final ArrayList<Integer> textColors = new ArrayList<>();
    private final ArrayList<Float> yCoords = new ArrayList<>();
    private float maxYCoord;

    private float windowTopMargin = 0f;

    private boolean isMoving;

    private final BaseChart chartView;

    private float xCoord = 0f;
    private int xIndex = -1;

    private float minX = 0;
    private float maxX = 0;

    private ValueAnimator finisMovementAnimator;
    private ValueAnimator.AnimatorUpdateListener finisMovementAnimatorUpdate = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            xCoord = ((float) animation.getAnimatedValue());
            measurePoints(xCoord);
            invalidate();
        }
    };

    InfoView(Context c, LineChartView chartView) {
        super(c);
        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy", Utils.getLocale(c));

        this.chartView = chartView;

        verticalLinePaint.setStyle(Paint.Style.STROKE);
        verticalLinePaint.setStrokeWidth(verticalLineWidth);

        circleFillPaint.setStyle(Paint.Style.FILL);
        circleFillPaint.setAntiAlias(true);

        circleStrokePaint.setStyle(Paint.Style.STROKE);
        circleStrokePaint.setAntiAlias(true);
        circleStrokePaint.setStrokeWidth(circleStrokeWidth);

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

    private void initTheme() {
        circleFillPaint.setColor(Utils.getColor(getContext(), Utils.INFO_VIEW_CIRCLE_COLOR));
        verticalLinePaint.setColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));
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

                measurePoints(xCoord);
                measureWindow(xIndex);

                invalidate();

                break;
            case MotionEvent.ACTION_MOVE:
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

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                windowTopMargin = 0;
                isMoving = false;
                invalidate();
                break;
        }



//        if (isMoving) {
//            measure(newCoord);
//            invalidate();
//        }

        if (Math.abs(downX - event.getX()) > 50) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return isMoving;
    }

    void measureWindow(int newXIndex) {
        long date = chartView.xPoints[newXIndex];
        tempDate.setTime(date);

        dateText = dateFormat.format(tempDate);

//        float dateTextWidth = dateTextPaint.measureText(dateText);

//        float valuesWidth = 0;
        float top = dateTextY + dateTextHeight + rowMargin;

        textNames.clear();
        textValues.clear();
        textColors.clear();
        topValues.clear();
        leftValues.clear();
        valuesWidths.clear();

        for (int i = 0; i < chartView.graphs.length; i++) {
            BaseChartGraph graph = chartView.graphs[i];
            if (graph.isEnable) {
                textNames.add(graph.name);
//                float nameWidth = nameTextPaint.measureText(graph.name);

                String valueText = String.valueOf(graph.values[newXIndex]);
                textValues.add(valueText);
                textColors.add(graph.linePaint.getColor());
                float valueWidth = valueTextPaint.measureText(valueText);
                leftValues.add(contentWidth - valueWidth);
                topValues.add(top);
                top += nameTextHeight;
//                valuesWidth += Math.max(nameWidth, valueWidth);
                if (i + 1 < chartView.graphs.length && chartView.graphs[i + 1].isEnable) {
                    top += rowMargin;
                }
            }
        }

        windowHeight = top + backgroundPadding.bottom;

//        width = left + Math.max(dateTextWidth, valuesWidth) + backgroundPadding.right + leftRightPadding;

        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = windowWidth;
        backgroundSize.bottom = (int) windowHeight;

        background.setBounds(backgroundSize);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isMoving || textValues.isEmpty()) return;

        canvas.drawLine(xCoord, verticalLineTopMargin, xCoord, getHeight(), verticalLinePaint);

        for (int i = 0; i < textValues.size(); i++) {
            float yCoord = yCoords.get(i);

            int color = textColors.get(i);
            circleStrokePaint.setColor(color);
            canvas.drawCircle(xCoord, yCoord, circleRadius, circleFillPaint);
            canvas.drawCircle(xCoord, yCoord, circleRadius, circleStrokePaint);
        }

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

    void updateTheme() {
        initTheme();
        invalidate();
    }
}
