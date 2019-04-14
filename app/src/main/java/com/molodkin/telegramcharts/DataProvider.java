package com.molodkin.telegramcharts;

import android.content.Context;
import android.util.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    static ChartData getData(Context context, int resId) throws IOException {
        InputStream raw = context.getResources().openRawResource(resId);
        JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(raw, "UTF8")));

        return parseChartData(reader);
    }

    private static ChartData parseChartData(JsonReader reader) throws IOException {
        reader.beginObject();

        ArrayList<Long> x = new ArrayList<>();
        ArrayList<ArrayList<Integer>> yList = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> colors = new ArrayList<>();

        boolean stacked = false;
        boolean percentage = false;
        boolean yScaled = false;

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "columns": {
                    reader.beginArray();

                    //x
                    reader.beginArray();
                    reader.skipValue();
                    while (reader.hasNext()) {
                        x.add(reader.nextLong());
                    }
                    reader.endArray();

                    //y
                    while (reader.hasNext()) {
                        ArrayList<Integer> y = new ArrayList<>(x.size());
                        reader.beginArray();
                        reader.skipValue();
                        while (reader.hasNext()) {
                            y.add(reader.nextInt());
                        }
                        reader.endArray();
                        yList.add(y);
                    }
                    reader.endArray();
                    break;
                }
                case "names": {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        reader.nextName();
                        names.add(reader.nextString());
                    }
                    reader.endObject();
                    break;
                }
                case "colors": {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        reader.nextName();
                        colors.add(reader.nextString());
                    }
                    reader.endObject();
                    break;
                }
                case "percentage" :
                    percentage = reader.nextBoolean();
                    break;
                case "stacked" :
                    stacked = reader.nextBoolean();
                    break;
                case "y_scaled" :
                    yScaled = reader.nextBoolean();
                    break;
                default: {
                    reader.skipValue();
                    break;
                }
            }
        }

        reader.endObject();

        ArrayList<int []> yPrimitives = new ArrayList<>(yList.size());
        for (ArrayList<Integer> integers : yList) {
            yPrimitives.add(toIntArray(integers));
        }

        ChartData.Type type;
        if (stacked) {
            type = percentage ? ChartData.Type.STACK_PERCENTAGE : ChartData.Type.STACK;
        } else {
            type = yScaled ? ChartData.Type.LINE_SCALED : ChartData.Type.LINE;
        }

        return new ChartData(toLongArray(x), yPrimitives, names, colors, type);
    }


    private static int [] toIntArray(List<Integer> list) {
        int [] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private static long [] toLongArray(List<Long> list) {
        long [] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}
