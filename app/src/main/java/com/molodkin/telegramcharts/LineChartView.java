package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

public final class LineChartView extends View {

    private static final boolean LOG_IS_ENABLED = false;
    private static final String LOG_TAG = "LineChart";

    static final long SCALE_ANIMATION_DURATION = 250L;
    static final long FADE_ANIMATION_DURATION = 150L;

    ChartGraph[] graphs;

    long[] xPoints;

    public final Matrix chartMatrix = new Matrix();
    public final Matrix chartInvertMatrix = new Matrix();

    private final float [] chartMatrixValues = new float[9];

    int start = 0;
    int end = 0;

    int drawStart = 0;
    int drawEnd = 0;

    int yAdjustStart = 0;
    int yAdjustEnd = 0;

    private int [] availableYSteps = {5, 10, 20, 25, 40, 50, 100, 500, 1_000, 1_500, 2_000, 2_500, 3_000, 4_000, 5000, 10_000, 20_000, 30_000, 40_000, 50_000, 100_000, 200_000, 300_000, 400_000, 500_000, 1000_000};

    private ArrayList<XAxisPoint> pointToHideAnimated = new ArrayList<>();

    private int maxYValueTemp;
    int maxYValue;

    private int rowNumber = 6;
    private int [] rowYValues = new int[rowNumber];
    private int [] rowYValuesToHide = new int[rowNumber];
    private String [] rowYTextsValues = new String[rowNumber];
    private String [] rowYTextsValuesToHide = new String[rowNumber];
    private int rowYValuesAlpha = 255;

    private final ArrayList<XAxisPoint> xAxisPoints = new ArrayList<>();

    int sideMargin = Utils.dpToPx(this, 20);
    int xAxisHeight = Utils.dpToPx(this, 33);
    private int xAxisWidth = Utils.dpToPx(this, 1);

    private float xAxisTextHeight;

    private int graphLineWidth = Utils.dpToPx(this, 2);

    float availableChartHeight;
    float availableChartWidth;

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

    float stepY;

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

    private void init() {
        xAxisDateFormat = new SimpleDateFormat("MMM d", Utils.getLocale(getContext()));

        initPaints();
        initGraphs();
    }

    private void initPaints() {
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        axisTextPaint.setTextSize(axesTextSize);
        axisTextPaint.setAntiAlias(true);
        xAxisTextHeight = Utils.getFontHeight(axisTextPaint);
        initTheme();
    }

