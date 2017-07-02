package xyz.parti.catan.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import xyz.parti.catan.R;


public class ShimmerLoaderView extends View {
    private ShimmerLoaderController loaderController;

    public ShimmerLoaderView(Context context) {
        super(context);
        init(null);
    }

    public ShimmerLoaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ShimmerLoaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        loaderController = new ShimmerLoaderController(this);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.loader_view, 0, 0);
        loaderController.setWidthWeight(typedArray.getFloat(R.styleable.loader_view_width_weight, ShimmerLoaderController.MAX_WEIGHT));
        loaderController.setHeightWeight(typedArray.getFloat(R.styleable.loader_view_height_weight, ShimmerLoaderController.MAX_WEIGHT));
        loaderController.setUseGradient(typedArray.getBoolean(R.styleable.loader_view_use_gradient, ShimmerLoaderController.USE_GRADIENT_DEFAULT));
        typedArray.recycle();

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.brand_gray_light));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        loaderController.onSizeChanged();
    }

    public void stopLoading() {
        if (loaderController != null) {
            loaderController.stopLoading();
        }
    }

    public void startLoading() {
        if (loaderController != null) {
            loaderController.startLoading();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        loaderController.onDraw(canvas);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (VISIBLE == visibility) {
            startLoading();
        } else {
            stopLoading();
        }
    }

    public void setRectColor(Paint rectPaint) {
       rectPaint.setColor(ContextCompat.getColor(getContext(), R.color.style_color_primary_light));
    }
}
