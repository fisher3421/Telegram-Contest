package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.Collections;

class LineYAxis extends BaseYAxis {

    LineYAxis(Context context, BaseChart chart, Matrix matrix) {
        super(context, chart, matrix);
    }

    @Override
    public void adjustYAxis(boolean init) {
        if (isHalfLine) {
            if (!chart.graphs[isRight ? 1 : 0].isEnable) return;
        }
        super.adjustYAxis(init);
    }

    @Override
    int getMaxValue() {
        if (isHalfLine) {
            if (!isRight) {
                return getMaxValue(0);
            } else {
                return getMaxValue(1);
            }
        } else {
            return getMaxValueAll();
        }
    }

    @Override
    int getMaxValueFullRange() {
        return getMaxValueAll(0, chart.xPoints.length);
    }

    private int getMaxValue(int graphIndex) {
        if (!chart.graphs[graphIndex].isEnable) {
            return maxYValueTemp;
        }
        return chart.graphs[graphIndex].getMax(chart.visibleStart, chart.visibleEnd);
    }

    private int getMaxValueAll() {
        int value = getMaxValueAll(chart.visibleStart, chart.visibleEnd);
        if (value == -1) {
            return maxYValueTemp;
        } else {
            return value;
        }
    }

    private int getMaxValueAll(int start, int end) {
        ArrayList<Integer> maxValues = new ArrayList<>(chart.graphs.length);
        for (BaseChartGraph graph : chart.graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(start, end));
        }

        if (maxValues.size() == 0) {
            return -1;
        } else {
            return Collections.max(maxValues);
        }
    }

    private int getMinValue(int index) {
        if (!chart.graphs[index].isEnable) {
            return minYValueTemp;
        }

        return chart.graphs[index].getMin(chart.visibleStart, chart.visibleEnd);
    }

    private int getMinValueAll() {
        ArrayList<Integer> minValues = new ArrayList<>(chart.graphs.length);
        for (BaseChartGraph graph : chart.graphs) {
            if (graph.isEnable) minValues.add(graph.getMin(chart.visibleStart, chart.visibleEnd));
        }

        if (minValues.size() == 0) {
            return maxYValueTemp;
        } else {
            return Collections.min(minValues);
        }
    }

    @Override
    public int getMinValue() {
        if (isHalfLine) {
            if (!isRight) {
                return getMinValue(0);
            } else {
                return getMinValue(1);
            }
        } else {
            return getMinValueAll();
        }
    }


}