    private void initTheme() {
        axisPaint.setColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));
        axisTextPaint.setColor(Utils.getColor(getContext(), Utils.AXIS_TEXT_COLOR));
    }

    private void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;

        start = 0;
        end = data.x.length;

        drawStart = 0;
        drawEnd = data.x.length;

        yAdjustStart = 0;
        yAdjustEnd = data.x.length;

        graphs = new ChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new ChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), graphLineWidth, data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - xAxisHeight;
        availableChartWidth = (float) getWidth() - sideMargin * 2;

        maxYValue = getAdjustedMaxYValue();
        maxYValueTemp = maxYValue;

        for (int i = 0; i < rowNumber; i++) {
            rowYValues[i] = (int) (i * maxYValue * 1f / rowNumber);
            rowYTextsValues[i] = String.valueOf(rowYValues[i]);
        }

        stepY = availableChartHeight / maxYValue;
        stepX = availableChartWidth / (xPoints.length - 1);

        int endX = (int) ((availableChartWidth) / stepX);
        int stepXAxis = Math.round(xAxisTextWidthWithMargins / stepX);

        while (endX > 0) {
            xAxisPoints.add(0, buildXPoint(endX));
            endX-= stepXAxis;
        }

        for (int i = 0; i < end - 1; i++) {
            for (ChartGraph graph : graphs) {
                int j = i * 4;
                int k = i * 2;
                float x1 = i * stepX;
                float y1 = availableChartHeight - graph.values[i] * stepY;

                graph.points[k] = x1;
                graph.points[k + 1] = y1;

                graph.linePoints[j] = x1;
                graph.linePoints[j + 1] = y1;
                graph.linePoints[j + 2] = (i + 1) * stepX;
                graph.linePoints[j + 3] = availableChartHeight - graph.values[i + 1] * stepY;

                if (i == 0) {
                    graph.path.moveTo(0, availableChartHeight - graph.values[0] * stepY);
                } else {
                    graph.path.lineTo(i * stepX, availableChartHeight - graph.values[i] * stepY);
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initGraphs();
    }

    public void updateTheme() {
        initTheme();
        invalidate();
    }

    private float getXViewCoord(int x) {
        tempPoint[0] = x * stepX;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[0];
    }

    int xIndexByCoord(float x) {
        tempPoint[0] = x - sideMargin;

        chartMatrix.invert(chartInvertMatrix);
        chartInvertMatrix.mapPoints(tempPoint);

        int value = Math.round(tempPoint[0] / stepX);
        return Math.max(Math.min(value, xPoints.length - 1), 0);
    }

    float xValueByCoord(float x) {
        tempPoint[0] = x - sideMargin;

        chartMatrix.invert(chartInvertMatrix);
        chartInvertMatrix.mapPoints(tempPoint);

        float value = tempPoint[0] / stepX;
        return Math.max(Math.min(value, xPoints.length - 1), 0);
    }

    float yCoordByIndex(int y) {
        tempPoint[1] = availableChartHeight - y * stepY;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[1];
    }

    float yCoordByValue(float y) {
        tempPoint[1] = availableChartHeight - y * stepY;
        chartMatrix.mapPoints(tempPoint);
        return tempPoint[1];
    }

    private void adjustXAxis() {
        final ArrayList<XAxisPoint> pointsToShow = new ArrayList<>();
        final ArrayList<XAxisPoint> pointsToHide = new ArrayList<>();
        XAxisPoint first = xAxisPoints.get(0);
        XAxisPoint second = xAxisPoints.get(1);
        int currentStepX = second.x - first.x;

        float currentDistance = getXViewCoord(second.x) - getXViewCoord(first.x);

        if (currentDistance > xAxisTextWidthWithMargins) {
            int numberToAdd = (int) (currentDistance / xAxisTextWidthWithMargins) - 1;
            currentStepX = currentStepX / (numberToAdd + 1);
            for (int i = 0; i < xAxisPoints.size() - numberToAdd; i+= numberToAdd + 1) {
                for (int j = 1; j <= numberToAdd; j++) {
                    int newX = xAxisPoints.get(i).x + currentStepX * j;
                    if (newX >= xPoints.length) break;
                    XAxisPoint point = buildXPointTransparent(newX);
                    xAxisPoints.add(i + j, point);
                    pointsToShow.add(point);
                }
            }
        } else if (currentDistance < xAxisTextWidth ) {
            int numberToRemove = (int) (xAxisTextWidth / currentDistance);
            ListIterator<XAxisPoint> iterator = xAxisPoints.listIterator(xAxisPoints.size());
            iterator.previous();
            while (iterator.hasPrevious()) {
                for (int i = 0; i < numberToRemove; i++) {
                    if (iterator.hasPrevious()){
                        pointsToHide.add(iterator.previous());
                        iterator.remove();
                    } else break;
                }
                if (iterator.hasPrevious()) iterator.previous();
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

        //remove left linePoints
        Iterator<XAxisPoint> iterator = xAxisPoints.iterator();
        while (iterator.hasNext()) {
            XAxisPoint point = iterator.next();
            if (!isXTextVisible(point)) iterator.remove();
            else break;
        }

        //remove right linePoints
        ListIterator<XAxisPoint> reverse = xAxisPoints.listIterator(xAxisPoints.size());
        while (reverse.hasPrevious()) {
            XAxisPoint point = reverse.previous();
            if (!isXTextVisible(point)) reverse.remove();
            else break;
        }

        if (pointsToShow.size() > 0) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 255);
            valueAnimator.setDuration(FADE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (XAxisPoint point : pointsToShow) {
                        point.alpha = (int) animation.getAnimatedValue();
                    }
                    invalidate();

                }
            });
            valueAnimator.start();
        }

        if (pointsToHide.size() > 0) {
            pointToHideAnimated = new ArrayList<>(pointsToHide);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(255, 0);
            valueAnimator.setDuration(FADE_ANIMATION_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    for (XAxisPoint point : pointToHideAnimated) {
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
        float xCoord = getXViewCoord(point.x) + sideMargin;
        return xCoord + point.width / 2 > 0 && xCoord - point.width / 2 < getWidth();
    }

    private boolean isXTextVisible(int x) {
        float xCoord = getXViewCoord(x) + sideMargin;
        return xCoord + xAxisHalfOfTextWidth > 0 && xCoord - xAxisHalfOfTextWidth < getWidth();
    }

    private float getYViewCoord(int y) {
        chartMatrix.getValues(chartMatrixValues);
        return y * stepY * chartMatrixValues[4];
    }

    private int getMaxYValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(graphs.length);
        for (ChartGraph graph : graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(yAdjustStart, yAdjustEnd));
        }

        if (maxValues.size() == 0) {
            return maxYValueTemp;
        } else {
            return Collections.max(maxValues);
        }
    }

    private int getAdjustedMaxYValue() {
        int value = getMaxYValue();
        int tempStep = (int) Math.ceil(value * 1f / rowNumber);

        for (int i = 0; i < availableYSteps.length - 1; i++) {
            if (availableYSteps[i] < tempStep && tempStep <= availableYSteps[i + 1]) {
                value = rowNumber * availableYSteps[i + 1];
                break;
            }
        }

        return value;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        canvas.translate(sideMargin, 0);

        drawXAxes(canvas);

        drawXTexts(canvas);

        drawPoints(canvas);
    }

    private void drawXAxes(Canvas canvas) {
        canvas.save();

        canvas.translate(0f, availableChartHeight);

        for (int i = 0; i < rowYValues.length; i++) {
            int y = rowYValues[i];
            canvas.save();
            canvas.translate(0, -getYViewCoord(y));
            axisPaint.setAlpha(rowYValuesAlpha);
            canvas.drawLine(0f, 0f, availableChartWidth, 0f, axisPaint);

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
                canvas.drawLine(0f, 0f, availableChartWidth, 0f, axisPaint);

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

        Iterator<XAxisPoint> iterator = pointToHideAnimated.iterator();

        while (iterator.hasNext()) {
            XAxisPoint next = iterator.next();
            if (next.alpha == 0) iterator.remove();
        }

        for (XAxisPoint point : pointToHideAnimated) {
            float xCoord = getXViewCoord(point.x);
            axisTextPaint.setAlpha(point.alpha);
            canvas.drawText(point.date, xCoord - point.width / 2f, 0, axisTextPaint);
        }

        canvas.restore();

    }

    private void drawPoints(Canvas canvas) {
//        int drawStart = Math.max(Math.min(start, this.drawStart), 0);
//        int drawEnd = Math.min(Math.max(end, this.drawEnd), xPoints.length);
        log("drawPoints drawStart: " + this.drawStart + " drawEnd: " + drawEnd);
//        log("drawPoints start: " + start + " end: " + end);
//        log("drawPoints drawStart: " + drawStart + " drawEnd: " + drawEnd);
//        log("drawPoints ------------------------------------");
        for (ChartGraph graph : graphs) {
            if (graph.linePaint.getAlpha() > 0) {
                graph.draw(canvas, chartMatrix, Math.max(drawStart, 0), Math.min(drawEnd, xPoints.length));
            }
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
                graphs[index].linePaint.setAlpha(value);
                graphs[index].pointPaint.setAlpha(value);
                graphs[index].scrollLinePaint.setAlpha(value);
                graphs[index].scrollPointPaint.setAlpha(value);
                invalidate();

            }
        });
        valueAnimator.start();

        adjustYAxis();
    }

    public void adjustYAxis() {
        int newTempMaxYValue = getAdjustedMaxYValue();

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

        float toScale = availableChartWidth / (getXViewCoord(end - 1) - getXViewCoord(start));
        startScaleAnimation(toScale, true);
        this.start = start;

        this.yAdjustStart = xIndexByCoord(0);
        this.yAdjustEnd = xIndexByCoord(getWidth()) + 1;

        adjustYAxis();
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

        if (end == this.end) return;

        float toScale = availableChartWidth / (getXViewCoord(end - 1) - getXViewCoord(start));
        startScaleAnimation(toScale, false);

        this.end = end;

        this.yAdjustStart = xIndexByCoord(0);
        this.yAdjustEnd = xIndexByCoord(getWidth()) + 1;

        adjustYAxis();
    }

    public void setStartEnd(int start, int end) {
        if (end > xPoints.length) return;
        if (start >= end - 1) return;

        if (start == this.start && end == this.end) return;

        final float[] prev = new float[1];
        prev[0] = 0f;

        float fromCoordStart = getXViewCoord(this.start);
        float toCoordStart = getXViewCoord(start);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, fromCoordStart - toCoordStart);
        valueAnimator.setDuration(SCALE_ANIMATION_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                chartMatrix.postTranslate(value - prev[0], 0);
                drawStart = xIndexByCoord(0);
                drawEnd = xIndexByCoord(getWidth()) + 1;
                prev[0] = value;
                adjustXAxis();
                invalidate();

            }
        });
        valueAnimator.start();

        this.start = start;
        this.end = end;

        this.yAdjustStart = xIndexByCoord(0);
        this.yAdjustEnd = xIndexByCoord(getWidth()) + 1;

        adjustYAxis();
    }

    private void startScaleAnimation(float toScale, final boolean isStart) {
        float fromScale = availableChartWidth / (getXViewCoord(end - 1) - getXViewCoord(start));

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
                chartMatrix.postScale(value / prev[0], 1, isStart ? availableChartWidth : 0f, 0f);
                drawStart = xIndexByCoord(0);
                drawEnd = xIndexByCoord(getWidth()) + 1;
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
