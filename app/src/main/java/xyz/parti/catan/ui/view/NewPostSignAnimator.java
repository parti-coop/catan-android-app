package xyz.parti.catan.ui.view;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import xyz.parti.catan.R;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by dalikim on 2017. 5. 5..
 */

public class NewPostSignAnimator {
    private final Animation slideUp;
    private final Animation slideDown;
    private View view;

    public NewPostSignAnimator(@NonNull View view) {
        this.view = view;
        this.view.setTranslationX(view.getHeight());
        this.view.setVisibility(View.INVISIBLE);

        slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                NewPostSignAnimator.this.view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                NewPostSignAnimator.this.view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void hideImmediately() {
        hideDelayed(0);
    }

    public void hideDelayed(int delayMillis) {
        if(!isVisible() || isAnimationRunning()) {
            return;
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isVisible() && !isAnimationRunning()) {
                    view.startAnimation(NewPostSignAnimator.this.slideUp);
                }
            }
        }, delayMillis);
    }

    public void show() {
        if(isVisible() || isAnimationRunning()) {
            return;
        }
        view.startAnimation(slideDown);
    }

    public boolean isVisible() {
        return view.getVisibility() == View.VISIBLE;
    }

    private boolean isAnimationRunning() {
        return (slideDown.hasStarted() && !slideDown.hasEnded()) || (slideUp.hasStarted() && !slideUp.hasEnded());
    }
}
