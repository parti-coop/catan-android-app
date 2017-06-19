package xyz.parti.catan.ui.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.Constants;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.services.AppVersionService;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.RxGuardian;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class AppVersionCheckTask {
    private static final String KEY_LAST_CHECKED_AT_MILLS = "app_version_last_checked_at_mills";
    private static final long CHECK_INTERVAL_MILLS = 48 * 60 * 60 * 1000;
    private final RxGuardian rxGuardian;

    private SharedPreferences pref;
    private String currentVersion;
    private Disposable lastVersionObserver;

    public AppVersionCheckTask(String currentVersion, Context context) {
        this.currentVersion = currentVersion;
        pref = context.getSharedPreferences(Constants.PREF_NAME_VERSION_CHECKER, Context.MODE_PRIVATE);
        this.rxGuardian = new RxGuardian();
    }

    public void cancel() {
        this.rxGuardian.unsubscribeAll();
    }

    public interface NewVersionAction {
        void run(String newVersion);
    }

    public void check(final @NonNull NewVersionAction action) {
        long lastCheckedAtMills = this.pref.getLong(KEY_LAST_CHECKED_AT_MILLS, -1);

        if(lastCheckedAtMills > 0 && (System.currentTimeMillis() - lastCheckedAtMills < CHECK_INTERVAL_MILLS)) {
            return;
        }
        AppVersionService appVersionService = ServiceBuilder.createUnsignedService(AppVersionService.class);
        this.lastVersionObserver = rxGuardian.subscribe(lastVersionObserver,
                appVersionService.getLastVersion(),
                new Consumer<Response<JsonObject>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Response<JsonObject> response) throws Exception {
                        if (response.isSuccessful()) {
                            pref.edit().putLong(KEY_LAST_CHECKED_AT_MILLS, System.currentTimeMillis()).apply();

                            String lastVersion = response.body().get("last_version").getAsString();
                            if (TextUtils.isEmpty(lastVersion) || lastVersion.equals(currentVersion)) {
                                return;
                            }
                            action.run(lastVersion);
                        } else {
                            CatanLog.d("실패 " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                        CatanLog.e("getNewVersionIfAvailable 오류 ", error);
                    }
                });
    }
}
