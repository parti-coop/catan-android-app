package xyz.parti.catan.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

public class LogInMenuActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 9001;

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
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_menu);

        ButterKnife.bind(this);

        facebookAuthClient = initFacebook();
        twitterAuthClient = initTwitter();
        googleApiClient = initGoogle();

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

    private GoogleApiClient initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestProfile()
                .requestEmail()
                .build();
        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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

    @OnClick(R.id.button_login_by_google)
    public void loginGoogle() {
        showProgress(true);
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE == requestCode) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        } if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            googleActivityResult(result);
        } else {
            facebookAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void googleActivityResult(GoogleSignInResult result) {
        Log.d(Constants.TAG_TEST, result.getStatus().toString());
        if(result.getSignInAccount() == null) {
            Log.d(Constants.TAG, "구글 인증 정보 없음");
        }
        if(result.isSuccess() && result.getSignInAccount() != null) {
            if(BuildConfig.DEBUG) {
                Log.d(Constants.TAG, "구글 로그인 성공");
            }
            String token = result.getSignInAccount().getIdToken();
            partiLogin("google_oauth2", token);

            Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(status -> Log.d(Constants.TAG_TEST, "REVOKE"));
        } else {
            Log.d(Constants.TAG, result.getStatus().toString());
            showProgress(false);
            Toast.makeText(LogInMenuActivity.this.getApplicationContext(), R.string.error_login, Toast.LENGTH_LONG).show();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(Constants.TAG, "onConnectionFailed:" + connectionResult);
        ReportHelper.wtf(this, connectionResult.getErrorMessage());
        showProgress(false);
    }

    private class NotFoundUserError extends Throwable {}

    @Override
    public boolean willFinishIfLogOut() {
        return false;
    }
}
