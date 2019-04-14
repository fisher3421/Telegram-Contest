package com.molodkin.telegramcharts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.molodkin.telegramcharts.ChartData.Type.STACK_PERCENTAGE;

@SuppressLint("ViewConstructor")
public class ChartLayout extends FrameLayout {

    public BaseChart chartView;
    private RangeChartView rangeChartView;
    private BaseInfoView infoView;
    private RangeBorderView rangeBorderView;

    final int scrollHeight = Utils.getDim(this, R.dimen.scrollHeight);
    final int scrollPadding = Utils.getDim(this, R.dimen.range_top_bottom_padding);
    int chartHeight = Utils.getDim(this, R.dimen.chartHeight);
    int sideMargin = Utils.getDim(this, R.dimen.margin20);
    int checkBoxMargin = Utils.dpToPx(this, 8);

    private Date tempDate = new Date();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Utils.getLocale(getContext()));

    private ArrayList<TCheckBox> checkBoxes = new ArrayList<>();

    private TextView chartNameView;
    private TextView dateView;
    private ChartData data;

    private ChartListener chartListener;

    public ChartLayout(Context context) {
        super(context);
    }

    public void setChartListener(ChartListener selectDayListener) {
        this.chartListener = selectDayListener;
    }

    private void init() {
        switch (data.type) {
            case LINE:
            case LINE_SCALED:
                chartView = new LineChartView(getContext());
                chartView.secondY = data.type == ChartData.Type.LINE_SCALED;
                chartView.isBig = true;
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

        LayoutParams chartLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight);
        chartView.setLayoutParams(chartLp);

        infoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight - chartView.xAxisHeight));

        rangeChartView = new RangeChartView(getContext(), chartView);
        rangeChartView.setBackground(getContext().getDrawable(R.drawable.bg_range));
        rangeChartView.setClipToOutline(true);
        if (data.type == STACK_PERCENTAGE) {
            rangeChartView.addTopMargin = false;
            rangeChartView.yScale = 125f/100;
            rangeChartView.translateY = 25;
        }
        rangeBorderView = new RangeBorderView(getContext(), chartView);

        LayoutParams scrollLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight);
        scrollLP.topMargin = chartHeight + scrollPadding;
        scrollLP.leftMargin = sideMargin;
        scrollLP.rightMargin = sideMargin;
        scrollLP.bottomMargin = sideMargin;
        rangeChartView.setLayoutParams(scrollLP);

        LayoutParams scrollBorderLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, scrollHeight + scrollPadding * 2);
        scrollBorderLP.topMargin = chartHeight;
        scrollBorderLP.leftMargin = sideMargin;
        scrollBorderLP.rightMargin = sideMargin;
        scrollBorderLP.bottomMargin = sideMargin;

        rangeBorderView.setLayoutParams(scrollBorderLP);

        chartNameView = new TextView(getContext());
        chartNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getDim(this, R.dimen.chartNameTextSize));
        chartNameView.setTextColor(Utils.getColor(getContext(), R.color.chartName));
        chartNameView.setTypeface(Typeface.DEFAULT_BOLD);

        FrameLayout.LayoutParams chartNameLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chartNameLP.leftMargin = sideMargin;
        chartNameView.setLayoutParams(chartNameLP);

        dateView = new TextView(getContext());
        dateView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getDim(this, R.dimen.chartDateTextSize));
        dateView.setGravity(Gravity.END);
        dateView.setTextColor(Utils.getColor(getContext(), R.color.chartName));
        dateView.setTypeface(Typeface.DEFAULT_BOLD);

        FrameLayout.LayoutParams dateViewLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateViewLP.gravity = Gravity.END;
        dateViewLP.topMargin = Utils.dpToPx(this, 2);
        dateViewLP.rightMargin = sideMargin;
        dateView.setLayoutParams(dateViewLP);

        addView(chartView);
        addView(chartNameView);
        addView(dateView);
        addView(infoView);
        addView(rangeChartView);
        addView(rangeBorderView);

        chartView.setData(data);

        initCheckboxes();

        rangeBorderView.setOnRangeChanged(new RangeBorderView.OnRangeChanged() {
            @Override
            public void onChanged(int start, int end) {
                long dateStartMills = chartView.xPoints[start];
                tempDate.setTime(dateStartMills);
                String dateStartStr = dateFormat.format(tempDate);
                long dateEndMills = chartView.xPoints[end - 1];
                tempDate.setTime(dateEndMills);
                String dateEndStr = dateFormat.format(tempDate);
                dateView.setText(String.format("%s - %s", dateStartStr, dateEndStr));
            }
        });

        infoView.setZoomInListenr(new BaseInfoView.ZoomInListenr() {
            @Override
            public void zoomIn(float x) {
            }
        });

        dateView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChartData zoomData;
                try {
                    zoomData = DataProvider.getData(getContext(), R.raw.c_1_2018_04_07);
                } catch (IOException ignore) { return; }
                chartListener.onZoomOut();
//                chartView.zoom(400, false);
//                infoView.setChartView(zoomChartView);
//                zoomChartView.setData(zoomData);
//                zoomChartView.zoom(100, true);
            }
        });

        chartNameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chartListener.onDaySelected();
//                chartView.zoom(400, true);
//                zoomChartView.zoom(100, false);
//                infoView.setChartView(chartView);
            }
        });
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
        if (chartView == null) {
            init();
        } else {
            chartView.setData(data);
        }
    }

    public void updateTheme() {
        if (chartView == null) return;
        chartView.updateTheme();
        rangeBorderView.updateTheme();
        infoView.updateTheme();
        chartNameView.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        dateView.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (checkBoxes.size()  < 2) return;

        int top = chartHeight + scrollHeight + scrollPadding * 2 + sideMargin * 3 + checkBoxes.get(0).getMeasuredHeight();
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

        int topChild = chartHeight + scrollHeight + sideMargin * 2 + scrollPadding * 2;
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

    public interface ChartListener {
        void onDaySelected();
        void onZoomOut();
    }

    public abstract static class AbsChartListener implements ChartListener {
        @Override
        public void onDaySelected() {

        }

        @Override
        public void onZoomOut() {

        }
    }
}
