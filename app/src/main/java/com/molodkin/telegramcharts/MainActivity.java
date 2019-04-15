package com.molodkin.telegramcharts;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    ArrayList<ChartGroupLayout> charts = new ArrayList<>();
    private LinearLayout actionBarLayout;
    private TextView title;
    private ImageView switchDayNightIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<ChartData> data =null;

        data = new ArrayList<>();

        try {
//            data = DataProvider.getData(this);
            data.add(DataProvider.getData(this, R.raw.c1));
//            data.add(DataProvider.getData(this, R.raw.c2));
//            data.add(DataProvider.getData(this, R.raw.c3));
//            data.add(DataProvider.getData(this, R.raw.c4));
//            data.add(DataProvider.getData(this, R.raw.c5));
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        setContentView(R.layout.activity_main);

        title = findViewById(R.id.title);

        actionBarLayout = findViewById(R.id.actionBarLayout);

        LinearLayout root = findViewById(R.id.root);

        int layoutTopMargin = Utils.getDim(this, R.dimen.margin20);

        for (int i = 0; i < data.size(); i++) {
            ChartGroupLayout chartLayout = new ChartGroupLayout(this, i + 1);
//            ChartGroupLayout chartLayout = new ChartGroupLayout(this, 4);

            chartLayout.setPadding(0, layoutTopMargin, 0, 0);

            chartLayout.setData(data.get(i));
            chartLayout.setChartName(String.format("Chart %s", String.valueOf(i + 1)));

            charts.add(chartLayout);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = Utils.dpToPx(this, 32);
            root.addView(chartLayout, lp);
//            break;
        }

        switchDayNightIcon = findViewById(R.id.switchDayNightMode);
        switchDayNightIcon.setOnClickListener(new View.OnClickListener() {
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
        if (Utils.isDayMode) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            switchDayNightIcon.setColorFilter(Utils.getColor(this, R.color.iconLightColor));
        } else {
            getWindow().getDecorView().setSystemUiVisibility(0);
            switchDayNightIcon.setColorFilter(Utils.getColor(this, R.color.white));
        }

        getWindow().getDecorView().setBackgroundColor(Utils.getColor(MainActivity.this, Utils.WINDOW_BACKGROUND_COLOR));
        actionBarLayout.setBackgroundColor(Utils.getColor(MainActivity.this, Utils.PRIMARY_COLOR));
        title.setTextColor(Utils.getColor(this, Utils.PRIMARY_TEXT_COLOR));
        for (ChartGroupLayout chart : charts) {
            chart.setBackgroundColor(Utils.getColor(MainActivity.this, Utils.CHART_BACKGROUND_COLOR));
            chart.updateTheme();
        }

    }
}
