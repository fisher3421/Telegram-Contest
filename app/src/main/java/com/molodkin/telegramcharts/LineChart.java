package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class LineChart extends View {


    private static final boolean LOG_IS_ENABLED = true;
    private static final String LOG_TAG = "LineChart";


    private ChartGraph [] graphs;

    private long [] xPoints = ChartData.X;


    private int start = 0;
    private int end = xPoints.length;

    private int maxYValueTemp;
    private int maxYValue;

    private int rowNumber = 6;
    private int columnNumber = 5;

    private String [] yAxisTexts = new String[rowNumber];
    private String [] xAxisTexts = new String[columnNumber + 1];

    private int scrollHeight = Utils.dpToPx(this, 40);
    private int xAxisHeight = Utils.dpToPx(this, 33);
    private int xAxisWidth = Utils.dpToPx(this, 1);

    private int xAxisSideMargin = Utils.dpToPx(this, 20);

    private int graphWidth = Utils.dpToPx(this, 2);

    private float availableHeight;

    private int textSize = Utils.spToPx(this.getContext(), 14);
    private int xTextMargin = Utils.dpToPx(this, 2);

    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();

    private float rowHeight;
    private float stepX;

    private Rect xTextBounds = new Rect();

    private DateFormat dateFormat = new SimpleDateFormat("MMM d");
    private float xTextsStep;

    public LineChart(Context context) {
        super(context);
        init();
    }

    public LineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        initPaints();
        initGapths();
    }

    private void initPaints() {
        axisPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray));
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(xAxisWidth);

        axisTextPaint = new TextPaint();
        axisTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_text));
        axisTextPaint.setTextSize(textSize);
    }

    private void initGapths() {
        graphs = new ChartGraph[4];

        ChartGraph chartGraph0 = new ChartGraph(ChartData.Y0, ContextCompat.getColor(getContext(), R.color.graph1), graphWidth);
        ChartGraph chartGraph1 = new ChartGraph(ChartData.Y1, ContextCompat.getColor(getContext(), R.color.graph2), graphWidth);
        ChartGraph chartGraph2 = new ChartGraph(ChartData.Y2, ContextCompat.getColor(getContext(), R.color.graph3), graphWidth);
        ChartGraph chartGraph3 = new ChartGraph(ChartData.Y3, ContextCompat.getColor(getContext(), R.color.graph4), graphWidth);

        graphs[0] = chartGraph0;
        graphs[1] = chartGraph1;
        graphs[2] = chartGraph2;
        graphs[3] = chartGraph3;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        availableHeight = (float) getHeight() - scrollHeight - xAxisHeight;
        rowHeight = availableHeight / rowNumber;

        maxYValue = getMaxYValue();
        maxYValueTemp = maxYValue;

        updateYAxis();

        float coeffY = availableHeight / maxYValue;
        stepX = ((float) getWidth()) / (xPoints.length - 1);

        updateXAxis();

        for (ChartGraph graph : graphs) {
            graph.path.reset();
            graph.path.moveTo(0, availableHeight - graph.values[0] * coeffY);
        }

        for (int i = 1; i < end; i++) {
            for (ChartGraph graph : graphs) {
                graph.path.lineTo(i * stepX, availableHeight - graph.values[i] * coeffY);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        log("onTouchEvent ------------------------------------");

        float x = event.getX();
        int i = xIndexByCoord(x);
        log("onTouchEvent x: " + x);
        log("onTouchEvent i: " + i);
        log("onTouchEvent graph0: " + graphs[0].values[i]);
        log("onTouchEvent graph1: " + graphs[1].values[i]);
        log("onTouchEvent graph2: " + graphs[2].values[i]);
        log("onTouchEvent graph3: " + graphs[3].values[i]);

        return super.onTouchEvent(event);
    }

    private int xIndexByCoord(float x) {
        float [] touchPoints = new float[] {x, 0};
        Matrix invert = Utils.invertMatrix(graphs[0].matrix);

        invert.mapPoints(touchPoints);

        return Math.round(touchPoints[0] / stepX);
    }

    private void updateYAxis() {
        int rowStep = (int) (Math.ceil(maxYValueTemp * 1f / rowNumber));

        for (int i = 0; i < rowNumber; i++) {
            yAxisTexts[i] = String.valueOf(rowStep * i);
        }
    }

    private void updateXAxis() {
        int startIndex = xIndexByCoord(xAxisSideMargin);
        int endIndex = xIndexByCoord(getWidth() - xAxisSideMargin);

        int range = endIndex - startIndex;
        float step = range * 1f / columnNumber;
        if (step >= 1) {
            for (int i = 0; i <= columnNumber; i++) {
                long millsec = xPoints[startIndex + Math.round(i * step)];
                xAxisTexts[i] = dateFormat.format(new Date(millsec));
            }
            xTextsStep = (getWidth() - (xAxisSideMargin * 2f)) / columnNumber;
        } else {
            for (int i = 0; i < range; i++) {
                long millsec = xPoints[startIndex +i];
                xAxisTexts[i] = dateFormat.format(new Date(millsec));
            }
            xAxisTexts[range] = null;
            xTextsStep = getWidth() * 1f / range;
        }

        axisTextPaint.getTextBounds(xAxisTexts[0], 0, xAxisTexts[0].length(), xTextBounds);

    }

    private int getMaxYValue() {
        ArrayList<Integer> maxValues = new ArrayList<>(graphs.length);
        for (ChartGraph graph : graphs) {
            maxValues.add(graph.getMax(start, end));
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
    }

    private void drawXAxes(Canvas canvas) {
        canvas.save();

        canvas.translate(0f, availableHeight);

        for (int i = 0; i < rowNumber; i++) {
            canvas.drawLine(0f, 0f, getWidth(), 0f, axisPaint);

            canvas.save();

            canvas.translate(0f, -xTextMargin);

            canvas.drawText(yAxisTexts[i], 0, 0, axisTextPaint);

            canvas.restore();

            canvas.translate(0f, -rowHeight);
        }

        canvas.restore();
    }

    private void drawXTexts(Canvas canvas) {
        if (xAxisTexts[0] == null) return;

        canvas.save();

        canvas.translate(0, availableHeight + xTextMargin + xTextBounds.height());

        for (String xAxisText : xAxisTexts) {
            if (xAxisText == null) break;
            canvas.drawText(xAxisText, 0, 0, axisTextPaint);
            canvas.translate(xTextsStep, 0);
        }

        canvas.restore();

    }

    private void drawPoints(Canvas canvas) {
        for (ChartGraph graph : graphs) {
            graph.draw(canvas);
        }
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int start) {
        if (start >= end - 2) return;

//        matrix.reset();
        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, true);
        this.start = start;
        updateXAxis();
        invalidate();
    }

    public void adjustYAxis() {
        int newTempMaxYValue = getMaxYValue();

        float toScale = this.maxYValue * 1f / newTempMaxYValue;

        float fromScale = this.maxYValue * 1f / this.maxYValueTemp;

        this.maxYValueTemp = newTempMaxYValue;

        Log.d("LineChart_adjustYAxis", "fromScale: " + fromScale);
        Log.d("LineChart_adjustYAxis", "toScale: " + toScale);

        final float [] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(250L);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                for (ChartGraph graph : graphs) {
                    graph.matrix.postScale(1, value / prev[0], 0f, availableHeight);
                }
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();

        updateYAxis();
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

//        matrix.reset();
        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, false);

        this.end = end;
        updateXAxis();
        invalidate();
    }

    public void setStartEnd(int start, int end) {
        if (end > xPoints.length) return;
        if (start >= end) return;

        final float [] prev = new float[1];
        prev[0] = 0f;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, (this.start - start) * stepX);
        valueAnimator.setDuration(250L);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                for (ChartGraph graph : graphs) {
                    graph.matrix.postTranslate(value - prev[0], 0);
                }
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();

        this.start = start;
        this.end = end;
        updateXAxis();
        invalidate();
    }

    private void startScaleAnimation(float toScale, final boolean isStart) {
        float fromScale = xPoints.length / (end - start * 1f);

        Log.d("LineChart", "fromScale: " + fromScale);
        Log.d("LineChart", "toScale: " + toScale);

        final float [] prev = new float[1];
        prev[0] = fromScale;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
        valueAnimator.setDuration(250L);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                for (ChartGraph graph : graphs) {
                    graph.matrix.postScale(value / prev[0], 1, isStart ? getWidth() : 0, 0f);
                }
                prev[0] = value;
                invalidate();

            }
        });
        valueAnimator.start();
    }

    private void startScaleVerticalAnimation(float toScale) {
//        float fromScale = xPoints.length / (end - start * 1f);
//
//        Log.d("LineChart", "fromScale: " + fromScale);
//        Log.d("LineChart", "toScale: " + toScale);
//
//        final float [] prev = new float[1];
//        prev[0] = fromScale;
//
//        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromScale, toScale);
//        valueAnimator.setDuration(250L);
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float value = (float) animation.getAnimatedValue();
//                for (ChartGraph graph : graphs) {
//                    graph.matrix.postScale(value / prev[0], 1, isStart ? getWidth() : 0, 0f);
//                }
//                prev[0] = value;
//                invalidate();
//
//            }
//        });
//        valueAnimator.start();
    }

    private void log(String text) {
        if (LOG_IS_ENABLED) Log.d(LOG_TAG, text);
    }
}
