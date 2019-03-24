package com.molodkin.telegramcharts;

import java.util.ArrayList;

public class ChartData {

    final long x[];
    final ArrayList<int []> values;
    final ArrayList<String> names;
    final ArrayList<String> colors;

    public ChartData(long[] x, ArrayList<int[]> values, ArrayList<String> names, ArrayList<String> colors) {
        this.x = x;
        this.values = values;
        this.names = names;
        this.colors = colors;
    }
}
