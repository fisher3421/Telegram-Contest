package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

class InfoView extends View {

    private final static long MOVE_DELAY = 100L;
    private final static long MOVE_ANIMATION = 100L;

    private final int dateTextSize = Utils.spToPx(getContext(), 12);
    private final int valueTextSize = Utils.spToPx(getContext(), 14);
    private final int nameTextSize = Utils.spToPx(getContext(), 10);

    private float dateTextHeight;
    private float valueTextHeight;
    private float nameTextHeight;

    private final int topBottomPadding = Utils.dpToPx(getContext(), 8);
    private final int leftRightPadding = Utils.dpToPx(getContext(), 16);
    private final int leftRightDataMargin = Utils.dpToPx(getContext(), 16);
    private final int dateValueMargin = Utils.dpToPx(getContext(), 8);
    private final int valueNameMargin = Utils.dpToPx(getContext(), 2);

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
    private final float height;
    private float width;

    private final float dateTextY;
    private final float dateValueY;
    private final float dateNameY;
    private String dateText;

    private final ArrayList<Float> leftValues = new ArrayList<>();
    private final ArrayList<String> textValues = new ArrayList<>();
    private final ArrayList<String> textNames = new ArrayList<>();
    private final ArrayList<Integer> textColors = new ArrayList<>();
    private final ArrayList<Float> yCoords = new ArrayList<>();
    private final ArrayList<Float> yCoordsSored = new ArrayList<>();

    private float windowTopMargin = 0f;

    private boolean isMoving;

    private final LineChartView chartView;

    private float xCoord = 0f;

    private float minX = 0;
    private float maxX = 0;

    private final Timer finisMovementTimer = new Timer();;
    private TimerTask timerTask;

