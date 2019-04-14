package com.molodkin.telegramcharts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.widget.FrameLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChartGroupLayout extends FrameLayout {

    ChartLayout chartLayout;
    ChartLayout zoomLayout;
    private ValueAnimator valueAnimator;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd", Locale.US);

    public ChartGroupLayout(Context context, final int number) {
        super(context);
        chartLayout = new ChartLayout(context, false);
        chartLayout.setChartListener(new ChartLayout.AbsChartListener() {
            @Override
            public void onDaySelected(long date) {
                ChartData zoomData;
                try {
                    String resName = String.format("c_%s_%s", String.valueOf(number), format.format(new Date(date)));
                    int resId = getContext().getResources().getIdentifier(resName, "raw", getContext().getPackageName());
                    zoomData = DataProvider.getData(getContext(), resId);
                } catch (IOException ignore) { return; }
                zoomLayout.setData(zoomData);
                zoomLayout.updateTheme();

                zoomLayout.setVisibility(VISIBLE);
                zoomLayout.setAlpha(0);

                valueAnimator = ValueAnimator.ofFloat(0f , 1f);
//                valueAnimator.setDuration(5000);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        chartLayout.setAlpha(1 - animation.getAnimatedFraction());
                        zoomLayout.setAlpha(animation.getAnimatedFraction());
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        chartLayout.setVisibility(GONE);
                    }
                });
                valueAnimator.start();

            }
        });
        zoomLayout = new ChartLayout(context, true);
        if (number == 4) {
            zoomLayout.isRangeViewVisible = false;
        }
        zoomLayout.setChartListener(new ChartLayout.AbsChartListener() {
            @Override
            public void onZoomOut() {
                chartLayout.setVisibility(VISIBLE);
                chartLayout.setAlpha(0);
                valueAnimator = ValueAnimator.ofFloat(0f , 1f);
//                valueAnimator.setDuration(5000);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        chartLayout.setAlpha(animation.getAnimatedFraction());
                        zoomLayout.setAlpha(1 - animation.getAnimatedFraction());
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        zoomLayout.setVisibility(GONE);
                    }
                });
                valueAnimator.start();
            }
        });
        addView(chartLayout);
        addView(zoomLayout);
    }

    public void setData(ChartData data) {
        chartLayout.setData(data);
    }

    public void updateTheme() {
        chartLayout.updateTheme();
        zoomLayout.updateTheme();
    }

    public void setChartName(String format) {
        chartLayout.setChartName(format);
    }
}
