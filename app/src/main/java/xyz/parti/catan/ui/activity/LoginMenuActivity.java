package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;

public class LoginMenuActivity extends AppCompatActivity {
    @BindView(R.id.loginByEmailButton)
    View loginByEmailButton;
    @BindView(R.id.loginByFacebookButton)
    View loginByFacebookButton;
    @BindView(R.id.signUpLink)
    View signUpLink;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbackManager = initFacebook();
        setContentView(R.layout.activity_login_menu);
        ButterKnife.bind(LoginMenuActivity.this);

        loginByEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginMenuActivity.this, EmailLoginActivity.class);
                LoginMenuActivity.this.startActivity(i);
            }
        });
        loginByFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(LoginMenuActivity.this, Arrays.asList("email", "user_friends"));
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://parti.xyz/users/pre_sign_up"));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private CallbackManager initFacebook() {
        CallbackManager callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(Constants.TAG, "페이스북 로그인 성공");
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        return callbackManager;
    }
}
