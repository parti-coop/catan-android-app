package xyz.parti.catan;

import android.support.multidex.MultiDexApplication;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.facebook.stetho.Stetho;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;
import xyz.parti.catan.data.activerecord.ReadPostFeed;
import xyz.parti.catan.data.preference.JoinedPartiesPreference;

public class CatanApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Iconify.with(new FontAwesomeModule());

        Configuration dbConfiguration = new Configuration.Builder(this)
                .setDatabaseName("Catan")
                .addModelClass(ReadPostFeed.class)
                .create();
        ActiveAndroid.initialize(dbConfiguration);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().schemaVersion(1).build();
        Realm.setDefaultConfiguration(config);

        new JoinedPartiesPreference(this).reset();

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).withMetaTables().build())
                .build());
    }
}
