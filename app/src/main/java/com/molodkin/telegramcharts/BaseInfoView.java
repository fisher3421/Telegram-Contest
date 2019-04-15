package com.molodkin.telegramcharts;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

abstract class BaseInfoView extends View {

    protected final static long MOVE_ANIMATION = 200L;

    private static final String DATE_SEPARATOR = "_";

    @SuppressWarnings("FieldCanBeLocal")
    private final int dateTextSize = Utils.spToPx(getContext(), 12);
    @SuppressWarnings("FieldCanBeLocal")
    private final int valueTextSize = Utils.spToPx(getContext(), 12);
    @SuppressWarnings("FieldCanBeLocal")
    private final int nameTextSize = Utils.spToPx(getContext(), 12);

    private float dateTextHeight;
    private float nameTextHeight;
    private float valueTextHeight;

    @SuppressWarnings("FieldCanBeLocal")
    private final int topBottomPadding = Utils.dpToPx(getContext(), 8);
    @SuppressWarnings("FieldCanBeLocal")
    private final int leftRightPadding = Utils.dpToPx(getContext(), 16);

    private final int rowMargin = Utils.dpToPx(getContext(), 8);
    final int windowLineMargin = Utils.dpToPx(getContext(), 8);
    private final int dataSideMargin = Utils.dpToPx(getContext(), 8);

    private final Rect backgroundSize = new Rect();
    private final Rect backgroundPadding = new Rect();

    private NinePatchDrawable background;

    protected final Paint circleFillPaint = new Paint();
    protected final Paint circleStrokePaint = new Paint();
    protected final Paint verticalLinePaint = new Paint();

    private final TextPaint dateTextPaint = new TextPaint();
    private final TextPaint valueTextPaint = new TextPaint();
    private final TextPaint nameTextPaint = new TextPaint();

    private DateFormat dateFormat;

    private final Date tempDate = new Date();

    private final float left;
    final int windowWidth = Utils.spToPx(getContext(), 160);
    float windowHeight;
    private float contentWidth;

    private final float dateTextY;
    private String prevDateText1;
    private String dateText1;
    private String prevDateText2;
    private String dateText2;
    private float preDateText1Width;
    private float dateText1Width;
    private float dateMargin;

    private final ArrayList<Float> leftValues = new ArrayList<>();
    private final ArrayList<Float> topValues = new ArrayList<>();
    private final ArrayList<String> preTextValues = new ArrayList<>();
    private final ArrayList<String> textValues = new ArrayList<>();
    private final ArrayList<String> textNames = new ArrayList<>();
    private final ArrayList<String> textPercentage = new ArrayList<>();
    private final ArrayList<String> preTextPercentage = new ArrayList<>();
    private final ArrayList<Float> textPercentageWidths = new ArrayList<>();
    private final ArrayList<Float> textPercentageLeft = new ArrayList<>();
    protected final ArrayList<Integer> textColors = new ArrayList<>();
    protected final ArrayList<Float> alphas = new ArrayList<>();

    protected float maxYCoord;

    float windowTopMargin = Utils.dpToPx(getContext(), 30);

    private boolean isVisible;
    private boolean isScrolling;

    protected final BaseChart chartView;

    protected float xCoord = 0f;
    protected int xIndex = -1;

    boolean showPercentage = false;
    boolean showAll = false;

    private ValueAnimator changeValueAnimator;

    private float percentageMaxWidth;

    public static final DecimalFormat DECIMAL_FORMAT;

    static {
        DECIMAL_FORMAT = new DecimalFormat("###,###");
        DecimalFormatSymbols symbols = DECIMAL_FORMAT.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(' ');
        DECIMAL_FORMAT.setDecimalFormatSymbols(symbols);
    }

    private ZoomInListenr zoomInListenr;
    private RectF tempRect = new RectF();

    float preWindowLeftMargin;
    float windowLeftMargin;

    private Drawable arrow = null;

