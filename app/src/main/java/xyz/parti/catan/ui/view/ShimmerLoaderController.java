package xyz.parti.catan.ui.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.animation.LinearInterpolator;


class ShimmerLoaderController {
    private final static float MIN_WEIGHT = 0.0f;
    final static float MAX_WEIGHT = 1.0f;
    final static boolean USE_GRADIENT_DEFAULT = false;
    private final static int COLOR_DEFAULT_GRADIENT = Color.rgb(245, 245, 245);

    private ShimmerLoaderView loaderView;
    private Paint rectPaint;
    private LinearGradient linearGradient;
    private float progress;
    private ValueAnimator valueAnimator;
    private float widthWeight = MAX_WEIGHT;
    private float heightWeight = MAX_WEIGHT;
    private boolean useGradient = USE_GRADIENT_DEFAULT;

    private final static int MAX_COLOR_CONSTANT_VALUE = 255;
    private final static int ANIMATION_CYCLE_DURATION = 750; //milis

    ShimmerLoaderController(ShimmerLoaderView view) {
        loaderView = view;
        init();
    }

    private void init() {
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        loaderView.setRectColor(rectPaint);
        setValueAnimator(0.5f, 1, ObjectAnimator.INFINITE);
    }

    void onDraw(Canvas canvas) {
        float margin_height = canvas.getHeight() * (1 - heightWeight) / 2;
        rectPaint.setAlpha((int) (progress * MAX_COLOR_CONSTANT_VALUE));
        if (useGradient) {
            prepareGradient(canvas.getWidth() * widthWeight);
        }
        canvas.drawRect(0,
                margin_height,
                canvas.getWidth() * widthWeight,
                canvas.getHeight() - margin_height,
                rectPaint);
    }

    void onSizeChanged() {
        linearGradient = null;
        startLoading();
    }

    private void prepareGradient(float width) {
        if (linearGradient == null) {
            linearGradient = new LinearGradient(0, 0, width, 0, rectPaint.getColor(),
                    COLOR_DEFAULT_GRADIENT, Shader.TileMode.MIRROR);
        }
        rectPaint.setShader(linearGradient);
    }

    void startLoading() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            init();
            valueAnimator.start();
        }
    }

    void setHeightWeight(float heightWeight) {
        this.heightWeight = validateWeight(heightWeight);
    }

    void setWidthWeight(float widthWeight) {
        this.widthWeight = validateWeight(widthWeight);
    }

    void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
    }

    private float validateWeight(float weight) {
        if (weight > MAX_WEIGHT)
            return MAX_WEIGHT;
        if (weight < MIN_WEIGHT)
            return MIN_WEIGHT;
        return weight;
    }

    void stopLoading() {
        valueAnimator.cancel();
        setValueAnimator(progress, 0, 0);
        valueAnimator.start();
    }

    private void setValueAnimator(float begin, float end, int repeatCount) {
        valueAnimator = ValueAnimator.ofFloat(begin, end);
        valueAnimator.setRepeatCount(repeatCount);
        valueAnimator.setDuration(ANIMATION_CYCLE_DURATION);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                loaderView.invalidate();
            }
        });
    }
}
