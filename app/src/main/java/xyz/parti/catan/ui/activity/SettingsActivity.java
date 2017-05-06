package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.services.SettingsService;
import xyz.parti.catan.sessions.SessionManager;

import static android.R.style.ThemeOverlay;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class SettingsActivity extends BaseActivity {
    @BindView(R.id.textview_version)
    TextView versionTextView;

    private SessionManager session;
    private SettingsService settingsService;
    private JsonObject setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        session = new SessionManager(getApplicationContext());
        settingsService = ServiceGenerator.createService(SettingsService.class, session);

        ButterKnife.bind(this);

        versionTextView.setText(Constants.VERSION);
    }

    @OnClick(R.id.layout_setting_profile)
    public void showSettingProfile(View view) {
        openMenu("profile_url", "https://parti.xyz/users/edit");
    }

    @OnClick(R.id.layout_help)
    public void showHelp(View view) {
        openMenu("help_url", "http://docs.parti.xyz/docs/help/");
    }

    @OnClick(R.id.layout_terms)
    public void showTerms(View view) {
        openMenu("terms_url", "https://parti.xyz/terms");
    }

    @OnClick(R.id.layout_privacy)
    public void showPolicy(View view) {
        openMenu("privacy_url", "https://parti.xyz/privacy");
    }

    @OnClick(R.id.layout_license)
    public void showAbout(View view) {
        //openMenu("app_license_url", String.format("https://parti.xyz/app/license?version=%s", Constants.VERSION));
        new LibsBuilder()
                //provide a style (optional) style/Base.Theme.AppCompat.Light.DialogWhenLarge(LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                .withActivityTheme(R.style.Theme_AppCompat_Light_NoActionBar)
                .withActivityTitle(getResources().getString(R.string.license))
                .withLicenseShown(true)
                .withLicenseDialog(true)
                .withLibraries("android_iconify", "parceler", "tedpermission", "fancybuttons", "stetho", "shimmer_android")
                //start the activity
                .start(this);
    }

    private boolean startActivityWith(JsonObject setting, String fieldName) {
        return startActivityWith(setting, fieldName, null);
    }

    private boolean startActivityWith(JsonObject setting, String fieldName, String fallbackUrl) {
        if (setting != null && setting.get(fieldName).getAsString() != null) {
            Log.d(Constants.TAG_LOCAL, setting.toString());
            startActivityForUrl(setting.get(fieldName).getAsString());
            return true;
        }
        Log.d(Constants.TAG_LOCAL, "XXX");

        if(fallbackUrl == null) {
            return false;
        }

        startActivityForUrl(fallbackUrl);
        return true;
    }

    private void openMenu(final String menuName, final String fallbackUrl) {
        boolean result = startActivityWith(setting, menuName);
        if(result) {
            return;
        }

        Call<JsonElement> call = settingsService.getAll();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if(response.isSuccessful()) {
                    setting = response.body().getAsJsonObject();
                    startActivityWith(setting, menuName, fallbackUrl);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "load Setting error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    private void startActivityForUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @OnClick(R.id.textview_logout)
    public void logOut(View view) {
        session.logoutUser();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