    private ValueAnimator finisMovementAnimator;
    private ValueAnimator.AnimatorUpdateListener finisMovementAnimatorUpdate = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float newX = ((float) animation.getAnimatedValue());
            xCoord = newX;
            measure();
            invalidate();
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int x = chartView.xIndexByCoord(xCoord);
            float toXcoord = chartView.getXViewCoord(x);
            finisMovementAnimator = ValueAnimator.ofFloat(xCoord, toXcoord);
            finisMovementAnimator.addUpdateListener(finisMovementAnimatorUpdate);
            finisMovementAnimator.start();
            return false;
        }
    });

    InfoView(Context c, LineChartView chartView) {
        super(c);
        dateFormat = new SimpleDateFormat("EEE, MMM d", Utils.getLocale(c));

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
        dateTextHeight = Utils.getFontHeight(dateTextPaint);

        valueTextPaint.setTextSize(valueTextSize);
        valueTextPaint.setAntiAlias(true);
        valueTextHeight = Utils.getFontHeight(valueTextPaint);

        nameTextPaint.setTextSize(nameTextSize);
        nameTextPaint.setAntiAlias(true);
        nameTextHeight = Utils.getFontHeight(nameTextPaint);

        left = backgroundPadding.left + leftRightPadding;
        float top = backgroundPadding.top + topBottomPadding;

        dateTextY = top + dateTextHeight;
        dateValueY = dateTextY + dateValueMargin + valueTextHeight;
        dateNameY = dateValueY + valueNameMargin + nameTextHeight;

        height = dateNameY + topBottomPadding + backgroundPadding.bottom;

        initTheme();

        background.getPadding(backgroundPadding);
    }

    private void initTheme() {
        circleFillPaint.setColor(Utils.getColor(getContext(), Utils.INFO_VIEW_CIRCLE_COLOR));
        verticalLinePaint.setColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));
        background = (NinePatchDrawable) getContext().getResources().getDrawable(Utils.getResId(Utils.INFO_VIEW_BACKGROUND), getContext().getTheme());
        dateTextPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMoving = true;
                downX = event.getX();
                scheduleMovement();
                break;
            case MotionEvent.ACTION_MOVE:
                scheduleMovement();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelMovement();
                windowTopMargin = 0;
                isMoving = false;
                break;
        }

        xCoord = event.getX();

        if (xCoord > maxX) {
            xCoord = maxX;
        }

        if (xCoord < minX) {
            xCoord = minX;
        }

        if (isMoving) {
            measure();
        }

        invalidate();

        if (Math.abs(downX - event.getX()) > 50) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return isMoving;
    }

    private void cancelMovement() {
        if (finisMovementAnimator != null) finisMovementAnimator.cancel();
        if (timerTask != null )timerTask.cancel();
    }

    private void scheduleMovement() {
        cancelMovement();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };

        finisMovementTimer.schedule(timerTask, MOVE_DELAY);
    }

    void measure() {
        float xValue  = chartView.xValueByCoord(xCoord);
        int x = Math.round(xValue);
        long date = chartView.xPoints[x];
        tempDate.setTime(date);

        dateText = dateFormat.format(tempDate);

        float dateTextWidth = dateTextPaint.measureText(dateText);

        float valuesWidth = 0;

        textNames.clear();
        textValues.clear();
        textColors.clear();
        leftValues.clear();
        yCoords.clear();
        yCoordsSored.clear();

        for (int i = 0; i < chartView.graphs.length; i++) {
            ChartGraph graph = chartView.graphs[i];
            if (graph.isEnable) {
                textNames.add(graph.name);
                float nameWidth = nameTextPaint.measureText(graph.name);

                int xBefore = (int) xValue;
                int y1 = graph.values[xBefore];
                int y2 = graph.values[(int) Math.ceil(xValue)];
                float fraction = xValue - xBefore;
                float y = y2 * fraction + y1 * (1 - fraction);

                float yCoord = chartView.yCoordByValue(y);
                yCoords.add(yCoord);
                if (yCoordsSored.size() > 0) {
                    if (yCoordsSored.get(0) > yCoord) {
                        yCoordsSored.add(0, yCoord);
                    } else {
                        yCoordsSored.add(yCoord);
                    }
                } else {
                    yCoordsSored.add(yCoord);
                }

                String valueText = String.valueOf(graph.values[x]);
                textValues.add(valueText);
                textColors.add(graph.linePaint.getColor());
                float valueWidth = valueTextPaint.measureText(valueText);
                leftValues.add(valuesWidth);
                valuesWidth += Math.max(nameWidth, valueWidth);
                if (i + 1 < chartView.graphs.length && chartView.graphs[i + 1].isEnable)
                    valuesWidth += leftRightDataMargin;
            }
        }

        width = left + Math.max(dateTextWidth, valuesWidth) + backgroundPadding.right + leftRightPadding;

        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = (int) width;
        backgroundSize.bottom = (int) height;

        background.setBounds(backgroundSize);

        boolean isTopMarginValid = true;

        int circleDiameter = circleRadius * 2;

        for (int i = 0; i < yCoordsSored.size(); i++) {
            float y = yCoordsSored.get(i);

            if (y + circleDiameter > windowTopMargin && y - circleDiameter < windowTopMargin + height) {
                isTopMarginValid = false;
                break;
            }
        }

        if (!isTopMarginValid) {
            for (int i = 0; i < yCoordsSored.size(); i++) {
                float y = yCoordsSored.get(i);
                if (i == 0) {
                    if (y > height + circleDiameter) {
                        windowTopMargin = 0;
                        break;
                    }
                } else {
                    float yPre = yCoordsSored.get(i - 1);
                    if (Math.abs(y - yPre) > height + circleDiameter * 2) {
                        windowTopMargin = yPre + circleDiameter;
                        break;
                    } else if (i == yCoordsSored.size() - 1) {
                        if (y + circleDiameter + height < getHeight()) {
                            windowTopMargin = y + circleDiameter;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isMoving || textValues.isEmpty()) return;


        float startYVerticalLIne = Math.min(windowTopMargin + verticalLineTopMargin, yCoordsSored.get(0));

        canvas.drawLine(xCoord, startYVerticalLIne, xCoord, getHeight(), verticalLinePaint);

        for (int i = 0; i < textValues.size(); i++) {
            float yCoord = yCoords.get(i);

            int color = textColors.get(i);
            circleStrokePaint.setColor(color);
            canvas.drawCircle(xCoord, yCoord, circleRadius, circleFillPaint);
            canvas.drawCircle(xCoord, yCoord, circleRadius, circleStrokePaint);
        }

        canvas.save();

        float windowLeftMargin = xCoord - verticalLineWindowLeftMargin;

        if (windowLeftMargin < 0) {
            windowLeftMargin = 0;
        } else if (windowLeftMargin + width >= getWidth()) {
            windowLeftMargin = getWidth() - width;
        }

        canvas.translate(0 , windowTopMargin);

        canvas.translate(windowLeftMargin, 0);

        background.draw(canvas);

        canvas.translate(left, 0);

        canvas.drawText(dateText, 0, dateTextY, dateTextPaint);

        for (int i = 0; i < textValues.size(); i++) {
            String textValue = textValues.get(i);
            int color = textColors.get(i);
            valueTextPaint.setColor(color);
            nameTextPaint.setColor(color);
            canvas.drawText(textValue, leftValues.get(i), dateValueY, valueTextPaint);
            canvas.drawText(textNames.get(i), leftValues.get(i), dateNameY, nameTextPaint);
        }

        canvas.restore();
    }

    void updateTheme() {
        initTheme();
        invalidate();
    }
}
