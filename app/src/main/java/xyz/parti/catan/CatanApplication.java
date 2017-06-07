package xyz.parti.catan;

import android.support.multidex.MultiDexApplication;

import com.activeandroid.ActiveAndroid;
import com.facebook.stetho.Stetho;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import timber.log.Timber;

public class CatanApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Iconify.with(new FontAwesomeModule());
        Stetho.initializeWithDefaults(this);
        ActiveAndroid.initialize(this);
    }
}
