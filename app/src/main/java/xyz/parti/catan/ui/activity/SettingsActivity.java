package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.LinearLayout;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Setting;
import xyz.parti.catan.data.services.DeviceTokensService;
import xyz.parti.catan.data.services.SettingsService;
import xyz.parti.catan.helper.AppVersionHelper;
import xyz.parti.catan.helper.RxGuardian;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class SettingsActivity extends BaseActivity {
    private Disposable settingPublisher;
    private RxGuardian rxGuardian = new RxGuardian();
    private Setting settings = new Setting();

    @BindView(R.id.layout_prefs)
    LinearLayout prefsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setUp();
    }

    private void setUp() {
        SettingsService settingsService = ServiceBuilder.createUnsignedService(SettingsService.class);
        settingPublisher = rxGuardian.subscribe(settingPublisher,
                settingsService.getAll(),
                response -> {
                    if(response.isSuccessful()) {
                        settings = response.body();
                    } else {
                        Log.d(Constants.TAG, "Setting Info Error");
                    }
                    setUpFragment();
                    prefsLayout.setVisibility(View.VISIBLE);
                }, error -> {
                    Log.e(Constants.TAG, "Setting Info Exception", error);
                    setUpFragment();
                    prefsLayout.setVisibility(View.VISIBLE);
                }
        );
    }

    @Override
    protected void onDestroy() {
        if(rxGuardian != null) {
            rxGuardian.unsubscribeAll();
        }
        super.onDestroy();
    }

    private void setUpFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(settings));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        SettingsFragmentBasic settingsFragmentBasic = new SettingsFragmentBasic();
        settingsFragmentBasic.setArguments(bundle);
        ft.add(R.id.layout_pref_container_basic, settingsFragmentBasic);

        SettingsFragmentInfo settingsFragmentInfo = new SettingsFragmentInfo();
        ft.add(R.id.layout_pref_container_info, settingsFragmentInfo);

        SettingsFragmentHelp settingsFragmentHelp = new SettingsFragmentHelp();
        settingsFragmentHelp.setArguments(bundle);
        ft.add(R.id.layout_pref_container_help, settingsFragmentHelp);

        SettingsFragmentAccount settingsFragmentAccount = new SettingsFragmentAccount();
        ft.add(R.id.layout_pref_container_account, settingsFragmentAccount);

        ft.commit();
    }

    public static class SettingsFragmentBasic extends PreferenceFragmentCompat {
        private SessionManager session;
        private RxGuardian rxGuardian = new RxGuardian();
        private Disposable createDeviceTokenPublisher;
        private Disposable removeDeviceTokenPublisher;
        private DeviceTokensService deviceTokensService;
        private Setting setting;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences_basic);

            Parcelable settingArg = getArguments().getParcelable("setting");
            if(settingArg != null) {
                setting = Parcels.unwrap(settingArg);
            }

            session = new SessionManager(getActivity());
            deviceTokensService = ServiceBuilder.createNoRefreshService(DeviceTokensService.class, session.getPartiAccessToken());
        }

        @Override
        public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
            View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
            if(view != null) {
                setUpAction();
            }
            return view;
        }

        @Override
        public void onDestroy() {
            if(rxGuardian != null) {
                rxGuardian.unsubscribeAll();
            }
            super.onDestroy();
        }

        private void setUpAction() {
            setUpShowProfileAction();
            setUpReceivePushMessageAction();
        }

        private void setUpReceivePushMessageAction() {
            Preference pref = findPreference(Constants.PREF_VALUE_KEY_RECEIVE_PUSH_MESSAGE);
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                if((Boolean) newValue) {
                    String refreshedToken = getFirebaseInstanceToken();
                    createDeviceTokenPublisher = rxGuardian.subscribe(createDeviceTokenPublisher, deviceTokensService.create(refreshedToken),
                            response -> Log.d(Constants.TAG, "Create Instance ID"));
                } else {
                    String refreshedToken = getFirebaseInstanceToken();
                    removeDeviceTokenPublisher = rxGuardian.subscribe(removeDeviceTokenPublisher, deviceTokensService.destroy(refreshedToken),
                            response -> Log.d(Constants.TAG, "Destroy Instance ID"));
                }
                return true;
            });
        }

        private String getFirebaseInstanceToken() {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            return refreshedToken;
        }

        private void setUpShowProfileAction() {
            Preference pref = findPreference("pref_show_profile");
            pref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(setting.profile_url)));
                return false;
            });
        }
    }

    public static class SettingsFragmentHelp extends PreferenceFragmentCompat {
        private Setting setting;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences_help);

            Parcelable settingArg = getArguments().getParcelable("setting");
            if(settingArg != null) {
                setting = Parcels.unwrap(settingArg);
            }
        }

        @Override
        public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
            View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
            if(view != null) {
                setUpAction();
            }
            return view;
        }

        private void setUpAction() {
            setUpMenuAction("pref_help", setting.help_url);
            setUpMenuAction("pref_terms", setting.terms_url);
            setUpMenuAction("pref_privacy", setting.privacy_url);
        }

        private void setUpMenuAction(String pref_name, String url) {
            Preference pref = findPreference(pref_name);
            pref.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return false;
            });
        }
    }

    public static class SettingsFragmentInfo extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences_info);
        }

        @Override
        public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
            View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
            if(view != null) {
                setUpAction();
            }
            return view;
        }

        private void setUpAction() {
            Preference version = findPreference("pref_version");
            version.setSummary(new AppVersionHelper(getActivity()).getCurrentVerion());

            setUpLicenseAction();
        }

        private void setUpLicenseAction() {
            Preference pref = findPreference("pref_license");
            pref.setOnPreferenceClickListener(preference -> {
                new LibsBuilder()
                        .withActivityTheme(R.style.Theme_AppCompat_Light_NoActionBar)
                        .withActivityTitle(getResources().getString(R.string.license))
                        .withLicenseShown(true)
                        .withLicenseDialog(true)
                        .withLibraries("android_iconify", "parceler", "tedpermission", "fancybuttons", "stetho", "shimmer_android", "rxjava", "rxandroid", "material-dialogs", "FastAdapter", "matisse")
                        .start(getActivity());
                return false;
            });
        }
    }

    public static class SettingsFragmentAccount extends PreferenceFragmentCompat {
        RxGuardian rxGuardian = new RxGuardian();

        private SessionManager session;
        private Disposable removeDeviceTokenPublisher;
        private DeviceTokensService deviceTokensService;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences_account);

            session = new SessionManager(getActivity());
            deviceTokensService = ServiceBuilder.createNoRefreshService(DeviceTokensService.class, session.getPartiAccessToken());
        }

        @Override
        public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
            View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
            if(view != null) {
                setUpLogOutAction();
            }
            return view;
        }

        @Override
        public void onDestroy() {
            if(rxGuardian != null) {
                rxGuardian.unsubscribeAll();
            }
            super.onDestroy();
        }

        private void setUpLogOutAction() {
            Preference pref = findPreference("pref_logout");
            pref.setOnPreferenceClickListener(preference -> {
                confirmLogOut();
                return false;
            });
        }

        private void confirmLogOut() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.logout_confirm_title);
            builder.setMessage(R.string.logout_confirm_message);
            builder.setPositiveButton(R.string.yes, (dialog, whichs) -> {
                String refreshedToken = getFirebaseInstanceToken();
                removeDeviceTokenPublisher = rxGuardian.subscribe(removeDeviceTokenPublisher, deviceTokensService.destroy(refreshedToken), response -> {
                    Log.d(Constants.TAG, "Destroy Instance ID");
                    realLogout();
                }, error -> {
                    Log.e(Constants.TAG, "Error to destroy Instance ID", error);
                    realLogout();
                });
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        }

        private void realLogout() {
            logOutFacebook();
            logOutTwitter();
            session.logoutUser();
            clearCookies();
        }

        private void logOutTwitter() {
            TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();
            if (twitterSession != null) {
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
            }
        }

        public void clearCookies() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } else {
                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getActivity());
                cookieSyncMngr.startSync();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();
            }
        }

        private void logOutFacebook() {
            LoginManager.getInstance().logOut();
        }

        private String getFirebaseInstanceToken() {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            return refreshedToken;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