    private GestureDetector gestureDetector = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                isScrolling = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                isVisible = true;
            }

            if (isScrolling) {
                onActionMove(e2.getX());
            }

            return isScrolling;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX();
            tempRect.set(windowLeftMargin, windowTopMargin, windowLeftMargin + windowWidth, windowTopMargin + windowHeight);
            if (!isVisible || isOutSide()) {
                isVisible = true;

                xIndex = chartView.xIndexByCoord(x);

                move();
            } else if (tempRect.contains(e.getX(), e.getY())) {
                notifyZoomIn();
            } else {
                isScrolling = false;
                isVisible = false;
                preTextValues.clear();
                preTextPercentage.clear();
                prevDateText1 = null;
                dateText1 = null;
                prevDateText2 = null;
                dateText2 = null;
                preDateText1Width = 0;
                textValues.clear();
                invalidate();
            }

            return isVisible;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    });

    BaseInfoView(Context c, BaseChart chartView) {
        super(c);
        if (!chartView.isZoomed) {
            arrow = getContext().getDrawable(R.drawable.baseline_chevron_right_black_18);
            int arrowSize = Utils.dpToPx(this, 20);
            int arrowLeft = windowWidth - arrowSize - dataSideMargin - Utils.dpToPx(this, 2);
            arrow.setBounds(arrowLeft, topBottomPadding, arrowLeft + arrowSize, topBottomPadding + arrowSize);
            dateFormat = new SimpleDateFormat("EEE, d" + DATE_SEPARATOR + "MMM yyyy", Utils.getLocale(c));
        } else {
            dateFormat = new SimpleDateFormat("HH:mm", Utils.getLocale(c));
        }

        this.chartView = chartView;

        dateTextPaint.setTextSize(dateTextSize);
        dateTextPaint.setAntiAlias(true);
        dateTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        dateTextHeight = Utils.getFontHeight(dateTextPaint);

        dateMargin = dateTextPaint.measureText(" ");

        valueTextPaint.setTextSize(valueTextSize);
        valueTextPaint.setAntiAlias(true);
        valueTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        valueTextHeight = Utils.getFontHeight(valueTextPaint);

        nameTextPaint.setTextSize(nameTextSize);
        nameTextPaint.setAntiAlias(true);
        nameTextHeight = Utils.getFontHeight(nameTextPaint);

        left = backgroundPadding.left + leftRightPadding;
        float top = backgroundPadding.top + topBottomPadding;

        dateTextY = top + dateTextHeight;

        contentWidth = windowWidth - leftRightPadding * 2 - backgroundPadding.left - backgroundPadding.right;

        initTheme();

        background.getPadding(backgroundPadding);
    }

    public void move() {
        if (!isVisible) return;

        xCoord = chartView.xCoordByIndex(xIndex);

        measureWindow(xIndex);

        calcWindowMargin();

        onActionDown(xCoord);

        invalidate();
    }

    void calcWindowMargin() {
        if (maxYCoord - windowTopMargin < windowHeight) {
            if (xCoord > getWidth() / 2) {
                windowLeftMargin = xCoord - windowLineMargin - windowWidth;
            } else {
                windowLeftMargin = xCoord + windowLineMargin;
            }
        } else {
            windowLeftMargin = xCoord - windowWidth / 2f;

            if (windowLeftMargin < 0) {
                windowLeftMargin = 0;
            } else if (windowLeftMargin + windowWidth >= getWidth()) {
                windowLeftMargin = getWidth() - windowWidth;
            }
        }
    }

    public void setZoomInListenr(ZoomInListenr zoomInListenr) {
        this.zoomInListenr = zoomInListenr;
    }

    private void notifyZoomIn() {
        if (zoomInListenr != null) zoomInListenr.zoomIn(chartView.xPoints[xIndex]);
    }

    protected void initTheme() {
        background = (NinePatchDrawable) getContext().getResources().getDrawable(Utils.getResId(Utils.INFO_VIEW_BACKGROUND), getContext().getTheme());
        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = windowWidth;
        backgroundSize.bottom = (int) windowHeight;
        background.setBounds(backgroundSize);

        if (showAll && textColors.size() > 0) {
            textColors.set(textColors.size() - 1, Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        }

        dateTextPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
        nameTextPaint.setColor(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));

        if (arrow != null) arrow.setColorFilter(Utils.getColor(getContext(), Utils.ARROW_COLOR), PorterDuff.Mode.SRC_ATOP);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getY() < chartView.graphTopMargin) return false;
        return gestureDetector.onTouchEvent(event);
    }

    protected void onActionDown(float x) {
    }

    protected void onActionMove(float x) {
    }

    void measureWindow(int newXIndex) {
        long date = chartView.xPoints[newXIndex];

        long animationPlayTime = 0;
        if (changeValueAnimator != null) {
            if (changeValueAnimator.isRunning()) {
                animationPlayTime = changeValueAnimator.getCurrentPlayTime();
            }
            changeValueAnimator.cancel();
        }

        if (!TextUtils.isEmpty(prevDateText1)) {
            changeValueAnimator = ValueAnimator.ofFloat(0, 1);
            changeValueAnimator.setCurrentPlayTime(animationPlayTime);
            changeValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
            changeValueAnimator.start();
        }

        tempDate.setTime(date);

        prevDateText1 = dateText1;
        prevDateText2 = dateText2;
        preDateText1Width = dateText1Width;

        if (chartView.isZoomed) {
            dateText1 = dateFormat.format(tempDate);
        } else {
            String [] dateText = dateFormat.format(tempDate).split(DATE_SEPARATOR);

            dateText1 = dateText[0];
            dateText1Width = dateTextPaint.measureText(dateText1) + dateMargin;
            dateText2 = dateText[1];
        }

        float top = dateTextY + dateTextHeight + rowMargin;

        preTextValues.clear();
        preTextValues.addAll(textValues);

        preTextPercentage.clear();
        preTextPercentage.addAll(textPercentage);

        textNames.clear();
        textValues.clear();
        textPercentage.clear();
        textPercentageWidths.clear();
        textPercentageLeft.clear();
        textColors.clear();
        topValues.clear();
        leftValues.clear();
        alphas.clear();


        if (showPercentage) {
            float sum = 0;
            percentageMaxWidth = 0;
            for (BaseChartGraph graph : chartView.graphs) {
                if (graph.isVisible()) sum += graph.values[newXIndex];
            }
            for (BaseChartGraph graph : chartView.graphs) {
                if (graph.isVisible()) {
                    String value = String.format("%s %%", String.valueOf(Math.round(100 * graph.values[newXIndex] / sum)));
                    float textWidth = dateTextPaint.measureText(value);
                    textPercentageWidths.add(textWidth);
                    if (textWidth > percentageMaxWidth) percentageMaxWidth = textWidth;
                    textPercentage.add(value);
                }
            }
        }

        Iterator<Float> textPercentageWidthsIterator = textPercentageWidths.iterator();

        int sum = 0;
        for (int i = 0; i < chartView.graphs.length; i++) {
            BaseChartGraph graph = chartView.graphs[i];
            if (graph.isVisible()) {
                textNames.add(graph.name);

                sum += graph.values[newXIndex];
                String valueText = DECIMAL_FORMAT.format(graph.values[newXIndex]);
                textValues.add(valueText);
                textColors.add(graph.paint.getColor());
                alphas.add(graph.alpha);
                float valueWidth = valueTextPaint.measureText(valueText);
                leftValues.add(contentWidth - valueWidth);
                topValues.add(top);

                if (showPercentage) {
                    textPercentageLeft.add(percentageMaxWidth - textPercentageWidthsIterator.next());
                }

                top += nameTextHeight * graph.alpha + rowMargin;
            }
        }
        if (showAll) {
            textNames.add("All");

            String valueText = DECIMAL_FORMAT.format(sum);
            textValues.add(valueText);
            textColors.add(Utils.getColor(getContext(), Utils.PRIMARY_TEXT_COLOR));
            alphas.add(1f);
            float valueWidth = valueTextPaint.measureText(valueText);
            leftValues.add(contentWidth - valueWidth);
            topValues.add(top);

            top += nameTextHeight + rowMargin;
        }
        top -= rowMargin;

        windowHeight = top + backgroundPadding.bottom;

        backgroundSize.left = 0;
        backgroundSize.top = 0;
        backgroundSize.right = windowWidth;
        backgroundSize.bottom = (int) windowHeight;

        background.setBounds(backgroundSize);
    }

    private boolean isOutSide() {
        return xCoord < 0 || xCoord > getWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isVisible || textValues.isEmpty()) return;

        if (isOutSide()) return;

        drawContent(canvas);

        canvas.save();

        canvas.translate(0, windowTopMargin);

