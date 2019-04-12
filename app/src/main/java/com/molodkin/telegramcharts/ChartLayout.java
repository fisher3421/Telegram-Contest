package com.molodkin.telegramcharts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static com.molodkin.telegramcharts.ChartData.Type.STACK_PERCENTAGE;

@SuppressLint("ViewConstructor")
public class ChartLayout extends FrameLayout {

    public BaseChart chartView;
    private RangeChartView rangeChartView;
    private BaseInfoView infoView;

    final int scrollHeight = Utils.getDim(this, R.dimen.scrollHeight);
    int chartHeight = Utils.getDim(this, R.dimen.chartHeight);
    int sideMargin = Utils.getDim(this, R.dimen.margin20);
    int checkBoxMargin = Utils.dpToPx(this, 8);
    private RangeBorderView rangeBorderView;

    private ArrayList<TCheckBox> checkBoxes = new ArrayList<>();
    private ArrayList<View> dividers = new ArrayList<>();

    private TextView chartNameView;
    private ChartData data;

    public ChartLayout(Context context) {
        super(context);
    }

    private void init() {
        switch (data.type) {
            case LINE:
            case LINE_SCALED:
                chartView = new LineChartView(getContext());
                chartView.secondY = data.type == ChartData.Type.LINE_SCALED;
                infoView = new LineInfoView(getContext(), chartView);
                break;
            case STACK:
                chartView = new StackChartView(getContext());
                infoView = new StackInfoView(getContext(), chartView);
                break;
            case STACK_PERCENTAGE:
                chartView = new StackPercentageChartView(getContext());
                infoView = new StackPercentageInfoView(getContext(), chartView);
                infoView.showPercentage = true;
                break;
        }

        chartView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

        infoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight - chartView.xAxisHeight));

        rangeChartView = new RangeChartView(getContext(), chartView);
        if (data.type == STACK_PERCENTAGE) {
            rangeChartView.addTopMargin = false;
            rangeChartView.yScale = 125f/100;
            rangeChartView.translateY = 25;
        }
        rangeBorderView = new RangeBorderView(getContext(), chartView);

        LayoutParams scrollLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight);
        scrollLP.topMargin = chartHeight;
        scrollLP.leftMargin = sideMargin;
        scrollLP.rightMargin = sideMargin;
        scrollLP.bottomMargin = sideMargin;
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

        chartView.setData(data);

        initCheckboxes();
    }

    private void initCheckboxes() {

        if (data.names.size()  < 2) return;

        for (int i = 0; i < data.names.size(); i++) {
            String name = data.names.get(i);
            String color = data.colors.get(i);
            final TCheckBox checkBox = (TCheckBox) LayoutInflater.from(getContext()).inflate(R.layout.check_view, this, false);
            checkBox.setColor(Color.parseColor(color));
            checkBox.setText(name);
            checkBox.setChecked(true);
            final int finalI = i;
            checkBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    chartView.enableGraph(finalI, checkBox.isChecked());
                    rangeChartView.adjustYAxis();
                }
            });

            checkBoxes.add(checkBox);
            addView(checkBox);
        }
    }

    public void setChartName(String chartName) {
        chartNameView.setText(chartName);
    }

    public void setData(ChartData data) {
        this.data = data;
        init();
    }

    public void updateTheme() {
        chartView.updateTheme();
        rangeBorderView.updateTheme();
        infoView.updateTheme();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (checkBoxes.size()  < 2) return;

        int top = chartHeight + scrollHeight + sideMargin * 3 + checkBoxes.get(0).getMeasuredHeight();
        int left = sideMargin;

        for (TCheckBox checkBox : checkBoxes) {
            if (left + checkBoxMargin * 2 + checkBox.getMeasuredWidth() > getMeasuredWidth()) {
                top += checkBox.getMeasuredHeight() + checkBoxMargin;
                left = sideMargin + checkBox.getMeasuredWidth() + checkBoxMargin;
            } else {
                left += sideMargin + checkBox.getMeasuredWidth();
            }
        }

        setMeasuredDimension(getMeasuredWidth(), MeasureSpec.makeMeasureSpec(top, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (checkBoxes.size()  < 2) return;

        int topChild = chartHeight + scrollHeight + sideMargin * 2;
        int leftChild = sideMargin;

        for (TCheckBox checkBox : checkBoxes) {
            if (leftChild + checkBoxMargin * 2 + checkBox.getMeasuredWidth() > right) {
                topChild += checkBox.getMeasuredHeight() + checkBoxMargin;
                leftChild = sideMargin;
                checkBox.layout(leftChild, topChild, leftChild + checkBox.getMeasuredWidth(), topChild + checkBox.getMeasuredHeight());
                leftChild += checkBox.getMeasuredWidth() + checkBoxMargin;
            } else {
                checkBox.layout(leftChild, topChild, leftChild + checkBox.getMeasuredWidth(), topChild + checkBox.getMeasuredHeight());
                leftChild += checkBoxMargin + checkBox.getMeasuredWidth();
            }
        }
    }
}
