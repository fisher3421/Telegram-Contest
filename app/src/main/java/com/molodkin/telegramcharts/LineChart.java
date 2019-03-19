package com.molodkin.telegramcharts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

//TODO: rtl support
public final class LineChart extends View {

    private static final boolean LOG_IS_ENABLED = true;
    private static final String LOG_TAG = "LineChart";

    static final long SCALE_ANIMATION_DURATION = 250L;
    static final long FADE_ANIMATION_DURATION = 125L;

    private enum UpdateXAxis {INIT, LEFT_IN, LEFT_OUT, RIGHT_IN, RIGHT_OUT, TRANSLATE_LEFT, TRANSLATE_RIGHT}

    ChartGraph[] graphs;

    long[] xPoints = ChartData.X;

    public final Matrix chartMatrix = new Matrix();

    private final float [] chartMatrixValues = new float[9];

    int start = 0;
    int end = xPoints.length;


    private int maxYValueTemp;
    int maxYValue;

    private int rowNumber = 6;
    private int [] rowYValues = new int[rowNumber];
    private int [] rowYValuesToHide = new int[rowNumber];
    private int rowYValuesAlpha = 255;
    private int columnNumber;

    private String[] yAxisTexts = new String[rowNumber];

    private final ArrayList<XAxisPoint> xAxisPoints = new ArrayList<>();

    int xAxisHeight = Utils.dpToPx(this, 33);
    private int xAxisWidth = Utils.dpToPx(this, 1);

    private float xAxisTextHeight;

    private int xAxisSideMargin = Utils.dpToPx(this, 20);

    private int graphLineWidth = Utils.dpToPx(this, 2);

    float availableChartHeight;

    private int axesTextSize = Utils.spToPx(this, 14);
    private int xTextMargin = Utils.dpToPx(this, 4);
    private int xAxisHalfOfMaxTextWidth = Utils.dpToPx(this, 27);

    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();

    private float rowHeight;
    float stepX;

    private Date tempDate = new Date();
    private DateFormat xAxisDateFormat;

    private float xTextsStep;
    private float[] tempPoint = new float[2];

    private final InfoView infoView = new InfoView(getContext());
    private final ScrollChartView scrollChartView = new ScrollChartView(getContext(), this);
    private float stepY;

    public LineChart(Context context) {
        super(context);
        init();
    }

    public LineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        xAxisDateFormat = new SimpleDateFormat("MMM d", Utils.getLocale(getContext()));

