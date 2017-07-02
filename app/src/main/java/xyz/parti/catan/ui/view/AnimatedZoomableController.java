package xyz.parti.catan.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;

import com.facebook.common.internal.Preconditions;

import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.ui.gestures.TransformGestureDetector;


public class AnimatedZoomableController extends AbstractAnimatedZoomableController {

    private static final Class<?> TAG = AnimatedZoomableController.class;

    private final ValueAnimator mValueAnimator;

    public static AnimatedZoomableController newInstance() {
        return new AnimatedZoomableController(TransformGestureDetector.newInstance());
    }

    public AnimatedZoomableController(TransformGestureDetector transformGestureDetector) {
        super(transformGestureDetector);
        mValueAnimator = ValueAnimator.ofFloat(0, 1);
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
    }

    @Override
    public void setTransformAnimated(
            final Matrix newTransform,
            long durationMs,
            @Nullable final Runnable onAnimationComplete) {
        CatanLog.d("setTransformAnimated: duration %d ms", durationMs);
        stopAnimation();
        Preconditions.checkArgument(durationMs > 0);
        Preconditions.checkState(!isAnimating());
        setAnimating(true);
        mValueAnimator.setDuration(durationMs);
        getTransform().getValues(getStartValues());
        newTransform.getValues(getStopValues());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                calculateInterpolation(getWorkingTransform(), (float) valueAnimator.getAnimatedValue());
                AnimatedZoomableController.super.setTransform(getWorkingTransform());
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                CatanLog.d("setTransformAnimated: animation cancelled");
                onAnimationStopped();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                CatanLog.d("setTransformAnimated: animation finished");
                onAnimationStopped();
            }
            private void onAnimationStopped() {
                if (onAnimationComplete != null) {
                    onAnimationComplete.run();
                }
                setAnimating(false);
                getDetector().restartGesture();
            }
        });
        mValueAnimator.start();
    }

    @SuppressLint("NewApi")
    @Override
    public void stopAnimation() {
        if (!isAnimating()) {
            return;
        }
        CatanLog.d("stopAnimation");
        mValueAnimator.cancel();
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.removeAllListeners();
    }

    @Override
    protected Class<?> getLogTag() {
        return TAG;
    }

}
