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

    ArrayList<LineChartLayout> charts = new ArrayList<>();
    private LinearLayout actionBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<ChartData> data = null;

        data = new ArrayList<>();

        try {
            data = DataProvider.getData(this);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        setContentView(R.layout.activity_main);

        actionBarLayout = findViewById(R.id.actionBarLayout);

        LinearLayout root = findViewById(R.id.root);

        int layoutTopMargin = Utils.getDim(this, R.dimen.margin20);

        for (int i = 0; i < data.size(); i++) {
            LineChartLayout chartLayout = new LineChartLayout(this);
            chartLayout.setPadding(0, layoutTopMargin, 0, 0);
            charts.add(chartLayout);
            chartLayout.setChartName(String.format("Chart %s", String.valueOf(i + 1)));
            chartLayout.setData(data.get(i));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i != 0) lp.topMargin = Utils.dpToPx(this, 32);
            root.addView(chartLayout, lp);
        }

        findViewById(R.id.switchDayNightMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.isDayMode = !Utils.isDayMode;
                updateMode();
            }
        });

        updateMode();
    }

    private void updateMode() {
        getWindow().setStatusBarColor(Utils.getColor(MainActivity.this, Utils.PRIMARY_DARK_COLOR));
        getWindow().getDecorView().setBackgroundColor(Utils.getColor(MainActivity.this, Utils.WINDOW_BACKGROUND_COLOR));
        actionBarLayout.setBackgroundColor(Utils.getColor(MainActivity.this, Utils.PRIMARY_COLOR));
        for (LineChartLayout chart : charts) {
            chart.setBackgroundColor(Utils.getColor(MainActivity.this, Utils.CHART_BACKGROUND_COLOR));
            chart.updateTheme();
        }

    }
}
