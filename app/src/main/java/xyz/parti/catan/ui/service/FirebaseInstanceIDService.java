package xyz.parti.catan.ui.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.JsonNull;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.services.DeviceTokensService;
import xyz.parti.catan.helper.PrefPushMessage;
import xyz.parti.catan.helper.RxGuardian;

/**
 * Created by dalikim on 2017. 5. 13..
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    RxGuardian rxGuardian = new RxGuardian();
    private Disposable ceateTokenPublisher;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(Constants.TAG, "FirebaseInstanceIDService - Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        SessionManager session = new SessionManager(this);
        if(session.isLoggedIn() && PrefPushMessage.isReceivable(this)) {
            DeviceTokensService service = ServiceBuilder.createNoRefreshService(DeviceTokensService.class, session.getPartiAccessToken());
            ceateTokenPublisher = rxGuardian.subscribe(ceateTokenPublisher, service.create(refreshedToken, BuildConfig.APPLICATION_ID),
                    new Consumer<Response<JsonNull>>() {
                        @Override
                        public void accept(@NonNull Response<JsonNull> response) throws Exception {
                            Log.d(Constants.TAG, "FirebaseInstanceIDService - Reset Instance ID");
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable error) throws Exception {
                            Log.e(Constants.TAG, "FirebaseInstanceIDService - Error to reset Instance ID", error);
                        }
                    });
        }
    }
}