//        Utils.log("onDraw preWindowLeftMargin: " + preWindowLeftMargin);
//        Utils.log("onDraw windowLeftMargin: " + windowLeftMargin);

        if (changeValueAnimator != null && changeValueAnimator.isRunning() && preWindowLeftMargin > 0) {
            float fraction = changeValueAnimator.getAnimatedFraction();
            canvas.translate(preWindowLeftMargin * (1 - fraction) + windowLeftMargin * fraction, 0);
        } else {
            canvas.translate(windowLeftMargin, 0);
        }

        background.draw(canvas);

        if (arrow != null) arrow.draw(canvas);

        canvas.translate(left, 0);

        if (chartView.isZoomed) {
            drawAnimatedText(canvas, prevDateText1, dateText1, dateTextPaint, 0, dateTextY, dateTextHeight);
        } else {
            drawAnimatedText(canvas, prevDateText1, dateText1, dateTextPaint, 0, dateTextY, dateTextHeight);
            canvas.save();
            if (changeValueAnimator != null && changeValueAnimator.isRunning() && preDateText1Width > 0) {
                float fraction = changeValueAnimator.getAnimatedFraction();
                canvas.translate(preDateText1Width * (1 - fraction) + dateText1Width * fraction, 0);
            } else {
                canvas.translate(dateText1Width, 0);
            }
            drawAnimatedText(canvas, prevDateText2, dateText2, dateTextPaint, 0, dateTextY, dateTextHeight);
            canvas.restore();
        }

        for (int i = 0; i < textValues.size(); i++) {
            float alpha = alphas.get(i);
            nameTextPaint.setAlpha((int) (255 * alpha));
            if (showPercentage) {
                String pre = preTextPercentage.size() > i ? preTextPercentage.get(i) : null;
                drawAnimatedText(canvas, pre, textPercentage.get(i), dateTextPaint, textPercentageLeft.get(i), topValues.get(i), dateTextHeight);
                canvas.drawText(textNames.get(i), percentageMaxWidth + dataSideMargin, topValues.get(i), nameTextPaint);
            } else {
                canvas.drawText(textNames.get(i), 0, topValues.get(i), nameTextPaint);
            }


            String textValue = textValues.get(i);
            int color = textColors.get(i);
            valueTextPaint.setColor(color);
            valueTextPaint.setAlpha((int) (255 * alpha));

            String pre = preTextValues.size() == textValues.size() ? preTextValues.get(i) : null;
            drawAnimatedText(canvas, pre, textValue, valueTextPaint, leftValues.get(i), topValues.get(i), valueTextHeight);
        }

        canvas.restore();
    }

    void drawAnimatedText(Canvas canvas, String from, String to, TextPaint paint, float x, float y, float height) {
        Utils.drawAnimatedText(canvas, from, to, paint, x, y, height, changeValueAnimator);
    }

    protected void drawContent(Canvas canvas) {
    }

    void updateTheme() {
        initTheme();
        invalidate();
    }

    public interface ZoomInListenr {
        void zoomIn(long date);
    }
}
