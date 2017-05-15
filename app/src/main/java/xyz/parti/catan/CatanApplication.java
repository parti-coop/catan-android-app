package xyz.parti.catan;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import timber.log.Timber;

public class CatanApplication extends MultiDexApplication {
    public enum AppStatus {
        BACKGROUND, // app is background
        FOREGROUND; // app is foreground
    }

    private AppStatus mAppStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Iconify.with(new FontAwesomeModule());
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            private int running = 0;


            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                ++running;
                if (running >= 1) {
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

    // check if app is return foreground
    public boolean isBackground() {
        return mAppStatus == null ||  mAppStatus.ordinal() == AppStatus.BACKGROUND.ordinal();
    }

    // check if app is return foreground
    public boolean isForground() {
        return mAppStatus != null && mAppStatus.ordinal() == AppStatus.FOREGROUND.ordinal();
    }
}
