package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;


public final class LineChartView extends BaseChart {

    public LineChartView(Context context) {
        super(context);
    }

    @Override
    public void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;

        start = 0;
        end = data.x.length;

        drawStart = 0;
        drawEnd = data.x.length;

        yAdjustStart = 0;
        yAdjustEnd = data.x.length;

        graphs = new LineChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new LineChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), graphLineWidth, data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - xAxisHeight;
        availableChartWidth = (float) getWidth() - sideMargin * 2;

        float scaleX = availableChartWidth / (xPoints.length - 1);

        if (!secondY) {
            yAxis1 = new LineYAxis(this, chartMatrix);
            yAxis1.isHalfLine = false;
            yAxis1.init();
        } else {
            yAxis1 = new LineYAxis(this, chartMatrix);
            yAxis1.isHalfLine = true;
            yAxis1.init();

            yAxis2 = new LineYAxis(this, chartMatrix2);
            yAxis2.isHalfLine = true;
            yAxis2.isRight = true;
            yAxis2.init();
        }

        chartMatrix.postScale(scaleX, 1, 0, 0);
        chartMatrix2.postScale(scaleX, 1, 0, 0);

        xAxis.init(scaleX);

        for (int i = 0; i < end - 1; i++) {
            for (int j = 0; j < graphs.length; j++) {
                LineChartGraph graph = (LineChartGraph) graphs[j];
                int k = i * 4;

                int maxYValue = yAxis2 != null && j == 1 ? yAxis2.maxValue : yAxis1.maxValue;
                float y1 = maxYValue - graph.values[i];

                graph.linePoints[k] = i;
                graph.linePoints[k + 1] = y1;
                graph.linePoints[k + 2] = (i + 1);
                graph.linePoints[k + 3] = maxYValue - graph.values[i + 1];
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

    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < graphs.length; i++) {
            BaseChartGraph graph = graphs[i];
            if (graph.alpha > 0) {
                if (secondY && i == 1) {
                    graph.draw(canvas, chartMatrix2, Math.max(drawStart, 0), Math.min(drawEnd, xPoints.length));
                } else {
                    graph.draw(canvas, chartMatrix, Math.max(drawStart, 0), Math.min(drawEnd, xPoints.length));
                }

            }
        }
    }

}