        initPaints();
        initGraphs();
    }

    private void initPaints() {
        axisPaint.setColor(Utils.getColor(getContext(), R.color.gray));
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        axisTextPaint.setColor(Utils.getColor(getContext(), R.color.gray_text));
        axisTextPaint.setTextSize(axesTextSize);
        xAxisTextHeight = Utils.getFontHeight(axisTextPaint);
    }

    private void initGraphs() {
        graphs = new ChartGraph[4];

        ChartGraph chartGraph0 = new ChartGraph(ChartData.Y0, Utils.getColor(getContext(), R.color.graph1), graphLineWidth);
        ChartGraph chartGraph1 = new ChartGraph(ChartData.Y1, Utils.getColor(getContext(), R.color.graph2), graphLineWidth);
        ChartGraph chartGraph2 = new ChartGraph(ChartData.Y2, Utils.getColor(getContext(), R.color.graph3), graphLineWidth);
        ChartGraph chartGraph3 = new ChartGraph(ChartData.Y3, Utils.getColor(getContext(), R.color.graph4), graphLineWidth);

        graphs[0] = chartGraph0;
        graphs[1] = chartGraph1;
        graphs[2] = chartGraph2;
        graphs[3] = chartGraph3;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        availableChartHeight = (float) getHeight() - scrollChartView.scrollHeight - xAxisHeight;

        float rowHeight = availableChartHeight / rowNumber;


        maxYValue = getMaxYValue();
        maxYValueTemp = maxYValue;

        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = (int) (i * maxYValue * 1f / rowNumber);
        }

        updateYAxis();

        stepY = availableChartHeight / maxYValue;
        stepX = ((float) getWidth()) / (xPoints.length - 1);

        columnNumber = (int) (getWidth() / (xAxisHalfOfMaxTextWidth * 4f)) + 1;

        updateXAxis(UpdateXAxis.INIT);

        for (ChartGraph graph : graphs) {
            graph.path.reset();
            graph.path.moveTo(0, availableChartHeight - graph.values[0] * stepY);
        }

        for (int i = 1; i < end; i++) {
            for (ChartGraph graph : graphs) {
                graph.path.lineTo(i * stepX, availableChartHeight - graph.values[i] * stepY);
            }
        }

        scrollChartView.sizeChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        log("onTouchEvent ------------------------------------");

        float y = event.getY();

        if (y < availableChartHeight) {
            handleChartTouch(event);
        }

        if (y > availableChartHeight + xAxisHeight || scrollChartView.isMoving(event)) {
            scrollChartView.handleScrollTouch(event);
        }

        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            scrollChartView.cancelMoving();
            infoView.cancelMoving();
        }

        return event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE;
    }


    private void handleChartTouch(MotionEvent event) {
        infoView.handleTouch(event);

        float x = event.getX();
        int chartLineXPoint = xIndexByCoord(x);
        infoView.measure(xPoints[chartLineXPoint], chartLineXPoint, graphs);
        invalidate();
    }

    private int xIndexByCoord(float x) {
        tempPoint[0] = x;
        Matrix invert = Utils.invertMatrix(chartMatrix);

        invert.mapPoints(tempPoint);

        return Math.round(tempPoint[0] / stepX);
    }

    private float xCoordByIndex(float x) {
        tempPoint[0] = x;
        chartMatrix.mapPoints(tempPoint);

        return tempPoint[0];
    }

    private void updateYAxis() {
        int rowStep = (int) (Math.ceil(maxYValueTemp * 1f / rowNumber));

        for (int i = 0; i < rowNumber; i++) {
            yAxisTexts[i] = String.valueOf(rowStep * i);
        }
    }

    private void updateXAxis(UpdateXAxis update) {
        int startIndex = xIndexByCoord(xAxisSideMargin);
        int endIndex = xIndexByCoord(getWidth() - xAxisSideMargin);

        int range = endIndex - startIndex;
        float step = range * 1f / columnNumber;

        switch (update) {
            case INIT:

                if (step >= 1) {
                    for (int i = 0; i <= columnNumber; i++) {
                        int x = startIndex + Math.round(i * step);
                        xAxisPoints.add(buildXPoint(x));
                    }
//                    xTextsStep = (getWidth() - (xAxisSideMargin * 2f)) / columnNumber;
                } else {
                    for (int i = 0; i < range; i++) {
                        int x = startIndex + i;
                        xAxisPoints.add(buildXPoint(x));
                    }
//                    xTextsStep = getWidth() * 1f / range;
                }
                break;

            case TRANSLATE_LEFT:
                translateXAxis(step);
//                clearTempPoint();
//                tempPoint[0] = xAxisPoints.get(xAxisPoints.size() - 1).x * stepX;
//                chartMatrix.mapPoints(tempPoint);
//                float rightPointXCoord = tempPoint[0];
//                if (rightPointXCoord - xAxisHalfOfMaxTextWidth > getWidth()) {
//                    xAxisPoints.remove(xAxisPoints.size() - 1);
//                    int newX = Math.round(xAxisPoints.get(0).x - step);
//                    if (newX >= 0) {
//                        long millsec = xPoints[newX];
//                        tempDate.setTime(millsec);
//                        String dateSring = xAxisDateFormat.format(tempDate);
//                        xAxisPoints.add(new XAxisPoint(newX, dateSring, 255, axisTextPaint.measureText(dateSring)));
//                    }
//                }
                break;

            case TRANSLATE_RIGHT:
                translateXAxis(step);
//                clearTempPoint();
//                tempPoint[0] = xAxisPoints.get(0).x * stepX;
//                chartMatrix.mapPoints(tempPoint);
//                float leftPointXCoord = tempPoint[0];
//                if (leftPointXCoord + xAxisHalfOfMaxTextWidth < 0) {
//                    xAxisPoints.remove(0);
//                    int newX = Math.round(xAxisPoints.get(xAxisPoints.size() - 1).x + step);
//                    if (newX < xPoints.length) {
//                        long millsec = xPoints[newX];
//                        tempDate.setTime(millsec);
//                        String dateSring = xAxisDateFormat.format(tempDate);
//                        xAxisPoints.add(new XAxisPoint(newX, dateSring, 255, axisTextPaint.measureText(dateSring)));
//                    }
//                }
                break;

            case LEFT_IN:

                XAxisPoint left = xAxisPoints.get(0);
                XAxisPoint nextLeft = xAxisPoints.get(1);
                int xDistance = nextLeft.x - left.x;
                int newX = left.x - xDistance;
                if (newX >= 0 && isXTextVisible(newX)) {
                    xAxisPoints.add(0, buildXPoint(newX));
                }

                final ArrayList<XAxisPoint> pointToHide = new ArrayList<>();

                ListIterator<XAxisPoint> iteratorReverse = xAxisPoints.listIterator(xAxisPoints.size());
                while (iteratorReverse.hasPrevious()) {
                    XAxisPoint previous1 = iteratorReverse.previous();
                    if (iteratorReverse.hasPrevious()) {
                        XAxisPoint previous2 = iteratorReverse.previous();
                        float xCoord1 = getXViewCoord(previous1.x);
                        float xCoord2 = getXViewCoord(previous2.x);
                        if (previous2.alpha == 255 && xCoord1 - xCoord2 < xAxisHalfOfMaxTextWidth * 2) {
//                            iteratorReverse.remove();
                            pointToHide.add(previous2);
                        }
                    }
                }

//                for (int i = xAxisPoints.size() - 1; i >= 0; i-=2) {
//                    XAxisPoint previous1 = xAxisPoints.get(i);
//                    XAxisPoint previous2 = xAxisPoints.get(i - 1);
//                    float xCoord1 = getXViewCoord(previous1.x);
//                    float xCoord2 = getXViewCoord(previous2.x);
//                    if (previous2.alpha == 255 && xCoord1 - xCoord2 < xAxisHalfOfMaxTextWidth * 2) {
//                        pointToHide.add(previous2);
//                    }
//                }

                if (pointToHide.size() > 0) {

                    ValueAnimator valueAnimator = ValueAnimator.ofInt(255, 0);
                    valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            for (XAxisPoint point : pointToHide) {
                                point.alpha = value;
                            }
                        }
                    });

                    valueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            for (XAxisPoint point : pointToHide) {
                                xAxisPoints.remove(point);
                            }
                        }
                    });
                    valueAnimator.start();
                }

                break;

            case LEFT_OUT:

                Iterator<XAxisPoint> iterator = xAxisPoints.iterator();
                while (iterator.hasNext()) {
                    XAxisPoint next = iterator.next();
                    if (!isXTextVisible(next)) {
                        iterator.remove();
                    } else {
                        break;
                    }
                }

                final ArrayList<XAxisPoint> pointToAnimate = new ArrayList<>();

                if (xAxisPoints.size() <= columnNumber / 2 + 1) {
                    XAxisPoint right = xAxisPoints.get(xAxisPoints.size() - 1);
                    XAxisPoint preRight = xAxisPoints.get(xAxisPoints.size() - 2);
                    int distanceX = right.x - preRight.x;

                    for (int i = 0; i <= columnNumber; i+=2) {
                        XAxisPoint newPoint = buildXPointTransparent(xAxisPoints.get(xAxisPoints.size() - i - 1).x - distanceX / 2);
                        xAxisPoints.add(xAxisPoints.size() - i - 1, newPoint);
                        pointToAnimate.add(newPoint);
                    }

                    ValueAnimator hideValueAnimator = ValueAnimator.ofInt(0, 255);
                    hideValueAnimator.setDuration(SCALE_ANIMATION_DURATION);
                    hideValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animation.getAnimatedValue();
                            for (XAxisPoint point : pointToAnimate) {
                                point.alpha = value;
                            }

                        }
                    });
                    hideValueAnimator.start();
                }

                break;
        }


    }

    private void translateXAxis(float step) {
        Iterator<XAxisPoint> iterator = xAxisPoints.iterator();
        while (iterator.hasNext()) {
            XAxisPoint point = iterator.next();
            if (!isXTextVisible(point)) {
                iterator.remove();
            }
        }

        for (int i = 1; i < columnNumber; i++) {
            XAxisPoint lastPoint = xAxisPoints.get(xAxisPoints.size() - 1);
            int newX = Math.round(lastPoint.x + step * i);
            if (newX >= xPoints.length) break;
            if (isXTextVisible(newX)) {
                xAxisPoints.add(buildXPoint(newX));
            } else {
                break;
            }
        }
        for (int i = 1; i < columnNumber; i++) {
            XAxisPoint firstPoint = xAxisPoints.get(0);
            int newX = Math.round(firstPoint.x - step * i);
            if (newX < 0) break;
            if (isXTextVisible(newX)) {
                xAxisPoints.add(0, buildXPoint(newX));
            } else {
                break;
            }
        }
    }

    private XAxisPoint buildXPoint(int x) {
        return buildXPoint(x, 255);
    }

    private XAxisPoint buildXPointTransparent(int x) {
        return buildXPoint(x, 0);
    }

    private XAxisPoint buildXPoint(int x, int alpha) {
        long millsec = xPoints[x];
        tempDate.setTime(millsec);
        String dateSring = xAxisDateFormat.format(tempDate);
        return new XAxisPoint(x, dateSring, alpha, axisTextPaint.measureText(dateSring));
    }

    private boolean isXTextVisible(XAxisPoint point) {
        float xCoord = getXViewCoord(point.x);
        return xCoord + point.width / 2 > 0 && xCoord - point.width / 2 < getWidth();
    }

    private boolean isXTextVisible(int x) {
        float xCoord = getXViewCoord(x);
        return xCoord + xAxisHalfOfMaxTextWidth > 0 && xCoord - xAxisHalfOfMaxTextWidth / 2 < getWidth();
    }

    private float getXViewCoord(int x) {
        tempPoint[0] = x * stepX;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[0];
    }

    private float getYViewCoord(int y) {
        chartMatrix.getValues(chartMatrixValues);
        return y * stepY * chartMatrixValues[4];
    }

    private int getMaxYValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(graphs.length);
        for (ChartGraph graph : graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(start, end));
        }

        return Collections.max(maxValues);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        drawXAxes(canvas);

        drawXTexts(canvas);

        drawPoints(canvas);

        scrollChartView.draw(canvas);

        infoView.draw(canvas, availableChartHeight, chartMatrix, stepX, stepY);
    }

    private void drawXAxes(Canvas canvas) {
        canvas.save();

        canvas.translate(0f, availableChartHeight);

        for (int y : rowYValues) {
            canvas.save();
            canvas.translate(0, -getYViewCoord(y));
            axisPaint.setAlpha(rowYValuesAlpha);
            canvas.drawLine(0f, 0f, getWidth(), 0f, axisPaint);

            canvas.save();

            canvas.translate(0f, -xTextMargin);

            axisTextPaint.setAlpha(rowYValuesAlpha);
            canvas.drawText(String.valueOf(y), 0, 0, axisTextPaint);

            canvas.restore();

            canvas.restore();
        }

        if (rowYValuesAlpha < 255) {
            for (int y : rowYValuesToHide) {
                canvas.save();

                canvas.translate(0, -getYViewCoord(y));
                axisPaint.setAlpha(255 - rowYValuesAlpha);
                canvas.drawLine(0f, 0f, getWidth(), 0f, axisPaint);

                canvas.save();

                canvas.translate(0f, -xTextMargin);

                axisTextPaint.setAlpha(255 - rowYValuesAlpha);
                canvas.drawText(String.valueOf(y), 0, 0, axisTextPaint);

                canvas.restore();

                canvas.restore();
            }
        }

        canvas.restore();
    }

    private void drawXTexts(Canvas canvas) {
        if (xAxisPoints.size() == 0) return;

        canvas.save();

        canvas.translate(0, availableChartHeight + xTextMargin + xAxisTextHeight);

        for (XAxisPoint point : xAxisPoints) {
            float xCoord = getXViewCoord(point.x);
            canvas.drawLine(xCoord, -xAxisHeight, xCoord, 0, axisPaint);
            axisTextPaint.setAlpha(point.alpha);
            canvas.drawText(point.date, tempPoint[0] - point.width / 2f, 0, axisTextPaint);
        }

        canvas.restore();

    }

    private void drawPoints(Canvas canvas) {
        for (ChartGraph graph : graphs) {
            if (graph.paint.getAlpha() > 0) graph.draw(canvas, chartMatrix);
        }
    }

    public void enableGraph(final int index, boolean enable) {
        graphs[index].isEnable = enable;

        int fromAlpha = enable ? 0 : 255;
        int toAlpha = enable ? 255 : 0;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(fromAlpha, toAlpha);
        valueAnimator.setDuration(FADE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                graphs[index].paint.setAlpha(value);
                graphs[index].scrollPaint.setAlpha(value);
                invalidate();

            }
        });
        valueAnimator.start();

        adjustYAxis();
        scrollChartView.adjustYAxis();
    }

    public void adjustYAxis() {
        int newTempMaxYValue = getMaxYValue();

        if (newTempMaxYValue == this.maxYValueTemp) return;

        System.arraycopy(rowYValues, 0, rowYValuesToHide, 0, rowNumber);

        rowYValuesAlpha = 0;
        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = (int) (i * newTempMaxYValue * 1f / rowNumber);
        }

        float toScale = this.maxYValue * 1f / newTempMaxYValue;

        float fromScale = this.maxYValue * 1f / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        log("adjustYAxis_fromScale: " + fromScale);
        log("adjustYAxis_toScale: " + toScale);

        final float[] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                chartMatrix.postScale(1, value / prev[0], 0f, availableChartHeight);
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();

        ValueAnimator valueAnimator2 = ValueAnimator.ofInt(0, 255);
        valueAnimator2.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rowYValuesAlpha = (int) animation.getAnimatedValue();
                invalidate();

            }
        });
        valueAnimator2.start();

        updateYAxis();
    }

    public void setStart(int start) {
        if (start >= end - 2) return;
        if (start == this.start) return;

        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, true);
        boolean isLeftIn = start < this.start;
        this.start = start;
        updateXAxis(isLeftIn ? UpdateXAxis.LEFT_IN : UpdateXAxis.LEFT_OUT);
        adjustYAxis();
        invalidate();
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

        if (end == this.end) return;

        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, false);

        boolean isRightIn = end > this.end;

        this.end = end;
        updateXAxis(isRightIn ? UpdateXAxis.RIGHT_IN : UpdateXAxis.RIGHT_OUT);
        adjustYAxis();
        invalidate();
    }

    public void setStartEnd(int start, int end) {
        if (end > xPoints.length) return;
        if (start >= end - 1) return;

        if (start == this.start && end == this.end) return;

        final float[] prev = new float[1];
        prev[0] = 0f;

        float fromCoordStart = xCoordByIndex(this.start * stepX);
        float toCoordStart = xCoordByIndex(start * stepX);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, fromCoordStart - toCoordStart);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                chartMatrix.postTranslate(value - prev[0], 0);
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();

        boolean isLeftTranslate = start < this.start;

        this.start = start;
        this.end = end;
        updateXAxis(isLeftTranslate ? UpdateXAxis.TRANSLATE_LEFT : UpdateXAxis.TRANSLATE_RIGHT);
        adjustYAxis();
        invalidate();
    }

    private void startScaleAnimation(float toScale, final boolean isStart) {
        float fromScale = xPoints.length / (end - start * 1f);

        log("fromScale: " + fromScale);
        log("toScale: " + toScale);

        final float[] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                chartMatrix.postScale(value / prev[0], 1, isStart ? getWidth() : 0, 0f);
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();
    }

    void log(String text) {
        if (LOG_IS_ENABLED) Log.d(LOG_TAG, text);
    }
}
