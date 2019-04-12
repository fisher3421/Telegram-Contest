package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

public final class StackPercentageChartView extends BaseChart {

    private float[] sums;

    public StackPercentageChartView(Context context) {
        super(context);
        enablingWithAlphaAnimation = false;
    }

    @Override
    public void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;
        sums = new float[xPoints.length];

        start = 0;
        end = data.x.length;

        visibleStart = 0;
        visibleEnd = data.x.length;

        graphs = new StackPercentageChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new StackPercentageChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - xAxisHeight;
        availableChartWidth = (float) getWidth() - sideMargin * 2;

        float scaleX = availableChartWidth / (xPoints.length - 1);

        yAxis1 = new StackPercentageYAxis(this, chartMatrix);
        yAxis1.isHalfLine = false;
        yAxis1.init();

        chartMatrix.postScale(scaleX, 1, 0, 0);

        xAxis.init(scaleX);

        buildRectangles();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initGraphs();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        canvas.translate(sideMargin, 0);

        canvas.save();

        drawData(canvas);

        if (yAxis1 != null) yAxis1.draw(canvas);

        canvas.restore();

        xAxis.draw(canvas);
    }

    private void buildRectangles() {

        for (int i = 0; i < xPoints.length; i++) {
            float sum = 0f;
            for (BaseChartGraph graph : graphs) {
                if (graph.alpha == 0) continue;
                sum += graph.values[i] * graph.alpha;
            }
            sums[i] = sum;
        }

        int maxValue = yAxis1.maxValue;

        for (int i = 0; i < xPoints.length; i++) {

            float top = maxValue;

            for (BaseChartGraph graph1 : graphs) {

                StackPercentageChartGraph graph = (StackPercentageChartGraph) graph1;
                if (graph.alpha == 0) continue;

                float value = 100 * graph.values[i] * graph.alpha / sums[i];
                float currentTop = top - value;

                if (i == 0) {
                    graph.path.reset();
                    graph.path.moveTo(i, maxValue);
                    graph.path.lineTo(i, currentTop);
                } else if (i == xPoints.length - 1) {
                    graph.path.lineTo(i, currentTop);
                    graph.path.lineTo(i, maxValue);
                    graph.path.lineTo(0, maxValue);
                } else {
                    graph.path.lineTo(i, currentTop);
                }

                top -= value;
            }
        }
    }

    @Override
    protected void graphAlphaChanged() {
        buildRectangles();
    }

    private void drawData(Canvas canvas) {
        for (int i = graphs.length - 1; i >= 0; i--) {
            BaseChartGraph graph = graphs[i];
            if (graph.alpha == 0) continue;
            graph.draw(canvas, chartMatrix, visibleStart, visibleEnd);
        }
    }


}
