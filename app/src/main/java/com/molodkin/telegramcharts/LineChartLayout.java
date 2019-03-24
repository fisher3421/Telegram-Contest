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

import java.util.ArrayList;

public class LineChartLayout extends FrameLayout {

    public LineChartView chartView;
    private ScrollChartView scrollChartView;
    private InfoView infoView;

    final int scrollHeight = Utils.dpToPx(this, 40);
    int chartHeight = Utils.dpToPx(this, 300);
    int checkboxHeight = Utils.dpToPx(this, 36);
    int checkboxTopMargin = Utils.dpToPx(this, 16);
    int sideMargin = Utils.dpToPx(this, 20);
    private ScrollBorderView scrollBorderView;

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

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

        infoView = new InfoView(getContext(), chartView);
        infoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight - chartView.xAxisHeight));

        scrollChartView = new ScrollChartView(getContext(), chartView);
        scrollBorderView = new ScrollBorderView(getContext(), chartView);

        LayoutParams scrollLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight);
        scrollLP.topMargin = chartHeight;
        scrollLP.leftMargin = sideMargin;
        scrollLP.rightMargin = sideMargin;
        scrollChartView.setLayoutParams(scrollLP);

        scrollBorderView.setLayoutParams(scrollLP);

        addView(chartView);
        addView(infoView);
        addView(scrollChartView);
        addView(scrollBorderView);
    }

    public void setData(ChartData data) {
        int height = chartHeight + scrollHeight + checkboxTopMargin;
        chartView.setData(data);
        for (int i = 0; i < data.names.size(); i++) {
            String name = data.names.get(i);
            String color = data.colors.get(i);
            CheckBox checkBox = (CheckBox) LayoutInflater.from(getContext()).inflate(R.layout.check_view, this, false);
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor(color)));
            checkBox.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
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
            ((MarginLayoutParams) checkBox.getLayoutParams()).topMargin = height;
            checkBoxes.add(checkBox);
            addView(checkBox);
            height += checkboxHeight;
        }
    }

    public void updateTheme() {
        chartView.updateTheme();
        scrollBorderView.updateTheme();
        infoView.updateTheme();
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        }
    }

}
