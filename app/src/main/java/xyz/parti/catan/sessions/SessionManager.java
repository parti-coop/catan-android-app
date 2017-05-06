package xyz.parti.catan.sessions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.models.User;
import xyz.parti.catan.ui.activity.BaseActivity;
import xyz.parti.catan.ui.activity.LogInMenuActivity;

/**
 * Created by dalikim on 2017. 3. 26..
 */

public class SessionManager {
    private static final String CURRENT_SESSION_VERSION = "1";
    private static final String PREF_NAME = "SESSION";

    private static final String KEY_SESSION_VERSION = "session_version";
    private static final String KEY_IS_LOGIN = "IsLoggedIn";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER = "user";
    private static final String KEY_USER_NICKNAME = "user_nickname";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_EXPIRES_IN = "expires_in";

    private SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private Context context;

    public PartiAccessToken getPartiAccessToken() {
        if(isLoggedIn()) {
            PartiAccessToken result = new PartiAccessToken();
            result.access_token = pref.getString(KEY_ACCESS_TOKEN, null);
            result.refresh_token = pref.getString(KEY_REFRESH_TOKEN, null);
            result.token_type = pref.getString(KEY_TOKEN_TYPE, null);
            result.expires_in = pref.getLong(KEY_EXPIRES_IN, 0);
            if(BuildConfig.DEBUG) {
                Log.d(Constants.TAG, "getPartiAccessToken");
                Log.d(Constants.TAG, result.access_token);
            }
            return result;
        }
        return null;
    }

    public interface OnCheckListener {
        void onLoggedIn();
        void onLoggedOut();
    }

    public SessionManager(Context context){
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(User user, PartiAccessToken accessToken){
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "createLoginSession");
            Log.d(Constants.TAG, accessToken.access_token);
        }
        editor.putString(KEY_SESSION_VERSION, CURRENT_SESSION_VERSION);
        editor.putBoolean(KEY_IS_LOGIN, true);
        editor.putLong(KEY_USER_ID, user.id);
        editor.putString(KEY_USER,  new Gson().toJson(user));
        editor.putString(KEY_USER_NICKNAME, user.nickname);
        editor.putString(KEY_ACCESS_TOKEN, accessToken.access_token);
        editor.putString(KEY_REFRESH_TOKEN, accessToken.refresh_token);
        editor.putString(KEY_TOKEN_TYPE, accessToken.token_type);
        editor.putLong(KEY_EXPIRES_IN, accessToken.expires_in);
        editor.commit();
    }

    /**
     * Create login session
     * */
    public void updateAccessToken(PartiAccessToken accessToken){
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "updateAccessToken");
            Log.d(Constants.TAG, accessToken.access_token);
        }
        editor.putString(KEY_ACCESS_TOKEN, accessToken.access_token);
        editor.putString(KEY_REFRESH_TOKEN, accessToken.refresh_token);
        editor.putString(KEY_TOKEN_TYPE, accessToken.token_type);
        editor.putLong(KEY_EXPIRES_IN, accessToken.expires_in);
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(OnCheckListener callback){
        // Check login status
        if(!this.isLoggedIn()) {
            startLogin();
            callback.onLoggedOut();
        } else {
            callback.onLoggedIn();
        }
    }

    /**
     * Quick check for login
     * **/
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGIN, false) && CURRENT_SESSION_VERSION.equals(pref.getString(KEY_SESSION_VERSION, "-"));
    }

    /**
     * Clear session details
     */
    public void logoutUser(){
        clear();
        startLogin();

        Intent broadcast = new Intent();
        broadcast.setAction(BaseActivity.ACTION_LOGOUT);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(broadcast);
    }

    public void clear() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    public void startLogin() {
        Intent i = new Intent(this.context, LogInMenuActivity.class);
        // Closing all the Activities
        Log.d(Constants.TAG_TEST, "Closing all the Activities");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Add new Flag to start new Activity
        Log.d(Constants.TAG_TEST, "Add new Flag to start new Activity");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        context.startActivity(i);
    }

    public User getCurrentUser() {
        String json = pref.getString(KEY_USER, null);
        return json == null ? null : new Gson().fromJson(json, User.class);
    }
}
