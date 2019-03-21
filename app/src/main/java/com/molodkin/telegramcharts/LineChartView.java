package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
public final class LineChartView extends View {

    private static final boolean LOG_IS_ENABLED = true;
    private static final String LOG_TAG = "LineChart";

    static final long SCALE_ANIMATION_DURATION = 250L;
    static final long FADE_ANIMATION_DURATION = 125L;

    private boolean canBeDrawn = false;

    ChartGraph[] graphs;

    long[] xPoints;

    public final Matrix chartMatrix = new Matrix();

    private final float [] chartMatrixValues = new float[9];

    int start = 0;
    int end = 0;

    private ArrayList<XAxisPoint> pointToHide = new ArrayList<>();

    private int maxYValueTemp;
    int maxYValue;

    private int rowNumber = 6;
    private int [] rowYValues = new int[rowNumber];
    private int [] rowYValuesToHide = new int[rowNumber];
    private String [] rowYTextsValues = new String[rowNumber];
    private String [] rowYTextsValuesToHide = new String[rowNumber];
    private int rowYValuesAlpha = 255;

    private final ArrayList<XAxisPoint> xAxisPoints = new ArrayList<>();

    int xAxisHeight = Utils.dpToPx(this, 33);
    private int xAxisWidth = Utils.dpToPx(this, 1);

    private float xAxisTextHeight;

    private int xAxisSideMargin = Utils.dpToPx(this, 20);

    private int graphLineWidth = Utils.dpToPx(this, 2);

    float availableChartHeight;

    private int axesTextSize = Utils.spToPx(this, 14);
    private int xTextMargin = Utils.dpToPx(this, 4);
    private int xAxisHalfOfTextWidth = Utils.dpToPx(this, 27);


    private int xAxisTextWidth = xAxisHalfOfTextWidth * 2;
    private int xAxisTextWidthWithMargins = xAxisTextWidth + 2 * Utils.dpToPx(this, 4);

    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();

    float stepX;

    private Date tempDate = new Date();
    private DateFormat xAxisDateFormat;

    private float[] tempPoint = new float[2];

    private final InfoView infoView = new InfoView(getContext());
    private final ScrollChartView scrollChartView = new ScrollChartView(getContext(), this);
    private float stepY;

    private ChartData data;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setData(ChartData data) {
        this.data = data;
        if (getWidth() > 0 && getHeight() > 0) {
            initGraphs();
        }
    }

    public void setFake() {
        this.data = ChartData.buidFake();
    }

    private void init() {
        xAxisDateFormat = new SimpleDateFormat("MMM d", Utils.getLocale(getContext()));

        initPaints();
        initGraphs();
    }

    private void initPaints() {
        axisPaint.setColor(Utils.getColor(getContext(), R.color.axis_day));
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        axisTextPaint.setColor(Utils.getColor(getContext(), R.color.text_day));
        axisTextPaint.setTextSize(axesTextSize);
        axisTextPaint.setAntiAlias(true);
        xAxisTextHeight = Utils.getFontHeight(axisTextPaint);
    }

    private void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;
        end = data.x.length;

