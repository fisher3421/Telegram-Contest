package com.molodkin.telegramcharts;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private boolean isDayMode = true;
//    private LineChartView lineChartView;
    ArrayList<LineChartLayout> charts = new ArrayList<>();
    private LinearLayout actionBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<ChartData> data = null;
        try {
            data = DataProvider.getData(this);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        setContentView(R.layout.activity_main);

        actionBarLayout = findViewById(R.id.actionBarLayout);

        LinearLayout root = findViewById(R.id.root);

        for (ChartData chartData : data) {
            LineChartLayout chartLayout = new LineChartLayout(this);
            charts.add(chartLayout);
            chartLayout.setData(chartData);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            root.addView(chartLayout, lp);

        }
//        lineChartView = findViewById(R.id.chart);
//        chartLayout = findViewById(R.id.chart1);
//        chartLayout.setData(data.get(0));
//        chartLayout.setData(ChartData.buidFake());

        findViewById(R.id.switchDayNightMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                chartLayout.chartView.setEnd(chartLayout.chartView.end++);
//                chartLayout.chartView.invalidate();
                isDayMode = !isDayMode;
                updateMode();
            }
        });

        updateMode();
    }

    private void updateMode() {
        getWindow().setStatusBarColor(Utils.getColor(MainActivity.this, isDayMode ? R.color.colorPrimaryDark : R.color.colorPrimaryDark_dark));
        getWindow().getDecorView().setBackgroundColor(Utils.getColor(MainActivity.this, isDayMode ? R.color.window_background_day : R.color.window_background_night));
        actionBarLayout.setBackgroundColor(Utils.getColor(MainActivity.this, isDayMode ? R.color.colorPrimary : R.color.colorPrimary_dark));
        for (LineChartLayout chart : charts) {
            chart.setBackgroundColor(Utils.getColor(MainActivity.this, isDayMode ? R.color.chart_background_day : R.color.chart_background_night));
            chart.setDayMode(isDayMode);
        }

    }
}
