package xyz.parti.catan.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
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
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.StyleHelper;
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
    private AlertDialog googleServiceErrorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        partiLoginTask = setupLoginTask();
        facebookAuthClient = initFacebook();
        twitterAuthClient = initTwitter();
        progressToggler = new ProgressToggler(panelLayout, statusProgressBar);

        loginByEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LogInActivity.this, EmailLoginActivity.class);
                LogInActivity.this.startActivity(i);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://parti.xyz/users/pre_sign_up"));
                startActivity(i);
            }
        });
        decorView = getWindow().getDecorView();
    }

    @Override
    public void onPause() {
        if(partiLoginTask != null) {
            this.partiLoginTask.cancel();
        }
        super.onPause();
    }

    public void onDestroy() {
        if(partiLoginTask != null) {
            this.partiLoginTask.destroy();
        }
        if(this.googleServiceErrorDialog != null) {
            this.googleServiceErrorDialog.dismiss();
        }
        super.onDestroy();
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
    private LoginTask setupLoginTask() {
        return new LoginTask(this, new LoginTask.After() {
            @Override
            public void onSuccess() {
                progressToggler.toggle(false);
                Intent i = new Intent(LogInActivity.this.getApplicationContext(), MainActivity.class);
                LogInActivity.this.startActivity(i);
                LogInActivity.this.finish();
            }

            @Override
            public void onNotFoundUser() {
                progressToggler.toggle(false);
                Snackbar snackbar = Snackbar.make(panelLayout, getResources().getString(R.string.login_social_not_found_user), 30 * 1000)
                        .setAction(R.string.ok,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://parti.xyz/users/pre_sign_up"));
                                        startActivity(i);
                                    }
                                });
                StyleHelper.forSnackbar(LogInActivity.this, snackbar);
                snackbar.show();
            }

            @Override
            public void onFail() {
                reportError(getResources().getString(R.string.login_fail));
                progressToggler.toggle(false);
            }

            @Override
            public void onError(Throwable e) {
                reportError(getResources().getString(R.string.login_fail));
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

    private void showGoogleServiceErrorMessage() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.common_google_play_services_update_title)
                .setMessage(R.string.google_play_services_update_text)        // 메세지 설정
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE)));
                            }
                        } catch (android.content.ActivityNotFoundException ignore) {
                            showMessage(R.string.not_support_device);
                            finish();
                        }
                    }
                });
        googleServiceErrorDialog = builder.create();
        googleServiceErrorDialog.show();
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
                    CatanLog.d("트위터 로그인 성공");
                }
                TwitterAuthToken accessToken = loginResult.data.getAuthToken();
                partiLoginTask.loginSocial("twitter", accessToken.token, accessToken.secret);
            }

            @Override
            public void failure(TwitterException error) {
                progressToggler.toggle(false);
                CatanLog.e(error.getMessage(), error);
            }
        });
    }

    @OnClick(R.id.button_login_by_google)
    public void loginGoogle() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                showGoogleServiceErrorMessage();
            } else {
                showMessage(R.string.not_support_device);
            }
            return;
        }

        if(this.googleApiClient == null) {
            this.googleApiClient = initGoogle();
        }

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
        if(result.getSignInAccount() == null) {
            CatanLog.d("구글 인증 정보 없음");
        }
        if(result.isSuccess() && result.getSignInAccount() != null) {
            CatanLog.d("구글 로그인 성공");
            String token = result.getSignInAccount().getIdToken();
            partiLoginTask.loginSocial("google_oauth2", token);
        } else {
            CatanLog.d(result.getStatus().toString());
            progressToggler.toggle(false);
            showMessage(R.string.error_google_login);
        }
    }

    private CallbackManager initFacebook() {
        CallbackManager callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                CatanLog.d("페이스북 로그인 성공");
                AccessToken accessToken = loginResult.getAccessToken();
                partiLoginTask.loginSocial("facebook", accessToken.getToken());
            }

            @Override
            public void onCancel() {
                progressToggler.toggle(false);
                showMessage(R.string.error_login);
            }

            @Override
            public void onError(FacebookException error) {
                progressToggler.toggle(false);
                showMessage(R.string.error_login);
                CatanLog.e(error.getMessage(), error);
            }
        });
        return callbackManager;
    }

    private TwitterAuthClient initTwitter() {
        return new TwitterAuthClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        CatanLog.d("onConnectionFailed:" + connectionResult);
        reportError(connectionResult.getErrorMessage());
        progressToggler.toggle(false);
    }

    @Override
    public boolean willFinishIfLogOut() {
        return false;
    }
}
