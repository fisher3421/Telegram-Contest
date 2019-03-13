package com.molodkin.telegramcharts;

import android.content.Context;
import android.graphics.Matrix;
import android.view.View;

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

    public static int spToPx(Context c, int sp) {
        return (int) (c.getResources().getDisplayMetrics().scaledDensity * sp);
    }

    public static Matrix invertMatrix(Matrix matrix) {
        Matrix invertMatrix = new Matrix();
        matrix.invert(invertMatrix);
        return invertMatrix;
    }
}
