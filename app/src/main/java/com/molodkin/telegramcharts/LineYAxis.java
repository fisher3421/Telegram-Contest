package com.molodkin.telegramcharts;

import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.Collections;

class LineYAxis extends BaseYAxis {

    LineYAxis(BaseChart chart, Matrix matrix) {
        super(chart, matrix);
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

    private int getMaxValue(int graphIndex) {
        if (!chart.graphs[graphIndex].isEnable) {
            return maxYValueTemp;
        }
        return chart.graphs[graphIndex].getMax(chart.start, chart.end);
    }

    private int getMaxValueAll() {
        ArrayList<Integer> maxValues = new ArrayList<>(chart.graphs.length);
        for (BaseChartGraph graph : chart.graphs) {
            if (graph.isEnable) maxValues.add(graph.getMax(chart.start, chart.end));
        }

        if (maxValues.size() == 0) {
            return maxYValueTemp;
        } else {
            return Collections.max(maxValues);
        }
    }

    private int getMinValue(int index) {
        if (!chart.graphs[index].isEnable) {
            return minYValueTemp;
        }

        return chart.graphs[index].getMin(chart.start, chart.end);
    }

    private int getMinValueAll() {
        ArrayList<Integer> minValues = new ArrayList<>(chart.graphs.length);
        for (BaseChartGraph graph : chart.graphs) {
            if (graph.isEnable) minValues.add(graph.getMin(chart.start, chart.end));
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
