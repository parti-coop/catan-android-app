package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.ui.task.LoginTask;
import xyz.parti.catan.ui.view.ProgressToggler;

public class LogInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 9001;

    @BindView(R.id.button_login_by_email)
    View loginByEmailButton;
    @BindView(R.id.button_sign_up)
    View signUpButton;
    @BindView(R.id.progressbar_status)
    ProgressBar statusProgressBar;
    @BindView(R.id.layout_panel)
    View panelLayout;

    private CallbackManager facebookAuthClient;
    private TwitterAuthClient twitterAuthClient;
    private GoogleApiClient googleApiClient;

    private LoginTask partiLoginTask;
    private ProgressToggler progressToggler;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        partiLoginTask = setUpLoginTask();
        facebookAuthClient = initFacebook();
        twitterAuthClient = initTwitter();
        googleApiClient = initGoogle();
        progressToggler = new ProgressToggler(panelLayout, statusProgressBar);

        loginByEmailButton.setOnClickListener(view -> {
            Intent i = new Intent(LogInActivity.this, EmailLoginActivity.class);
            LogInActivity.this.startActivity(i);
        });
        signUpButton.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://parti.xyz/users/pre_sign_up"));
            startActivity(i);
        });
        decorView = getWindow().getDecorView();
    }

    @Override
    public void onPause() {
        this.partiLoginTask.cancel();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // super.onWindowFocusChanged(hasFocus);
        if( hasFocus ) {
            int uiOption = getWindow().getDecorView().getSystemUiVisibility();
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                uiOption |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
                uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility( uiOption );
        }
    }

    @NonNull
    private LoginTask setUpLoginTask() {
        return new LoginTask(this, new LoginTask.After() {
            @Override
            public void onSuccess() {
                progressToggler.toggle(false);
                Intent i = new Intent(LogInActivity.this.getApplicationContext(), MainActivity.class);
                LogInActivity.this.startActivity(i);
                LogInActivity.this.finish();
            }

            @Override
            public void onFail() {
                progressToggler.toggle(false);
            }

            @Override
            public void onError() {
                progressToggler.toggle(false);
            }
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
        progressToggler.toggle(true);
        LoginManager.getInstance().logInWithReadPermissions(LogInActivity.this, Collections.singletonList("email"));
    }

    @OnClick(R.id.button_login_by_twitter)
    public void loginTwitter() {
        progressToggler.toggle(true);
        twitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> loginResult) {
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "트위터 로그인 성공");
                }
                TwitterAuthToken accessToken = loginResult.data.getAuthToken();
                partiLoginTask.loginSocial("twitter", accessToken.token, accessToken.secret);
            }

            @Override
            public void failure(TwitterException error) {
                progressToggler.toggle(false);
                Log.e(Constants.TAG, error.getMessage(), error);
            }
        });
    }

    @OnClick(R.id.button_login_by_google)
    public void loginGoogle() {
        progressToggler.toggle(true);
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
            partiLoginTask.loginSocial("google_oauth2", token);

            Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(status -> Log.d(Constants.TAG_TEST, "REVOKE"));
        } else {
            Log.d(Constants.TAG, result.getStatus().toString());
            progressToggler.toggle(false);
            Toast.makeText(LogInActivity.this.getApplicationContext(), R.string.error_login, Toast.LENGTH_LONG).show();
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
                partiLoginTask.loginSocial("facebook", accessToken.getToken());
            }

            @Override
            public void onCancel() {
                Log.d(Constants.TAG_TEST, "취소");
                progressToggler.toggle(false);
                Toast.makeText(LogInActivity.this.getApplicationContext(), R.string.error_login, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                progressToggler.toggle(false);
                Log.d(Constants.TAG_TEST, "취소2");
                Toast.makeText(LogInActivity.this.getApplicationContext(), R.string.error_login, Toast.LENGTH_LONG).show();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(Constants.TAG, "onConnectionFailed:" + connectionResult);
        ReportHelper.wtf(this, connectionResult.getErrorMessage());
        progressToggler.toggle(false);
    }

    @Override
    public boolean willFinishIfLogOut() {
        return false;
    }
}
