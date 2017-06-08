package xyz.parti.catan;

import android.support.multidex.MultiDexApplication;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.facebook.stetho.Stetho;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import timber.log.Timber;
import xyz.parti.catan.data.model.ReadParti;

public class CatanApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Iconify.with(new FontAwesomeModule());
        Stetho.initializeWithDefaults(this);

        Configuration dbConfiguration = new Configuration.Builder(this)
                .setDatabaseName("Catan")
                .addModelClass(ReadParti.class)
                .create();
        ActiveAndroid.initialize(dbConfiguration);
    }
}
