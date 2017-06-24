package xyz.parti.catan.ui.activity;

import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.FrameLayout;

import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonNull;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Setting;
import xyz.parti.catan.data.services.DeviceTokensService;
import xyz.parti.catan.data.services.SettingsService;
import xyz.parti.catan.helper.AppVersionHelper;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.RxGuardian;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class SettingsActivity extends BaseActivity {
    private Disposable settingPublisher;
    private RxGuardian rxGuardian = new RxGuardian();
    private Setting settings = new Setting();

    @BindView(R.id.layout_prefs)
    FrameLayout prefsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setup();
    }

    private void setup() {
        SettingsService settingsService = ServiceBuilder.createUnsignedService(SettingsService.class);
        settingPublisher = rxGuardian.subscribe(settingPublisher,
                settingsService.getAll(),
                new Consumer<Response<Setting>>() {
                    @Override
                    public void accept(@NonNull Response<Setting> response) throws Exception {
                        if (response.isSuccessful()) {
                            settings = response.body();
                        } else {
                            CatanLog.d("Setting Info Error");
                        }
                        setupFragment();
                        prefsLayout.setVisibility(View.VISIBLE);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        CatanLog.e("Setting Info Exception", error);
                        setupFragment();
                        prefsLayout.setVisibility(View.VISIBLE);
                    }
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

    private void setupFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("setting", Parcels.wrap(settings));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        SettingsFragmentBasic settingsFragmentBasic = new SettingsFragmentBasic();
        settingsFragmentBasic.setArguments(bundle);
        ft.add(R.id.layout_prefs, settingsFragmentBasic);

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
            addPreferencesFromResource(R.xml.preferences_info);
            addPreferencesFromResource(R.xml.preferences_help);
            addPreferencesFromResource(R.xml.preferences_account);

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
                setupAction();
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

        private void setupAction() {
            setupShowProfileAction();
            setupReceivePushMessageAction();

            Preference version = findPreference("pref_version");
            version.setSummary(AppVersionHelper.getCurrentVerion(getView().getContext()));

            setupLicenseAction();
            setupMenuAction("pref_help", setting.help_url);
            setupMenuAction("pref_terms", setting.terms_url);
            setupMenuAction("pref_privacy", setting.privacy_url);
            setupLogOutAction();
        }

        private void setupReceivePushMessageAction() {
            Preference pref = findPreference(Constants.PREF_VALUE_KEY_RECEIVE_PUSH_MESSAGE);
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue) {
                        String refreshedToken = getFirebaseInstanceToken();
                        createDeviceTokenPublisher = rxGuardian.subscribe(createDeviceTokenPublisher, deviceTokensService.create(refreshedToken, BuildConfig.APPLICATION_ID),
                                new Consumer<Response<JsonNull>>() {
                                    @Override
                                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                                        CatanLog.d("Create Instance ID");
                                    }
                                });
                    } else {
                        String refreshedToken = getFirebaseInstanceToken();
                        removeDeviceTokenPublisher = rxGuardian.subscribe(removeDeviceTokenPublisher, deviceTokensService.destroy(refreshedToken),
                                new Consumer<Response<JsonNull>>() {
                                    @Override
                                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                                        CatanLog.d("Destroy Instance ID");
                                    }
                                });
                    }
                    return true;
                }
            });
        }

        private void setupShowProfileAction() {
            Preference pref = findPreference("pref_show_profile");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                                                  @Override
                                                  public boolean onPreferenceClick(Preference preference) {
                                                      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(setting.profile_url)));
                                                      return false;
                                                  }
                                              });
        }

        private void setupLicenseAction() {
            Preference pref = findPreference("pref_license");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new LibsBuilder()
                            .withActivityTheme(R.style.Theme_AppCompat_Light_NoActionBar)
                            .withActivityTitle(getResources().getString(R.string.license))
                            .withLicenseShown(true)
                            .withLicenseDialog(true)
                            .withLibraries("android_iconify", "parceler", "tedpermission", "fancybuttons", "stetho", "shimmer_android", "rxjava", "rxandroid", "material-dialogs", "FastAdapter", "matisse")
                            .start(getActivity());
                    return false;
                }
            });
        }

        private void setupMenuAction(String pref_name, final String url) {
            Preference pref = findPreference(pref_name);
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return false;
                }
            });
        }

        private void setupLogOutAction() {
            Preference pref = findPreference("pref_logout");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    confirmLogOut();
                    return false;
                }
            });
        }

        private void confirmLogOut() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.logout_confirm_title);
            builder.setMessage(R.string.logout_confirm_message);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichs) {
                    String refreshedToken = getFirebaseInstanceToken();
                    removeDeviceTokenPublisher = rxGuardian.subscribe(removeDeviceTokenPublisher, deviceTokensService.destroy(refreshedToken),
                            new Consumer<Response<JsonNull>>() {
                                @Override
                                public void accept(@NonNull Response<JsonNull> response) throws Exception {
                                    CatanLog.d("Destroy Instance ID");
                                    realLogout();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable error) throws Exception {
                                    CatanLog.e("Error to destroy Instance ID", error);
                                    realLogout();
                                }
                            });
                }});
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
