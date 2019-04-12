package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

public final class StackChartView extends BaseChart {

    public StackChartView(Context context) {
        super(context);
        enablingWithAlphaAnimation = false;
    }

    @Override
    public void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;

        start = 0;
        end = data.x.length;

        visibleStart = 0;
        visibleEnd = data.x.length;

        graphs = new StackChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new StackChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), graphLineWidth, data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - xAxisHeight;
        availableChartWidth = (float) getWidth() - sideMargin * 2;

        float scaleX = availableChartWidth / (xPoints.length - 1);

        yAxis1 = new StackYAxis(this, chartMatrix);
        yAxis1.isHalfLine = false;
        yAxis1.init();

        chartMatrix.postScale(scaleX, 1, 0, 0);

        xAxis.init(scaleX);

        buildLines();

        float strokeWidth = availableChartWidth / (xPoints.length - 1);
        for (BaseChartGraph graph1 : graphs) {
            StackChartGraph graph = (StackChartGraph) graph1;
            graph.scrollPaint.setStrokeWidth(strokeWidth);
        }
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

        canvas.clipRect(-sideMargin, 0, availableChartWidth + sideMargin, availableChartHeight + clipMargin);

        drawPoints(canvas);

        if (yAxis1 != null) yAxis1.draw(canvas);
        if (yAxis2 != null) yAxis2.draw(canvas);

        canvas.restore();

        xAxis.draw(canvas);
    }

    private void buildLines() {

        float top;

        for (int i = 0; i < xPoints.length; i++) {
            top = yAxis1.maxValue;
            for (BaseChartGraph graph : graphs) {

                if (graph.alpha == 0) continue;

                int k = i * 4;

                float value = graph.values[i] * graph.alpha;

                graph.points[k] = i;
                graph.points[k + 1] = top;
                graph.points[k + 2] = i;
                top -= value;
                graph.points[k + 3] = top;
            }
        }
    }

    @Override
    protected void graphAlphaChanged() {
        buildLines();
    }

    private void drawPoints(Canvas canvas) {

        float currentWidth = xCoordByIndex(end) - xCoordByIndex(start);
        float strokeWidth = currentWidth / (end - start - 1);
        for (BaseChartGraph graph1 : graphs) {
            StackChartGraph graph = (StackChartGraph) graph1;
            graph.paint.setStrokeWidth(strokeWidth);
        }

        for (BaseChartGraph graph : graphs) {
            if (graph.alpha == 0) continue;
            graph.draw(canvas, chartMatrix, visibleStart, visibleEnd);
        }
    }



}
