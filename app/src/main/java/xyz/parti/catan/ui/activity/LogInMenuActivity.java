package xyz.parti.catan.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import io.reactivex.Flowable;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.services.AuthTokenService;
import xyz.parti.catan.data.services.UsersService;
import xyz.parti.catan.helper.ReportHelper;

public class LogInMenuActivity extends BaseActivity {
    @BindView(R.id.button_login_by_email)
    View loginByEmailButton;
    @BindView(R.id.button_sign_up)
    View signUpButton;
    @BindView(R.id.progressbar_status)
    View statusProgressBar;
    @BindView(R.id.layout_panel)
    View panelLayout;

    private CallbackManager facebookAuthClient;
    private TwitterAuthClient twitterAuthClient;
    private SessionManager session;
    private AuthTokenService authTokenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_menu);

        ButterKnife.bind(this);

        facebookAuthClient = initFacebook();
        twitterAuthClient = initTwitter();

        session = new SessionManager(this.getApplicationContext());
        authTokenService = ServiceBuilder.createUnsignedService(AuthTokenService.class);

        loginByEmailButton.setOnClickListener(view -> {
            Intent i = new Intent(LogInMenuActivity.this, EmailLoginActivity.class);
            LogInMenuActivity.this.startActivity(i);
        });
        signUpButton.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://parti.xyz/users/pre_sign_up"));
            startActivity(i);
        });
    }

    @OnClick(R.id.button_login_by_facebook)
    public void loginFacebook() {
        showProgress(true);
        LoginManager.getInstance().logInWithReadPermissions(LogInMenuActivity.this, Collections.singletonList("email"));
    }

    @OnClick(R.id.button_login_by_twitter)
    public void loginTwitter() {
        showProgress(true);
        twitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> loginResult) {
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "트위터 로그인 성공");
                }
                TwitterAuthToken accessToken = loginResult.data.getAuthToken();
                partiLogin("twitter", accessToken.token, accessToken.secret);
            }

            @Override
            public void failure(TwitterException error) {
                showProgress(false);
                Log.e(Constants.TAG, error.getMessage(), error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE == requestCode) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        } else {
            facebookAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    private CallbackManager initFacebook() {
        CallbackManager callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "페이스북 로그인 성공");
                }
                AccessToken accessToken = loginResult.getAccessToken();
                partiLogin("facebook", accessToken.getToken());
            }

            @Override
            public void onCancel() {
                Log.d(Constants.TAG_TEST, "취소");
                showProgress(false);
                Toast.makeText(LogInMenuActivity.this.getApplicationContext(), R.string.error_login, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                showProgress(false);
                Log.d(Constants.TAG_TEST, "취소2");
                Toast.makeText(LogInMenuActivity.this.getApplicationContext(), R.string.error_login, Toast.LENGTH_LONG).show();
                Log.e(Constants.TAG, error.getMessage(), error);
            }
        });
        return callbackManager;
    }

    private TwitterAuthClient initTwitter() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        return new TwitterAuthClient();
    }

    private void partiLogin(String provider, String assertion) {
        partiLogin(provider, assertion, null);
    }

    private void partiLogin(String provider, String assertion, String secret) {
        String grantType = "assertion";
        Flowable<Response<PartiAccessToken>> flowable = authTokenService.getNewAccessToken(provider,
                assertion, secret, grantType, BuildConfig.PARTI_APP_ID, BuildConfig.PARTI_SECRET_KEY);
        ServiceBuilder.basicOn(flowable
                .flatMap(response -> {
                        if(response.isSuccessful()) {
                            UsersService userService = ServiceBuilder.createNoRefreshService(UsersService.class, response.body());
                            return userService.getCurrentUser();
                        } else {
                            return Flowable.error(new NotFoundUserError());
                        }
                    }, (tokenResponse, userResponse) -> new Pair<>(tokenResponse.body(), userResponse))
                ).subscribe(
                    pair -> {
                        PartiAccessToken token = pair.first;
                        Response<User> response = pair.second;

                        if(! response.isSuccessful()) {
                            ReportHelper.wtf(this, getResources().getString(R.string.login_fail));
                            return;
                        }

                        User user = response.body();
                        session.createLoginSession(user, token);
                        if(BuildConfig.DEBUG) {
                            Log.d(Constants.TAG, user.nickname + "(으)로 로그인");
                        }
                        showProgress(false);

                        Intent i = new Intent(LogInMenuActivity.this.getApplicationContext(), MainActivity.class);
                        LogInMenuActivity.this.startActivity(i);
                        LogInMenuActivity.this.finish();
                    }, error -> {
                        if(error instanceof NotFoundUserError) {
                            ReportHelper.wtf(this, "TODO: 가입화면으로 넘겨야함");
                        } else {
                            ReportHelper.wtf(this, error);
                        }
                        showProgress(false);
                    });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        panelLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        panelLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                panelLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        statusProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        statusProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                statusProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class NotFoundUserError extends Throwable {}

    @Override
    public boolean willFinishIfLogOut() {
        return false;
    }
}
