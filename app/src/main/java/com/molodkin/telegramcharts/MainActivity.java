package com.molodkin.telegramcharts;


import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    private boolean isDayMode = false;
//    private LineChartView lineChartView;
    private ViewGroup chartLayout;
    private LinearLayout actionBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        List<ChartData> data = null;
        try {
            data = DataProvider.getData(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        actionBarLayout = findViewById(R.id.actionBarLayout);
//        lineChartView = findViewById(R.id.chart);
        LineChartLayout chartLayout = findViewById(R.id.chart1);
        chartLayout.setData(data.get(0));

        findViewById(R.id.switchDayNightMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
//        chartLayout.setBackgroundColor(Utils.getColor(MainActivity.this, isDayMode ? R.color.chart_background_day : R.color.chart_background_night));
//        lineChartView.setDayMode(isDayMode);
    }
}
