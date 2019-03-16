package com.molodkin.telegramcharts;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextPaint;
import android.view.View;

import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;

public class Utils {


    public static int dpToPx(Context c, int dp) {
        return (int) (c.getResources().getDisplayMetrics().density * dp);
    }

    public static int pxToDp(Context c, int px) {
        return (int) (px / c.getResources().getDisplayMetrics().density);
    }

    public static int dpToPx(View v, int dp) {
        return dpToPx(v.getContext(), dp);
    }

    public static int pxToDp(View v, int px) {
        return pxToDp(v.getContext(), px);
    }

    public static int spToPx(View v, int sp) {
        return spToPx(v.getContext(), sp);
    }

    public static int spToPx(Context c, int sp) {
        return (int) (c.getResources().getDisplayMetrics().scaledDensity * sp);
    }

    public static Matrix invertMatrix(Matrix matrix) {
        Matrix invertMatrix = new Matrix();
        matrix.invert(invertMatrix);
        return invertMatrix;
    }

    public static final int getColor(Context context, int id) {
        if (SDK_INT >= 23) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static final Locale getLocale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (SDK_INT >= 24) {
            return configuration.getLocales().get(0);
        } else {
            return configuration.locale;
        }
    }

    public static final float getFontHeight(TextPaint textPaint) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        return fontMetrics.descent - fontMetrics.ascent;
    }
}
