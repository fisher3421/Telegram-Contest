package com.molodkin.telegramcharts;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;

import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;

class Utils {


    static int dpToPx(Context c, int dp) {
        return (int) (c.getResources().getDisplayMetrics().density * dp);
    }

    static int dpToPx(View v, int dp) {
        return dpToPx(v.getContext(), dp);
    }

    static int spToPx(View v, int sp) {
        return spToPx(v.getContext(), sp);
    }

    static int spToPx(Context c, int sp) {
        return (int) (c.getResources().getDisplayMetrics().scaledDensity * sp);
    }

    static Matrix invertMatrix(Matrix matrix) {
        Matrix invertMatrix = new Matrix();
        matrix.invert(invertMatrix);
        return invertMatrix;
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
}
