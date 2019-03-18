package com.molodkin.telegramcharts;

class XAxisPoint {
    final int x;
    final String date;
    int alpha;
    final float width;

    public XAxisPoint(int x, String date, int alpha, float width) {
        this.x = x;
        this.date = date;
        this.alpha = alpha;
        this.width = width;
    }
}
