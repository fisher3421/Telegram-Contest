package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class LineChart extends View {


    private ChartGraph [] graphs;

    private long [] xPoints = ChartData.X;


    private int start = 0;
    private int end = xPoints.length;

    private int maxYValue;

    private int rowNumber = 6;

    private String [] yAxesTexts = new String[rowNumber];

    private int scrollHeight = Utils.dpToPx(this, 40);
    private int xAxisHeight = Utils.dpToPx(this, 33);
    private int xAxisWidth = Utils.dpToPx(this, 1);

    private int graphWidth = Utils.dpToPx(this, 2);

    private float availableHeight;

    private int textSize = Utils.spToPx(this.getContext(), 14);
    private int xTextMargin = Utils.dpToPx(this, 2);

    private Paint axisPaint = new Paint();
    private TextPaint axisTextPaint = new TextPaint();

    private float rowHeight;
    private float stepX;

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

        ArrayList<Integer> maxValues = new ArrayList<>(graphs.length);
        for (ChartGraph graph : graphs) {
            maxValues.add(graph.getMax(start, end));
        }

        maxYValue = Collections.max(maxValues);

        int rowStep = (int) (Math.ceil(maxYValue * 1f / rowNumber));

        for (int i = 0; i < rowNumber; i++) {
            yAxesTexts[i] = String.valueOf(rowStep * i);
        }

        float koeffY = availableHeight / maxYValue;
        stepX = ((float) getWidth()) / (xPoints.length - 1);

        for (ChartGraph graph : graphs) {
            graph.path.reset();
            graph.path.moveTo(0, availableHeight - graph.values[0] * koeffY);
        }

        for (int i = 1; i < end; i++) {
            for (ChartGraph graph : graphs) {
                graph.path.lineTo(i * stepX, availableHeight - graph.values[i] * koeffY);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        drawXAxes(canvas);

        drawPoints(canvas);
    }

    private void drawXAxes(Canvas canvas) {

        canvas.save();

        canvas.translate(0f, availableHeight);

        for (int i = 0; i < rowNumber; i++) {
            canvas.drawLine(0f, 0f, getWidth(), 0f, axisPaint);

            canvas.save();

            canvas.translate(0f, -xTextMargin);

            canvas.drawText(yAxesTexts[i], 0, 0, axisTextPaint);

            canvas.restore();

            canvas.translate(0f, -rowHeight);
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
        invalidate();
    }

    public void setEnd(int end) {
        if (end > xPoints.length) return;
        if (end <= start) return;

//        matrix.reset();
        float toScale = xPoints.length / (end - start * 1f);
        startScaleAnimation(toScale, false);

        this.end = end;
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

}
