package com.molodkin.telegramcharts;

public class ChartPoint implements Comparable<ChartPoint> {
    public final float value;
    public final String date;

    public ChartPoint(float value, String date) {
        this.value = value;
        this.date = date;
    }

    @Override
    public int compareTo(ChartPoint o) {
        return (int) (value - o.value);
    }
}
