package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;

public class LoginMenuActivity extends AppCompatActivity {
    @BindView(R.id.loginByEmailButton)
    View loginByEmailButton;
    @BindView(R.id.signUpLink)
    View signUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_menu);
        ButterKnife.bind(LoginMenuActivity.this);

        loginByEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginMenuActivity.this, EmailLoginActivity.class);
                LoginMenuActivity.this.startActivity(i);
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
}
