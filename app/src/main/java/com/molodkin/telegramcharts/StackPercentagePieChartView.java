package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.view.MotionEvent;

import java.util.ArrayList;

public final class StackPercentagePieChartView extends BaseChart {

    private float[] sums;

    private RectF rect = new RectF();

    private ArrayList<String> values = new ArrayList<>();
    private ArrayList<float []> angles = new ArrayList<>();

    private TextPaint textPaint;
    private int fromTextSize;
    private int koeffTextSize;
    private float top = 0;
    private float left = 0;
    private ValueAnimator animator = null;
    private int selectMargin = Utils.dpToPx(this,10);
    private int topMargin = Utils.dpToPx(this,10);
    private int popupMargin = Utils.dpToPx(this,10);
    private int selectedIndex = -1;
    private int preSelectedIndex = -1;

    @SuppressWarnings("FieldCanBeLocal")
    private final int valueTextSize = Utils.spToPx(getContext(), 12);
    @SuppressWarnings("FieldCanBeLocal")
    private final int nameTextSize = Utils.spToPx(getContext(), 12);

    private final int textSideMargin = Utils.dpToPx(getContext(),12);
    private final int textTopMargin = Utils.dpToPx(getContext(), 22);

    private NinePatchDrawable background;

    @SuppressWarnings("FieldCanBeLocal")
    private Rect backgroundSize;

    int windowWidth;
    int windowHeight;

    private TextPaint valueTextPaint;
    private TextPaint nameTextPaint;

    public StackPercentagePieChartView(Context context) {
        super(context, false);
        enablingWithAlphaAnimation = false;
    }

    @Override
    public void initTheme() {
        super.initTheme();

        windowWidth = Utils.spToPx(getContext(), 160);
        windowHeight = Utils.spToPx(getContext(), 35);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        fromTextSize = Utils.spToPx(getContext(), 10);
        koeffTextSize = Utils.spToPx(getContext(), 30);

        valueTextPaint = new TextPaint();

        nameTextPaint = new TextPaint();

        valueTextPaint.setTextSize(valueTextSize);
        valueTextPaint.setAntiAlias(true);
        valueTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        nameTextPaint.setTextSize(nameTextSize);
        nameTextPaint.setAntiAlias(true);

        background = (NinePatchDrawable) getContext().getResources().getDrawable(Utils.getResId(Utils.INFO_VIEW_BACKGROUND), getContext().getTheme());
        backgroundSize = new Rect();
        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = windowWidth;
        backgroundSize.bottom = windowHeight;
        background.setBounds(backgroundSize);

        nameTextPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
    }

    @Override
    public void initGraphs() {
        if (getWidth() == 0 || getHeight() == 0 || data == null) return;

        xPoints = data.x;
        sums = new float[xPoints.length];

        start = 0;
        end = data.x.length;

        visibleStart = 0;
        visibleEnd = data.x.length;

        graphs = new StackPercentageChartGraph[data.values.size()];

        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = new StackPercentageChartGraph(data.values.get(i), Color.parseColor(data.colors.get(i)), data.names.get(i));
        }

        availableChartHeight = (float) getHeight() - xAxisHeight;
        availableChartWidth = (float) getWidth() - sideMargin * 2;

        float scaleX = availableChartWidth / (xPoints.length - 1);

        yAxis1 = new StackPercentageYAxis(this, chartMatrix);
        yAxis1.isHalfLine = false;
        yAxis1.init();

        chartMatrix.postScale(scaleX, 1, 0, 0);

        float chartSize = Math.min(availableChartHeight, availableChartWidth) - sideMargin * 2;

        rect.set(0, 0, chartSize, chartSize);

        left = (getWidth() - rect.width()) / 2f;
        top = topMargin + (getHeight() - rect.height()) / 2f;
//
        buildRectangles();
        rangeChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX() - left;
            float y = event.getY() - top;
            if (rect.contains(x, y)) {
                float dx = x - rect.width() / 2;
                float dy = y - rect.height() / 2;
                double rad = Math.atan2(dy, dx);
                float angle = (float) (rad * 180 / Math.PI);
                if (angle < 0) {
                    angle = 360 + angle;
                }
                for (int i = 0; i < angles.size(); i++) {
                    float [] range = angles.get(i);
                    if (graphs[i].isEnable && range[0] < angle && angle < range[1]) {
                        preSelectedIndex = selectedIndex;
                        if (i != selectedIndex) {
                            selectedIndex = i;
                        } else {
                            selectedIndex = -1;
                        }
                        startAnimation();

                    }
                }
            } else {
                preSelectedIndex = selectedIndex;
                selectedIndex = -1;
                startAnimation();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initGraphs();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;
        drawData(canvas);
    }

    private void buildRectangles() {

        for (int i = 0; i < xPoints.length; i++) {
            float sum = 0f;
            for (BaseChartGraph graph : graphs) {
                if (graph.alpha == 0) continue;
                sum += graph.values[i] * graph.alpha;
            }
            sums[i] = sum;
        }

        int maxValue = yAxis1.maxValue;

        for (int i = 0; i < xPoints.length; i++) {

            float top = maxValue;

            for (BaseChartGraph graph1 : graphs) {

                StackPercentageChartGraph graph = (StackPercentageChartGraph) graph1;
                if (graph.alpha == 0) continue;

                float value = 100 * graph.values[i] * graph.alpha / sums[i];
                float currentTop = top - value;

                if (i == 0) {
                    graph.path.reset();
                    graph.path.moveTo(i, maxValue);
                    graph.path.lineTo(i, currentTop);
                } else if (i == xPoints.length - 1) {
                    graph.path.lineTo(i, currentTop);
                    graph.path.lineTo(i, maxValue);
                    graph.path.lineTo(0, maxValue);
                } else {
                    graph.path.lineTo(i, currentTop);
                }

                top -= value;
            }
        }
    }

