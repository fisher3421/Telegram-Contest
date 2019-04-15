package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;


public class TCheckBox extends CompoundButton {

    private Path checkMark;

    private final Paint checkPaint = new Paint();
    private final Paint rectanglePaint = new Paint();
    private final Paint borderPaint = new Paint();

    private ValueAnimator animator;

    private int color = Color.GREEN;

    private float roundDim = Utils.dpToPx(this, 40);
    private float checkSideMargin = Utils.dpToPx(this, 12);
    private float textSideMargin = Utils.dpToPx(this, 8);
    private float padding = Utils.dpToPx(this, 2);

    private float animatorFraction;

    private CheckBoxListener listener;

    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e) {
            startTouchAnimation(true);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isLocked()) {
                startTouchAnimation(false);
                startShakeAnimation();
            } else {
                setChecked(!isChecked());
                if (listener != null) listener.onClick();

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!isChecked()) {
                setChecked(true);
            } else {
                startTouchAnimation(false);
            }
            if (listener != null) listener.onLongClick();
        }
    });

    public void setLongClickListener(CheckBoxListener longClickListener) {
        this.listener = longClickListener;
    }

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            animatorFraction = (float) animator.getAnimatedValue();
            updateAnimatedValues();
            invalidate();
        }
    };

    public TCheckBox(Context context) {
        super(context);
        init();
    }

    public TCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private boolean isLocked() {
        for (TCheckBox checkBox : ((ChartLayout) getParent()).checkBoxes) {
            if (checkBox != this && checkBox.isChecked()) return false;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if (isLocked()) {
//                    startShakeAnimation();
//                    return false;
//                } else {
//                    startTouchAnimation(true);
//                }
//                break;
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                startTouchAnimation(false);
//                break;
//        }
//
//        return super.onTouchEvent(event);
    }



    public void setColor(int color) {
        this.color = color;
        borderPaint.setColor(color);
        rectanglePaint.setColor(color);
        invalidate();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (animatorUpdateListener == null){
            animatorFraction = checked ? 1f : 0f;
            return;
        }
        startClickAnimation();
    }



    private void startClickAnimation() {
        if (isChecked()) {
            startAnimation(1f);
        } else {
            startAnimation(0f);
        }
    }

    private void startTouchAnimation(boolean isDown) {
        if (isChecked()) {
            if (isDown) {
                startAnimation(0.8f);
            } else {
                startAnimation(1f);
            }
        } else {
            if (isDown) {
                startAnimation(0.2f);
            } else {
                startAnimation(0f);
            }
        }
    }

    private void startShakeAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        this.startAnimation(animation);
    }

    private void startAnimation(float to) {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(animatorFraction, to);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();
    }

    private void init() {
        initPath();

        checkPaint.setColor(Color.WHITE);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(Utils.dpToPx(this, 2));
        checkPaint.setAntiAlias(true);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);

        borderPaint.setColor(color);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Utils.dpToPx(this, 2));

        rectanglePaint.setColor(color);
        rectanglePaint.setAntiAlias(true);
        rectanglePaint.setStyle(Paint.Style.FILL);

        updateAnimatedValues();
    }

    private void initPath() {
        checkMark = new Path();
        checkMark.moveTo(0, 0);
        checkMark.lineTo(Utils.dpToPx(this, 4), Utils.dpToPx(this, 4));
        checkMark.lineTo(Utils.dpToPx(this, 12), Utils.dpToPx(this, -6));
    }

    private void updateAnimatedValues() {
        checkPaint.setAlpha((int) (255 * animatorFraction));
        rectanglePaint.setAlpha((int) (255 * animatorFraction));
        setTextColor(Utils.mixTwoColors(Color.WHITE, color, animatorFraction));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawRoundRect(padding, padding, getWidth() - padding, getHeight() - padding, roundDim, roundDim, borderPaint);

        canvas.drawRoundRect(padding, padding, getWidth() - padding, getHeight() - padding, roundDim, roundDim, rectanglePaint);

        canvas.save();

        canvas.translate(animatorFraction * textSideMargin, 0);
        super.onDraw(canvas);

        canvas.restore();

        canvas.save();

        canvas.translate(checkSideMargin, getHeight() / 2f);

        canvas.drawPath(checkMark, checkPaint);

        canvas.restore();
    }

    interface CheckBoxListener {
        void onClick();
        void onLongClick();
    }
}