        graphs = new ChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new ChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), graphLineWidth, data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - scrollChartView.scrollHeight - xAxisHeight;

        maxYValue = getMaxYValue();
        maxYValueTemp = maxYValue;

        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = (int) (i * maxYValue * 1f / rowNumber);
            rowYTextsValues[i] = String.valueOf(rowYValues[i]);
        }

        stepY = availableChartHeight / maxYValue;
        stepX = ((float) getWidth()) / (xPoints.length - 1);

        int endX = (int) ((getWidth() - xAxisHalfOfTextWidth) / stepX);
        int stepXAxis = Math.round(xAxisTextWidthWithMargins / stepX);

        while (endX > 0) {
            xAxisPoints.add(0, buildXPoint(endX));
            endX-= stepXAxis;
        }

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initGraphs();


    }

    public void setDayMode(boolean dayMode) {
        if (dayMode) {
            axisPaint.setColor(Utils.getColor(getContext(), R.color.axis_day));
            axisTextPaint.setColor(Utils.getColor(getContext(), R.color.text_day));
        } else {
            axisPaint.setColor(Utils.getColor(getContext(), R.color.axis_night));
            axisTextPaint.setColor(Utils.getColor(getContext(), R.color.text_night));
        }
        infoView.setDayMode(dayMode);
        invalidate();
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

    private void adjustXAxis() {
        pointToHide = new ArrayList<>();
        final ArrayList<XAxisPoint> poitToShow = new ArrayList<>();
        XAxisPoint first = xAxisPoints.get(0);
        XAxisPoint second = xAxisPoints.get(1);
        int currentStepX = second.x - first.x;

        float currentDistance = getXViewCoord(second.x) - getXViewCoord(first.x);

        if (currentDistance > xAxisTextWidthWithMargins * 1.5) {
            int numberToAdd = (int) (currentDistance / xAxisTextWidthWithMargins) - 1;
            currentStepX = currentStepX / (numberToAdd + 1);
            for (int i = 0; i < xAxisPoints.size() - numberToAdd; i+= numberToAdd + 1) {
                for (int j = 1; j <= numberToAdd; j++) {
                    int newX = xAxisPoints.get(i).x + currentStepX * j;
                    if (newX >= xPoints.length) break;
                    XAxisPoint point = buildXPointTransparent(newX);
                    xAxisPoints.add(i + j, point);
                    poitToShow.add(point);
                }
            }
        } else if (currentDistance < xAxisTextWidthWithMargins) {
            int numberToRemove = (int) (xAxisTextWidthWithMargins / currentDistance);
            Iterator<XAxisPoint> iterator = xAxisPoints.iterator();
            iterator.next();
            while (iterator.hasNext()) {
                for (int i = 0; i < numberToRemove; i++) {
                    if (iterator.hasNext()){
                        pointToHide.add(iterator.next());
                        iterator.remove();
                    } else break;
                }
                if (iterator.hasNext()) iterator.next();
                else break;
            }
        }

        first = xAxisPoints.get(0);
        second = xAxisPoints.get(1);
        currentStepX = second.x - first.x;


        int leftPointX = first.x - currentStepX;
        while (leftPointX >= 0) {
            if (isXTextVisible(leftPointX)) {
                XAxisPoint point = buildXPoint(leftPointX);
                xAxisPoints.add(0, point);
            } else break;
            leftPointX -= currentStepX;
        }

        int rightPointX = xAxisPoints.get(xAxisPoints.size() - 1).x + currentStepX;
        while (rightPointX < xPoints.length) {
            if (isXTextVisible(rightPointX)) {
                XAxisPoint point = buildXPoint(rightPointX);
                xAxisPoints.add(point);
            } else break;
            rightPointX += currentStepX;
        }

        //remove left points
        Iterator<XAxisPoint> iterator = xAxisPoints.iterator();
        while (iterator.hasNext()) {
            XAxisPoint point = iterator.next();
            if (!isXTextVisible(point)) iterator.remove();
            else break;
        }

        //remove right points
        ListIterator<XAxisPoint> reverse = xAxisPoints.listIterator(xAxisPoints.size());
        while (reverse.hasPrevious()) {
            XAxisPoint point = reverse.previous();
            if (!isXTextVisible(point)) reverse.remove();
            else break;
        }

        if (poitToShow.size() > 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 255);
            valueAnimator.setDuration(FADE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (XAxisPoint point : poitToShow) {
                        point.alpha = (int) animation.getAnimatedValue();
                    }
                    invalidate();

                }
            });
            valueAnimator.start();
        }

        if (pointToHide.size() > 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(255, 0);
            valueAnimator.setDuration(FADE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (XAxisPoint point : pointToHide) {
                        point.alpha = (int) animation.getAnimatedValue();
                    }
                    invalidate();

                }
            });
            valueAnimator.start();
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
        return xCoord + xAxisHalfOfTextWidth > 0 && xCoord - xAxisHalfOfTextWidth < getWidth();
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

        for (int i = 0; i < rowYValues.length; i++) {
            int y = rowYValues[i];
            canvas.save();
            canvas.translate(0, -getYViewCoord(y));
            axisPaint.setAlpha(rowYValuesAlpha);
            canvas.drawLine(0f, 0f, getWidth(), 0f, axisPaint);

            canvas.save();

            canvas.translate(0f, -xTextMargin);

            axisTextPaint.setAlpha(rowYValuesAlpha);
            canvas.drawText(rowYTextsValues[i], 0, 0, axisTextPaint);

            canvas.restore();

            canvas.restore();
        }

        if (rowYValuesAlpha < 255) {
            for (int i = 0; i < rowYValuesToHide.length; i++) {
                int y = rowYValuesToHide[i];
                canvas.save();

                canvas.translate(0, -getYViewCoord(y));
                axisPaint.setAlpha(255 - rowYValuesAlpha);
                canvas.drawLine(0f, 0f, getWidth(), 0f, axisPaint);

                canvas.save();

                canvas.translate(0f, -xTextMargin);

                axisTextPaint.setAlpha(255 - rowYValuesAlpha);
                canvas.drawText(rowYTextsValuesToHide[i], 0, 0, axisTextPaint);

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
            axisTextPaint.setAlpha(point.alpha);
            canvas.drawText(point.date, xCoord - point.width / 2f, 0, axisTextPaint);
        }

        Iterator<XAxisPoint> iterator = pointToHide.iterator();

        while (iterator.hasNext()) {
            XAxisPoint next = iterator.next();
            if (next.alpha == 0) iterator.remove();
        }

        for (XAxisPoint point : pointToHide) {
            float xCoord = getXViewCoord(point.x);
            axisTextPaint.setAlpha(point.alpha);
            canvas.drawText(point.date, xCoord - point.width / 2f, 0, axisTextPaint);
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
        System.arraycopy(rowYTextsValues, 0, rowYTextsValuesToHide, 0, rowNumber);

        rowYValuesAlpha = 0;
        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = (int) (i * newTempMaxYValue * 1f / rowNumber);
            rowYTextsValues[i] = String.valueOf(rowYValues[i]);
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
    }

    public void setStart(int start) {
        if (start >= end - 2) return;
        if (start == this.start) return;

        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, true);
        this.start = start;

        adjustYAxis();
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

        if (end == this.end) return;

        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, false);

        this.end = end;

        adjustYAxis();
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
                adjustXAxis();
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();

        this.start = start;
        this.end = end;

        adjustYAxis();
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
                adjustXAxis();
                invalidate();

            }
        });
        valueAnimator.start();
    }

    void log(String text) {
        if (LOG_IS_ENABLED) Log.d(LOG_TAG, text);
    }
}
