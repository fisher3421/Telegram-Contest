package com.molodkin.telegramcharts;

import android.graphics.Matrix;

class StackPercentageYAxis extends BaseYAxis {


    StackPercentageYAxis(BaseChart chart, Matrix matrix) {

        super(chart, matrix);
        adjustValues = false;
        rowNumber = 5;
    }

    @Override
    int getMaxValueFullRange() { return  125; }

    @Override
    int getMaxValue() {
        return 125;
    }

    @Override
    public int getMinValue() {
        return 0;
    }


}
