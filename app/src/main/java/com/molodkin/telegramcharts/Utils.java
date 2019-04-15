package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;

class Utils {

    static boolean isDayMode = true;

    static boolean DEBUG = false;

    private static HashMap<String, Integer> dayResource = new HashMap<>();
    private static HashMap<String, Integer> nightResource = new HashMap<>();

    static final String PRIMARY_COLOR = "PRIMARY_COLOR";
    static final String PRIMARY_TEXT_COLOR = "PRIMARY_TEXT_COLOR";
    static final String PRIMARY_DARK_COLOR = "PRIMARY_DARK_COLOR";
    static final String CHART_BACKGROUND_COLOR = "CHART_BACKGROUND_COLOR";
    static final String WINDOW_BACKGROUND_COLOR = "WINDOW_BACKGROUND_COLOR";
    static final String AXIS_COLOR = "AXIS_COLOR";
    static final String AXIS_TEXT_COLOR = "AXIS_TEXT_COLOR";
    static final String SCROLL_COVER_COLOR = "SCROLL_COVER_COLOR";
    static final String SCROLL_BORDER_COLOR = "SCROLL_BORDER_COLOR";
    static final String INFO_VIEW_BACKGROUND = "INFO_VIEW_BACKGROUND";
    static final String INFO_VIEW_CIRCLE_COLOR = "CIRCLE_COLOR";

    static {
        dayResource.put(PRIMARY_COLOR, R.color.colorPrimary);
        nightResource.put(PRIMARY_COLOR, R.color.colorPrimary_dark);

        dayResource.put(PRIMARY_TEXT_COLOR, R.color.black);
        nightResource.put(PRIMARY_TEXT_COLOR, R.color.white);

        dayResource.put(PRIMARY_DARK_COLOR, R.color.colorPrimaryDark);
        nightResource.put(PRIMARY_DARK_COLOR, R.color.colorPrimaryDark_dark);

        dayResource.put(CHART_BACKGROUND_COLOR, R.color.chart_background_day);
        nightResource.put(CHART_BACKGROUND_COLOR, R.color.chart_background_night);

        dayResource.put(WINDOW_BACKGROUND_COLOR, R.color.window_background_day);
        nightResource.put(WINDOW_BACKGROUND_COLOR, R.color.window_background_night);

        dayResource.put(AXIS_COLOR, R.color.axis_day);
        nightResource.put(AXIS_COLOR, R.color.axis_night);

        dayResource.put(AXIS_TEXT_COLOR, R.color.text_day);
        nightResource.put(AXIS_TEXT_COLOR, R.color.text_night);

        dayResource.put(SCROLL_COVER_COLOR, R.color.scroll_cover_day);
        nightResource.put(SCROLL_COVER_COLOR, R.color.scroll_cover_night);

        dayResource.put(SCROLL_BORDER_COLOR, R.color.scroll_border_day);
        nightResource.put(SCROLL_BORDER_COLOR, R.color.scroll_border_night);

        dayResource.put(INFO_VIEW_BACKGROUND, R.drawable.bg_info);
        nightResource.put(INFO_VIEW_BACKGROUND, R.drawable.bg_info_dark);

        dayResource.put(INFO_VIEW_CIRCLE_COLOR, R.color.chart_background_day);
        nightResource.put(INFO_VIEW_CIRCLE_COLOR, R.color.chart_background_night);
    }

    static int dpToPx(Context c, int dp) {
        return (int) (c.getResources().getDisplayMetrics().density * dp);
    }

    static int dpToPx(View v, int dp) {
        return dpToPx(v.getContext(), dp);
    }

    static int getDim(View v, int resId) {
        return getDim(v.getContext(), resId);
    }

    static int getDim(Context c, int resId) {
        return c.getResources().getDimensionPixelSize(resId);
    }

    static int spToPx(View v, int sp) {
        return spToPx(v.getContext(), sp);
    }

    static int spToPx(Context c, int sp) {
        return (int) (c.getResources().getDisplayMetrics().scaledDensity * sp);
    }

    static int getResId(String key) {
        if (isDayMode) {
            return dayResource.get(key);
        } else {
            return nightResource.get(key);
        }
    }

    static int getColor(Context context, String key) {
        return getColor(context, getResId(key));
    }

    static int getColor(Context context, int id) {
        if (SDK_INT >= 23) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    static Locale getLocale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (SDK_INT >= 24) {
            return configuration.getLocales().get(0);
        } else {
            return configuration.locale;
        }
    }

    static float getFontHeight(TextPaint textPaint) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        return fontMetrics.descent - fontMetrics.ascent;
    }

    public static void log(String message) {
        if (DEBUG) Log.d("telegramcharts", message);
    }

    public static int mixTwoColors( int color1, int color2, float amount ) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL   = 16;
        final byte GREEN_CHANNEL =  8;
        final byte BLUE_CHANNEL  =  0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int)(((float)(color1 >> ALPHA_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> ALPHA_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int r = ((int)(((float)(color1 >> RED_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> RED_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int g = ((int)(((float)(color1 >> GREEN_CHANNEL & 0xff )*amount) +
                ((float)(color2 >> GREEN_CHANNEL & 0xff )*inverseAmount))) & 0xff;
        int b = ((int)(((float)(color1 & 0xff )*amount) +
                ((float)(color2 & 0xff )*inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    static void drawAnimatedText(Canvas canvas, String from, String to, TextPaint paint, float x, float y, float height, ValueAnimator changeValueAnimator) {
        canvas.save();
        canvas.translate(x, y);

        if (TextUtils.isEmpty(from) || from.equals(to)) {
            paint.setAlpha(255);
            canvas.drawText(to, 0, 0, paint);
            canvas.restore();
            return;
        }

        float fraction = 1;
        if (changeValueAnimator != null && changeValueAnimator.isRunning()) {
            fraction = changeValueAnimator.getAnimatedFraction();
        }

        canvas.save();
        canvas.scale((1 - fraction), (1 - fraction), 0, -height);
        paint.setAlpha((int) (255 * (1 - fraction)));
        canvas.drawText(from, 0, 0, paint);
        canvas.restore();


        paint.setAlpha((int) (255 * fraction));

        canvas.save();
        canvas.scale(fraction, fraction, 0, height);
        canvas.drawText(to, 0, 0, paint);
        canvas.restore();

        canvas.restore();
    }

    static float getAnimationFraction(ValueAnimator valueAnimator) {
        return valueAnimator != null && valueAnimator.isRunning() ? valueAnimator.getAnimatedFraction() : 1f;
    }
}
