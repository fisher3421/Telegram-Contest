package com.molodkin.telegramcharts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

public class LineChartLayout extends LinearLayout {

    private LineChartView chartView;

    public LineChartLayout(Context context) {
        super(context);
        init();
    }

    public LineChartLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.chart_layout, this);
        chartView = (LineChartView) getChildAt(0);

    }

    public void setData(ChartData data) {
        chartView.setData(data);
        for (int i = 0; i < data.names.size(); i++) {
            String name = data.names.get(i);
            String color = data.colors.get(i);
            CheckBox checkBox = (CheckBox) LayoutInflater.from(getContext()).inflate(R.layout.check_view, this, false);
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor(color)));
            checkBox.setText(name);
            checkBox.setChecked(true);
            final int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    chartView.enableGraph(finalI, isChecked);
                }
            });
            addView(checkBox);
        }
    }

}
