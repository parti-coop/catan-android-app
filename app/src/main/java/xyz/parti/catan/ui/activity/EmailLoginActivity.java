package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.KeyboardHelper;
import xyz.parti.catan.ui.task.LoginTask;
import xyz.parti.catan.ui.view.ProgressToggler;

public class EmailLoginActivity extends BaseActivity {
    // UI references.
    @BindView(R.id.textview_email)
    EditText emailTextView;
    @BindView(R.id.edittext_password)
    EditText passwordEditText;
    @BindView(R.id.progressbar_status)
    ProgressBar statusProgressBar;
    @BindView(R.id.scrollview_form)
    View formView;
    @BindView(R.id.button_submit)
    View submitButton;

    private LoginTask partiLoginTask;
    private ProgressToggler progressToggler;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        ButterKnife.bind(EmailLoginActivity.this);

        partiLoginTask = setUpLoginTask();
        progressToggler = new ProgressToggler(formView, statusProgressBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @NonNull
    private LoginTask setUpLoginTask() {
        return new LoginTask(this, new LoginTask.After() {
            @Override
            public void onSuccess() {
                progressToggler.toggle(false);
                Intent i = new Intent(EmailLoginActivity.this.getApplicationContext(), MainActivity.class);
                EmailLoginActivity.this.startActivity(i);
                EmailLoginActivity.this.finish();
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
    
    @OnEditorAction(R.id.edittext_password)
    public boolean editPassword(int actionId) {
        if (actionId == R.id.login || actionId == EditorInfo.IME_NULL) {
            attemptLogin();
            return true;
        }
        return false;
    }

    @OnClick(R.id.button_submit)
    public void attemptLogin() {
        // Reset errors.
        emailTextView.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = emailTextView.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailTextView.setError(getString(R.string.error_field_required));
            focusView = emailTextView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailTextView.setError(getString(R.string.error_invalid_email));
            focusView = emailTextView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        progressToggler.toggle(true);
        KeyboardHelper.hideKey(this);
        partiLoginTask.loginCredentials(email, password);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}

