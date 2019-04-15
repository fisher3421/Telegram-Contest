package com.molodkin.telegramcharts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
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
    private RangeBorderView rangeBorderView;

    final int scrollHeight = Utils.getDim(this, R.dimen.scrollHeight);
    final int scrollPadding = Utils.getDim(this, R.dimen.range_top_bottom_padding);
    int chartHeight = Utils.getDim(this, R.dimen.chartHeight);
    int sideMargin = Utils.getDim(this, R.dimen.margin20);
    int checkBoxMargin = Utils.dpToPx(this, 8);
    boolean isRangeViewVisible = true;

    ArrayList<TCheckBox> checkBoxes = new ArrayList<>();

    private TextView chartNameView;
    private TextView zoomOutView;
    private DateTextView dateView;
    private ChartData data;

    private ChartListener chartListener;
    private final boolean isZoomed;

    public ChartLayout(Context context, boolean isZoomed) {
        super(context);
        this.isZoomed = isZoomed;
    }

    public void setChartListener(ChartListener selectDayListener) {
        this.chartListener = selectDayListener;
    }

    private void init() {
        switch (data.type) {
            case LINE:
            case LINE_SCALED:
                chartView = new LineChartView(getContext(), isZoomed);
                chartView.secondY = data.type == ChartData.Type.LINE_SCALED;
                infoView = new LineInfoView(getContext(), chartView);
                break;
            case STACK:
                chartView = new StackChartView(getContext(), isZoomed);
                infoView = new StackInfoView(getContext(), chartView);
                infoView.showAll = true;
                break;
            case STACK_PERCENTAGE:
                if (!isZoomed) {
                    chartView = new StackPercentageChartView(getContext());
                    infoView = new StackPercentageInfoView(getContext(), chartView);
                    infoView.showPercentage = true;
                } else {
                    chartView = new StackPercentagePieChartView(getContext());
                }

                break;
        }

        LayoutParams chartLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight);
        chartView.setLayoutParams(chartLp);

        if (infoView != null) infoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight - chartView.xAxisHeight));



        dateView = new DateTextView(getContext());

        FrameLayout.LayoutParams dateViewLP = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateViewLP.gravity = Gravity.END;
        dateViewLP.topMargin = Utils.dpToPx(this, 4);
        dateViewLP.rightMargin = sideMargin;
        dateView.setLayoutParams(dateViewLP);

        addView(chartView);



        if (isZoomed) {
            zoomOutView = new TextView(getContext());
            zoomOutView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getDim(this, R.dimen.chartNameTextSize));
            zoomOutView.setTextColor(Utils.getColor(getContext(), R.color.chartName));
            zoomOutView.setTypeface(Typeface.DEFAULT_BOLD);
            zoomOutView.setText(R.string.zoom_out);
            Drawable drawable = getContext().getDrawable(R.drawable.baseline_zoom_out_black_24);
            drawable.setColorFilter(Utils.getColor(getContext(), R.color.chartName), PorterDuff.Mode.SRC_ATOP);
            zoomOutView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            zoomOutView.setGravity(Gravity.TOP);
            zoomOutView.setCompoundDrawablePadding(Utils.dpToPx(this, 4));
            zoomOutView.setPadding(0, Utils.dpToPx(this, 2), 0, 0);

            FrameLayout.LayoutParams zoomOutLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            zoomOutLP.leftMargin = sideMargin;
            zoomOutView.setLayoutParams(zoomOutLP);

            addView(zoomOutView);
        } else {
            chartNameView = new TextView(getContext());
            chartNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getDim(this, R.dimen.chartNameTextSize));
            chartNameView.setTextColor(Utils.getColor(getContext(), R.color.chartName));
            chartNameView.setTypeface(Typeface.DEFAULT_BOLD);

            FrameLayout.LayoutParams chartNameLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chartNameLP.leftMargin = sideMargin;
            chartNameLP.topMargin = Utils.dpToPx(this, 2);
            chartNameView.setLayoutParams(chartNameLP);
            addView(chartNameView);
        }


        addView(dateView);
        if (infoView != null) addView(infoView);
        if (isRangeViewVisible) {
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

            addView(rangeChartView);
            addView(rangeBorderView);
            rangeBorderView.setOnRangeChanged(new RangeBorderView.OnRangeChanged() {
                @Override
                public void onChanged(int start, int end) {
                    if (infoView != null) infoView.move();
                    dateView.updateDate(chartView.xPoints[start], chartView.xPoints[end - 1]);
                }
            });
        }

        chartView.setData(data);

        initCheckboxes();

        chartView.setChartListener(new BaseChart.AbsChartListenr() {
            @Override
            public void updateInfoView() {
                if (infoView != null) infoView.move();
            }
        });

        if (infoView != null) infoView.setZoomInListenr(new BaseInfoView.ZoomInListenr() {
            @Override
            public void zoomIn(long date) {
                chartListener.onDaySelected(date);
            }
        });

        if (zoomOutView != null) {
            zoomOutView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    chartListener.onZoomOut();
//                chartView.zoom(400, true);
//                zoomChartView.zoom(100, false);
//                infoView.setChartView(chartView);
                }
            });
        }
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
            checkBox.setLongClickListener(new TCheckBox.CheckBoxListener() {
                @Override
                public void onClick() {
                    chartView.enableGraph(finalI);
                    if (rangeChartView != null) rangeChartView.adjustYAxis();
                }

                @Override
                public void onLongClick() {
                    for (TCheckBox box : checkBoxes) {
                        if (checkBox != box) box.setChecked(false);
                    }
                    chartView.enableAll(false, finalI, true);
                    if (rangeChartView != null) rangeChartView.adjustYAxis();
                }
            });

            checkBoxes.add(checkBox);
            addView(checkBox);
        }
    }

    public void setChartName(String chartName) {
        if (chartNameView != null) chartNameView.setText(chartName);
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
        if (rangeBorderView != null) rangeBorderView.updateTheme();
        if (infoView != null) infoView.updateTheme();
        if (chartNameView != null) chartNameView.setTextColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        dateView.updateTheme();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (checkBoxes.size()  < 2) return;

        int top = chartHeight + scrollHeight + scrollPadding * 2 + sideMargin * 2 + checkBoxes.get(0).getMeasuredHeight();
        if (!isRangeViewVisible) {
            top -= scrollHeight + scrollPadding * 2 + sideMargin;
        }
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

        int topChild = chartHeight + scrollHeight + sideMargin + scrollPadding * 2;
        if (!isRangeViewVisible) {
            topChild -= scrollHeight + scrollPadding * 2 + sideMargin;
        }
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
        void onDaySelected(long date);
        void onZoomOut();
    }

    public abstract static class AbsChartListener implements ChartListener {
        @Override
        public void onDaySelected(long date) {

        }

        @Override
        public void onZoomOut() {

        }
    }
}
