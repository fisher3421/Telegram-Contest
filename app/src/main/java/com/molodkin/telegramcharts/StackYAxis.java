package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Matrix;

import java.util.HashMap;

class StackYAxis extends BaseYAxis {

    private HashMap<String, int[][]> maxValuesMap = new HashMap<>();


    StackYAxis(Context context,  BaseChart chart, Matrix matrix) {
        super(context, chart, matrix);
    }

    @Override
    int getMaxValueFullRange() {
        return getMatrixValues()[0][chart.xPoints.length - 1];
    }

    @Override
    int getMaxValue() {
        return getMatrixValues()[chart.visibleStart][chart.visibleEnd - 1];
    }

    private int[][] getMatrixValues() {
        StringBuilder keyBuilder = new StringBuilder();
        for (BaseChartGraph graph : chart.graphs) {
            if (graph.isEnable) keyBuilder.append(graph.name);
        }
        String key = keyBuilder.toString();
        int[][] maxMatrix = maxValuesMap.get(key);
        if (maxMatrix == null) {
            int size = chart.graphs[0].values.length;
            maxMatrix = new int[size][size];
            for (int i = 0; i < size; i++) {
                maxMatrix[i][i] = getSum(i);
                for (int j = i + 1; j < size; j++) {
                    maxMatrix[i][j] = Math.max(maxMatrix[i][j - 1], getSum(j));
                }
            }
            maxValuesMap.put(key, maxMatrix);
        }
        return maxMatrix;
    }

    private int getSum(int index) {
        int sum = 0;
        for (BaseChartGraph graph : chart.graphs) {
            if (graph.isEnable) sum += graph.values[index];
        }
        return sum;
    }

    @Override
    public int getMinValue() {
        return 0;
    }


}
