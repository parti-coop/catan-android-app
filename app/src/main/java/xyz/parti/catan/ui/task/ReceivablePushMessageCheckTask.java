package xyz.parti.catan.ui.task;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonNull;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.services.DeviceTokensService;
import xyz.parti.catan.helper.PrefPushMessage;
import xyz.parti.catan.helper.RxGuardian;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class ReceivablePushMessageCheckTask {
    private static final String KEY_LAST_CHECKED_AT_MILLS = "app_version_last_checked_at_mills";
    private final RxGuardian rxGuardian;
    private final DeviceTokensService deviceTokensService;

    private SharedPreferences pref;
    private Context context;
    private Disposable createDeviceTokenPublisher;

    public ReceivablePushMessageCheckTask(Context context, SessionManager session) {
        pref = context.getSharedPreferences(Constants.PREF_NAME_RECEIVABLE_PUSH_MESSAGE_CHECKER, Context.MODE_PRIVATE);
        this.context = context;
        deviceTokensService = ServiceBuilder.createNoRefreshService(DeviceTokensService.class, session.getPartiAccessToken());
        this.rxGuardian = new RxGuardian();
    }

    public void cancel() {
        this.rxGuardian.unsubscribeAll();
    }

    public void check() {
        long lastCheckedAtMills = this.pref.getLong(KEY_LAST_CHECKED_AT_MILLS, -1);
        if(lastCheckedAtMills > 0) {
            return;
        }

        if(PrefPushMessage.isReceivable(context)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.receivable_push_message_confirm_title);
        builder.setMessage(R.string.receivable_push_message_confirm_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichs) {
                String refreshedToken = getFirebaseInstanceToken();
                createDeviceTokenPublisher = rxGuardian.subscribe(createDeviceTokenPublisher, deviceTokensService.create(refreshedToken, BuildConfig.APPLICATION_ID),
                        new Consumer<Response<JsonNull>>() {
                            @Override
                            public void accept(@NonNull Response<JsonNull> response) throws Exception {
                                Log.d(Constants.TAG, "Create Instance ID");
                                PrefPushMessage.setReceivable(context);
                            }
                        });
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
        pref.edit().putLong(KEY_LAST_CHECKED_AT_MILLS, System.currentTimeMillis()).apply();
    }

    private String getFirebaseInstanceToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }
}
