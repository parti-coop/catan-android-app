package xyz.parti.catan;

import android.app.Application;

import timber.log.Timber;

public class CatanApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
