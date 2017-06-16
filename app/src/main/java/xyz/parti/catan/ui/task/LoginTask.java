package xyz.parti.catan.ui.task;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonNull;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.services.AuthTokenService;
import xyz.parti.catan.data.services.DeviceTokensService;
import xyz.parti.catan.data.services.UsersService;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.helper.RxGuardian;

/**
 * Created by dalikim on 2017. 5. 9..
 */

public class LoginTask {
    private final AuthTokenService authTokenService;
    private Context context;
    private After afterLogin;
    private SessionManager session;
    private final RxGuardian rxGuardian;
    private Disposable loginDisposable;
    private Disposable ceateTokenPublisher;

    public LoginTask(Context context, LoginTask.After afterLogin) {
        this.context = context;
        this.afterLogin = afterLogin;
        this.session = new SessionManager(this.context.getApplicationContext());
        this.authTokenService = ServiceBuilder.createUnsignedService(AuthTokenService.class);
        this.rxGuardian = new RxGuardian();
    }

    public void cancel() {
        this.rxGuardian.unsubscribeAll();
    }

    public void destroy() {
        this.context = null;
        this.afterLogin = null;
        this.session = null;
        this.rxGuardian.unsubscribeAll();
    }

    public void loginSocial(String provider, String assertion) {
        loginSocial(provider, assertion, null);
    }

    public void loginSocial(String provider, String assertion, String secret) {
        String grantType = "assertion";
        Flowable<Response<PartiAccessToken>> flowable = authTokenService.getNewAccessTokenByAssertion(provider,
                assertion, secret, grantType, BuildConfig.PARTI_APP_ID, BuildConfig.PARTI_SECRET_KEY);
        process(flowable);
    }

    public void loginCredentials(String email, String password) {
        String grantType = "password";
        Flowable<Response<PartiAccessToken>> flowable = authTokenService.getNewAccessTokenByCredentials(email, password,
                grantType, BuildConfig.PARTI_APP_ID, BuildConfig.PARTI_SECRET_KEY);
        process(flowable);
    }

    private void process(Flowable<Response<PartiAccessToken>> flowable) {
        loginDisposable = rxGuardian.subscribe(loginDisposable,
                flowable.flatMap(
                        new Function<Response<PartiAccessToken>, Publisher<Response<User>>>() {
                            @Override
                            public Publisher<Response<User>> apply(@NonNull Response<PartiAccessToken> response) throws Exception {
                                if (response.isSuccessful()) {
                                    UsersService userService = ServiceBuilder.createNoRefreshService(UsersService.class, response.body());
                                    return userService.getCurrentUser();
                                } else {
                                    return Flowable.error(new NotFoundUserError());
                                }
                            }
                        }, new BiFunction<Response<PartiAccessToken>, Response<User>, Pair<PartiAccessToken, Response<User>>>() {
                            @Override
                            public Pair<PartiAccessToken, Response<User>> apply(@NonNull Response<PartiAccessToken> tokenResponse, @NonNull Response<User> userResponse) throws Exception {
                                return new Pair<>(tokenResponse.body(), userResponse);
                            }
                        })
                , new Consumer<Pair<PartiAccessToken, Response<User>>>() {
                    @Override
                    public void accept(@NonNull Pair<PartiAccessToken, Response<User>> pair) throws Exception {
                        PartiAccessToken token = pair.first;
                        Response<User> response = pair.second;

                        if (!response.isSuccessful()) {
                            afterLogin.onFail();
                            return;
                        }

                        User user = response.body();
                        session.createLoginSession(user, token);
                        if (BuildConfig.DEBUG) {
                            Log.d(Constants.TAG, user.nickname + "(으)로 로그인");
                        }
                        afterLogin.onSuccess();

                        saveDeviceToken();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if (error instanceof NotFoundUserError) {
                            afterLogin.onNotFoundUser();
                        } else {
                            afterLogin.onError(error);
                        }
                    }
                });
    }

    private void saveDeviceToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        DeviceTokensService service = ServiceBuilder.createNoRefreshService(DeviceTokensService.class, session.getPartiAccessToken());
        ceateTokenPublisher = rxGuardian.subscribe(ceateTokenPublisher, service.create(refreshedToken),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        Log.d(Constants.TAG, "Reset Instance ID");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        Log.e(Constants.TAG, "Error to reset Instance ID", error);
                    }
                });
    }

    private static class NotFoundUserError extends Throwable {}

    public interface After {
        void onSuccess();
        void onNotFoundUser();
        void onFail();
        void onError(Throwable e);
    }
}