    @Override
    protected void rangeChanged() {

        values.clear();
        angles.clear();

        int sum = 0;

        for (BaseChartGraph graph1 : graphs) {
            StackPercentageChartGraph graph = (StackPercentageChartGraph) graph1;
            if (graph.isEnable) {
                sum += graph.sumValuesMatrix[start][end - 1];
            }
        }

        int startAngle = 0;

        for (BaseChartGraph graph1 : graphs) {
            StackPercentageChartGraph graph = (StackPercentageChartGraph) graph1;
            float percent = 1f * graph.sumValuesMatrix[start][end - 1] / sum;
            int sweepAngle = Math.round(360f * percent);
            angles.add(new float[]{startAngle, startAngle + sweepAngle});
            if (graph.isEnable) {
                startAngle += sweepAngle;
            }
            values.add(String.format("%s %%", String.valueOf(Math.round(percent * 100))));
        }
//        startAnimation();
    }

    @Override
    protected void graphEnablingChanged() {
        rangeChanged();
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void graphAlphaChanged() {
        buildRectangles();
    }

    private void drawData(Canvas canvas) {

        float sum = 0;
        int last = 0;
        for (int i = 0; i < graphs.length; i++) {
            StackPercentageChartGraph graph = (StackPercentageChartGraph) graphs[i];
            if (graph.isVisible()) {
                sum += graph.sumValuesMatrix[start][end - 1] * graph.alpha;
                last = i;
            }
        }

        canvas.save();
        canvas.translate(left, top);

        int startAngle = 0;
        float selectedEndAngle = 0;

        for (int i = 0; i < graphs.length; i++) {
            StackPercentageChartGraph graph = (StackPercentageChartGraph) graphs[i];
            if (graph.isVisible()) {
                float percent = graph.alpha * graph.sumValuesMatrix[start][end - 1] / sum;
                int sweepAngle = Math.round(360f * percent);
                if (i == last) {
                    sweepAngle = 360 - startAngle;
                }

                double textAngle = Math.PI * (startAngle + sweepAngle / 2f) / 180;

                double cos = Math.cos(textAngle);
                double sin = Math.sin(textAngle);

                float selectedDX = 0;
                float selectedDY = 0;
                if ((i == selectedIndex || i == preSelectedIndex) && (int) percent != 1) {
                    float fraction = Utils.getAnimationFraction(animator);
                    fraction = i == selectedIndex ? fraction : 1 - fraction;
                    selectedDX = (float) (cos * selectMargin * fraction);
                    selectedDY = (float) (sin * selectMargin * fraction);
                    if (i == selectedIndex) selectedEndAngle = startAngle + sweepAngle;
                }

                canvas.save();

                canvas.translate(selectedDX, selectedDY);

                canvas.drawArc(rect, startAngle, sweepAngle, true, graph.piePaint);

                canvas.restore();

                textPaint.setTextSize(fromTextSize + koeffTextSize * percent);

                String text = values.get(i);
                float textHeight = Utils.getFontHeight(textPaint);


                float textX = (float) (rect.width() * cos / 3);
                float textY = (float) (rect.height() * sin / 3);
                if (textX > 0) {
                    textX -= textPaint.measureText(text) / 2f;
                }
                if (textY < 0) {
                    textY += textHeight / 2f;
                }

                canvas.save();

                canvas.translate(rect.width() / 2, rect.height() / 2);

                canvas.save();

                canvas.translate(selectedDX, selectedDY);

//                String preText = preValues.size() > i ? preValues.get(i) : null;

//                Utils.drawAnimatedText(canvas, preText, text, textPaint, textX, textY, textHeight, animator);

                canvas.drawText(values.get(i), textX, textY, textPaint);

                canvas.restore();

                canvas.restore();


                startAngle += sweepAngle;
            } else {
                if (i == selectedIndex) {
                    selectedIndex = -1;
                    preSelectedIndex = -1;
                }
            }
        }
        canvas.restore();

        drawPopup(canvas, selectedEndAngle);

    }

    private void drawPopup(Canvas canvas, float angle) {
        if (selectedIndex != -1 && graphs[selectedIndex].isVisible()) {
            StackPercentageChartGraph graph = (StackPercentageChartGraph) graphs[selectedIndex];
            double textAngle = Math.PI * angle / 180;

            double cos = Math.cos(textAngle);
            double sin = Math.sin(textAngle);

            float x = (float) (cos * rect.width() / 4);
            float y = (float) (sin * popupMargin);

            if (x >= 0) {
                if (y >= 0) {
                    x -= windowWidth;
                    y += windowHeight;
                } else {
                    x -= windowWidth;
                    y -= windowHeight;
                }
            } else {
                if (y >= 0) {
                    y -= windowHeight;
                } else {
                    y -= windowHeight;
                }
            }

            canvas.save();

            canvas.translate(left + rect.width() / 2 + x, top + rect.height() / 2 + y);

            background.setAlpha((int) (255 * Utils.getAnimationFraction(animator)));
            background.draw(canvas);

            canvas.drawText(graph.name, textSideMargin, textTopMargin, nameTextPaint);

            String valueText = BaseInfoView.DECIMAL_FORMAT.format(graph.sumValuesMatrix[start][end - 1]);
            float textWidth = valueTextPaint.measureText(valueText);
            valueTextPaint.setColor(graph.color);
            canvas.drawText(valueText, windowWidth - textSideMargin - textWidth, textTopMargin, valueTextPaint);

            canvas.restore();
        }
    }
}
