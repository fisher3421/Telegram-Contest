package com.molodkin.telegramcharts;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ChartGraph {

    private final TreeMap<Integer, Integer> treeMap = new TreeMap<>(Collections.<Integer>reverseOrder());

    public final int [] values;

    public final Paint paint;

    public boolean isEnable = true;

    public final Matrix matrix = new Matrix();

    public Path path = new Path();

    public int getMax(int start, int end) {
        for (Map.Entry<Integer, Integer> entry : treeMap.entrySet()) {
            if (entry.getValue() >= start && entry.getValue() < end) {
                return entry.getKey();
            }
        }
        return 0;
    }

    public ChartGraph(int[] values, int color, float width) {
        this.values = values;

        for (int i = 0; i < values.length; i++) {
            treeMap.put(values[i], i);
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(color);
    }

    public void draw(Canvas canvas) {
        Path transform = new Path(path);

        transform.transform(matrix);

        canvas.drawPath(transform, paint);
    }
}
