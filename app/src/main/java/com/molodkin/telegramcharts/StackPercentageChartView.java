package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public final class StackPercentageChartView extends BaseChart {

    private float[] sums;

    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Paint paint = new Paint();
    private float topOffset;
    private Paint sideRectanglePaint;

    public StackPercentageChartView(Context context) {
        super(context, true);
        enablingWithAlphaAnimation = false;
    }

    @Override
    public void initTheme() {
        super.initTheme();
        sideRectanglePaint = new Paint();
        sideRectanglePaint.setStyle(Paint.Style.FILL);
        sideRectanglePaint.setColor(Utils.getColor(getContext(), Utils.CHART_BACKGROUND_COLOR));
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

        topOffset = 25 * availableChartHeight / 125;

        float scaleX = availableChartWidth / (xPoints.length - 1);

        yAxis1 = new StackPercentageYAxis(this, chartMatrix);
        yAxis1.isHalfLine = false;
        yAxis1.init();

        bitmap = Bitmap.createBitmap((int) availableChartWidth + sideMargin * 2, (int) (availableChartHeight - topOffset), Bitmap.Config.RGB_565);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.translate(0, -topOffset);

        chartMatrix.postScale(scaleX, 1, 0, 0);

        xAxis.init();

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

        bitmapCanvas.save();
        bitmapCanvas.translate(sideMargin, 0);
        for (int i = graphs.length - 1; i >= 0; i--) {
            BaseChartGraph graph = graphs[i];
            if (graph.alpha == 0) continue;
            graph.draw(bitmapCanvas, chartMatrix, visibleStart, visibleEnd);
        }
        bitmapCanvas.restore();
        canvas.drawBitmap(bitmap, -sideMargin, topOffset, paint);


        float startCoord = xCoordByIndex(0);
        float endCoord = xCoordByIndex(xPoints.length - 1);

        if (startCoord > 0) {
            canvas.drawRect(-sideMargin, 0, startCoord - sideMargin, availableChartHeight, sideRectanglePaint);
        }

        if (endCoord < getWidth()) {
            canvas.drawRect(endCoord - sideMargin, 0, endCoord, availableChartHeight, sideRectanglePaint);
        }
    }
}
