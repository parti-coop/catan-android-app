package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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
import xyz.parti.catan.helper.StyleHelper;
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
    @BindView(R.id.layout_coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.scrollview_form)
    View formView;
    @BindView(R.id.button_submit)
    View submitButton;

    private LoginTask partiLoginTask;
    private ProgressToggler progressToggler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        ButterKnife.bind(EmailLoginActivity.this);

        partiLoginTask = setupLoginTask();
        progressToggler = new ProgressToggler(formView, statusProgressBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause() {
        this.partiLoginTask.cancel();
        super.onPause();
    }

    public void onDestroy() {
        this.partiLoginTask.destroy();
        super.onDestroy();
    }

    @NonNull
    private LoginTask setupLoginTask() {
        return new LoginTask(this, new LoginTask.After() {
            @Override
            public void onSuccess() {
                progressToggler.toggle(false);
                Intent i = new Intent(EmailLoginActivity.this.getApplicationContext(), MainActivity.class);
                EmailLoginActivity.this.startActivity(i);
                EmailLoginActivity.this.finish();
            }

            @Override
            public void onNotFoundUser() {
                progressToggler.toggle(false);
                Snackbar snackbar = Snackbar.make(coordinatorLayout, String.format(getResources().getString(R.string.login_password_not_found_user)), 30 * 1000)
                        .setAction(R.string.ok,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://parti.xyz/users/pre_sign_up"));
                                        startActivity(i);
                                    }
                                });
                StyleHelper.forSnackbar(EmailLoginActivity.this, snackbar);
                snackbar.show();
            }

            @Override
            public void onFail() {
                reportError(getResources().getString(R.string.login_fail));
                progressToggler.toggle(false);
            }

            @Override
            public void onError(Throwable e) {
                reportError(e);
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
        formView.post(new Runnable() {
            @Override
            public void run() {
                new KeyboardHelper(EmailLoginActivity.this).hideKey();
            }
        } );
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

