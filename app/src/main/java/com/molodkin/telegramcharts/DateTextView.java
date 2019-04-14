package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.ListIterator;

public class DateTextView extends View {

    private TextPaint textPaint;
    private float textHeight;
    private Date tempDate = new Date();

    private ArrayList<String> texts = new ArrayList<>();
    private ArrayList<String> preTexts = new ArrayList<>();
    private ArrayList<Float> preLefts = new ArrayList<>();
    private ArrayList<Float> lefts = new ArrayList<>();

    private static final String DATE_SEPARATOR = "_";
    private static final String SEPARATOR_BETWEEN_DATES = " - ";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("d" + DATE_SEPARATOR + " MMMM" + DATE_SEPARATOR + " yyyy", Utils.getLocale(getContext()));

    private ValueAnimator animator;

    public DateTextView(Context context) {
        super(context);
        textPaint = new TextPaint();
        textPaint.setTextSize(Utils.spToPx(this, 12));
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textHeight = Utils.getFontHeight(textPaint);
        updateTheme();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY));
    }

    public void updateTheme() {
        textPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        invalidate();
    }

    public void updateDate(long start, long end) {
        tempDate.setTime(start);
        String dateStartStr = dateFormat.format(tempDate);
        tempDate.setTime(end);
        String dateEndStr = dateFormat.format(tempDate);

        preTexts.clear();
        preTexts.addAll(texts);
        preLefts.clear();
        preLefts.addAll(lefts);

        texts.clear();
        lefts.clear();

        texts.addAll(Arrays.asList(dateStartStr.split(DATE_SEPARATOR)));
        texts.add(SEPARATOR_BETWEEN_DATES);
        texts.addAll(Arrays.asList(dateEndStr.split(DATE_SEPARATOR)));
        ListIterator<String> stringListIterator = texts.listIterator(texts.size());

        float width = 0;
        while (stringListIterator.hasPrevious()) {
            String previous = stringListIterator.previous();
            width += textPaint.measureText(previous);
            lefts.add(0, getWidth() - width);
        }

        long animationPlayTime = 0;
        if (animator != null) {
            if (animator.isRunning()) {
                animationPlayTime = animator.getCurrentPlayTime();
            }
            animator.cancel();
        }

        if (preTexts.size() > 0) {
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setCurrentPlayTime(animationPlayTime);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            animator.start();
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            String preText = null;
            float left = lefts.get(i);
            if (i < preTexts.size()) {
                preText = preTexts.get(i);
                float preLeft = preLefts.get(i);
                if (animator != null && animator.isRunning()) {
                    float fraction = animator.getAnimatedFraction();
                    left = (1 - fraction) * preLeft + left * fraction;
                }
            }
            drawAnimatedText(canvas, preText, text, textPaint, left, textHeight, textHeight);
        }

    }

    void drawAnimatedText(Canvas canvas, String from, String to, TextPaint paint, float x, float y, float height) {
        Utils.drawAnimatedText(canvas, from, to, paint, x, y, height, animator);
    }
}
