package com.molodkin.telegramcharts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class LineChartLayout extends FrameLayout {

    public LineChartView chartView;
    private RangeChartView rangeChartView;
    private InfoView infoView;

    final int scrollHeight = Utils.getDim(this, R.dimen.scrollHeight);
    int chartHeight = Utils.getDim(this, R.dimen.chartHeight);
    int checkboxHeight = Utils.getDim(this, R.dimen.checkboxHeight);
    int dividerHeight = Utils.dpToPx(this, 1);
    int checkboxDividerLeftMargin = Utils.getDim(this, R.dimen.checkboxDividerLeftMargin);
    int sideMargin = Utils.getDim(this, R.dimen.margin20);
    private RangeBorderView rangeBorderView;

    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<View> dividers = new ArrayList<>();

    private TextView chartNameView;

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

        rangeChartView = new RangeChartView(getContext(), chartView);
        rangeBorderView = new RangeBorderView(getContext(), chartView);

        LayoutParams scrollLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight);
        scrollLP.topMargin = chartHeight;
        scrollLP.leftMargin = sideMargin;
        scrollLP.rightMargin = sideMargin;
        rangeChartView.setLayoutParams(scrollLP);

        rangeBorderView.setLayoutParams(scrollLP);

        chartNameView = new TextView(getContext());
        chartNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getDim(this, R.dimen.chartNameTextSize));
        chartNameView.setTextColor(Utils.getColor(getContext(), R.color.chartName));
        chartNameView.setTypeface(Typeface.DEFAULT_BOLD);

        FrameLayout.LayoutParams chartNameLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chartNameLP.leftMargin = sideMargin;
        chartNameView.setLayoutParams(chartNameLP);

        addView(chartView);
        addView(chartNameView);
        addView(infoView);
        addView(rangeChartView);
        addView(rangeBorderView);
    }

    public void setChartName(String chartName) {
        chartNameView.setText(chartName);
    }

    public void setData(ChartData data) {
        int height = chartHeight + scrollHeight;// + checkboxTopMargin;
        chartView.setData(data);
        for (int i = 0; i < data.names.size(); i++) {
            String name = data.names.get(i);
            String color = data.colors.get(i);
            CheckBox checkBox = (CheckBox) LayoutInflater.from(getContext()).inflate(R.layout.check_view, this, false);
//            checkBox.setBackgroundColor(Color.parseColor(color));
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor(color)));
            checkBox.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
            checkBox.setText(name);
            checkBox.setChecked(true);
            final int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    chartView.enableGraph(finalI, isChecked);
                    rangeChartView.adjustYAxis();
                }
            });
            ((MarginLayoutParams) checkBox.getLayoutParams()).topMargin = height;
            checkBoxes.add(checkBox);
            addView(checkBox);
            height += checkboxHeight;

            if (i < data.names.size() - 1) {
                View divider = new View(getContext());
                divider.setBackgroundColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));

                FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight);
                lp.leftMargin = checkboxDividerLeftMargin;
                lp.topMargin = height;
                divider.setLayoutParams(lp);

                dividers.add(divider);
                addView(divider);
            }
        }
    }

    public void updateTheme() {
        chartView.updateTheme();
        rangeBorderView.updateTheme();
        infoView.updateTheme();
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        }
        for (View divider : dividers) {
            divider.setBackgroundColor(Utils.getColor(getContext(), Utils.AXIS_COLOR));
        }
    }

}
