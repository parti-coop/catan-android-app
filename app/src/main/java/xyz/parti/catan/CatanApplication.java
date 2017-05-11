package xyz.parti.catan;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import timber.log.Timber;

public class CatanApplication extends Application {
    public enum AppStatus {
        BACKGROUND, // app is background
        RETURNED_TO_FOREGROUND, // app returned to foreground(or first launch)
        FOREGROUND; // app is foreground
    }

    private AppStatus mAppStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Iconify.with(new FontAwesomeModule());
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private int running = 0;
            
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (++running == 1) {
                    mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
                } else if (running > 1) {
                    mAppStatus = AppStatus.FOREGROUND;
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (--running == 0) {
                    mAppStatus = AppStatus.BACKGROUND;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    // check if app is return foreground
    public boolean isReturnedForground() {
        return mAppStatus.ordinal() == AppStatus.RETURNED_TO_FOREGROUND.ordinal();
    }

    // check if app is return foreground
    public boolean isForground() {
        return mAppStatus.ordinal() == AppStatus.FOREGROUND.ordinal();
    }
}
