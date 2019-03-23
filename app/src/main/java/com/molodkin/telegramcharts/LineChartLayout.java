package com.molodkin.telegramcharts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

public class LineChartLayout extends FrameLayout {

    public LineChartView chartView;
    private ScrollChartView scrollChartView;

    final int scrollHeight = Utils.dpToPx(this, 40);
    int chartHeight = Utils.dpToPx(this, 300);
    int checkboxHeight = Utils.dpToPx(this, 40);

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
        chartView = new LineChartView(getContext());
        chartView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

        scrollChartView = new ScrollChartView(getContext(), chartView);
        ScrollBorderView scrollBorderView = new ScrollBorderView(getContext(), chartView);

        LayoutParams scrollLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight);
        scrollLP.topMargin = chartHeight;
        scrollChartView.setLayoutParams(scrollLP);

        scrollBorderView.setLayoutParams(scrollLP);

        addView(chartView);
        addView(scrollChartView);
        addView(scrollBorderView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setData(ChartData data) {
        int height = chartHeight + scrollHeight;
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
                    scrollChartView.adjustYAxis();
                }
            });
            LayoutParams lP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, checkboxHeight);
            lP.topMargin = height;
            addView(checkBox, lP);
            height += checkboxHeight;
        }
    }

}
