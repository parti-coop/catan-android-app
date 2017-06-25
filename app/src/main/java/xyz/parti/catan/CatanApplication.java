package xyz.parti.catan;

import android.support.multidex.MultiDexApplication;

import com.evernote.android.job.JobManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.stetho.Stetho;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import timber.log.Timber;
import xyz.parti.catan.data.UnsafeOkHttpClient;
import xyz.parti.catan.data.preference.JoinedPartiesPreference;
import xyz.parti.catan.ui.job.CatanJobCreator;

public class CatanApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Iconify.with(new FontAwesomeModule());

        Realm.init(this);

        RealmConfiguration.Builder builder = new RealmConfiguration.Builder();
        if(BuildConfig.DEBUG) {
            builder.deleteRealmIfMigrationNeeded();
        }
        RealmConfiguration config = builder.build();
        Realm.setDefaultConfiguration(config);

        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Fresco.initialize(this, OkHttpImagePipelineConfigFactory.newBuilder(this, okHttpClient).build());

        new JoinedPartiesPreference(this).reset();

        JobManager.create(this).addJobCreator(new CatanJobCreator());

        if(BuildConfig.DEBUG) {
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).withMetaTables().build())
                    .build());
        }
    }
}
