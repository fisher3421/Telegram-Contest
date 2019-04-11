package com.molodkin.telegramcharts;

import java.util.ArrayList;

class ChartData {

    enum Type {LINE, LINE_SCALED, STACK, STACK_PERCENTAGE}

    final long x[];
    final ArrayList<int []> values;
    final ArrayList<String> names;
    final ArrayList<String> colors;
    final Type type;

    ChartData(long[] x, ArrayList<int[]> values, ArrayList<String> names, ArrayList<String> colors, Type type) {
        this.x = x;
        this.values = values;
        this.names = names;
        this.colors = colors;
        this.type = type;
    }
}
