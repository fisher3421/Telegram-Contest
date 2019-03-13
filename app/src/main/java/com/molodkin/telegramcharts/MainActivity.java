package com.molodkin.telegramcharts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LineChart lineChart = findViewById(R.id.chart);

        final int max = lineChart.getEnd() - lineChart.getStart();

        final SeekBar seekBar1 = findViewById(R.id.seek1);
        final SeekBar seekBar2 = findViewById(R.id.seek2);
        final SeekBar seekBar3 = findViewById(R.id.seek3);

        seekBar1.setMax(max);
        seekBar1.setProgress(lineChart.getStart());

        seekBar2.setMax(max);
        seekBar2.setProgress(lineChart.getEnd());

        seekBar3.setMax(0);
        seekBar3.setProgress(0);

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lineChart.setStart(progress);
                    seekBar3.setMax(max - (lineChart.getEnd() - lineChart.getStart()));
                    seekBar3.setProgress(lineChart.getStart() - (max - lineChart.getEnd()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                lineChart.adjustYAxis();
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    lineChart.setEnd(progress);
                    seekBar3.setMax(max - (lineChart.getEnd() - lineChart.getStart()));
                    seekBar3.setProgress(lineChart.getStart() - (max - lineChart.getEnd()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                lineChart.adjustYAxis();
            }
        });

        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int range = lineChart.getEnd() - lineChart.getStart();
                    lineChart.setStartEnd(progress, progress + range);
                    seekBar1.setProgress(progress);
                    seekBar2.setProgress(progress + range);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
}
