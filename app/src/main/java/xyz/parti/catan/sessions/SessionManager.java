package xyz.parti.catan.sessions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import xyz.parti.catan.ui.activity.LoginMenuActivity;

/**
 * Created by dalikim on 2017. 3. 26..
 */

public class SessionManager {
    private static final String PREF_NAME = "SESSION";

    private static final String KEY_IS_LOGIN = "IsLoggedIn";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NICKNAME = "user_nickname";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRES_IN = "expires_in";

    private SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private Context context;

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
    public void createLoginSession(long user_id, String user_nickname, String access_token, String refresh_token, long expires_in){
        editor.putBoolean(KEY_IS_LOGIN, true);
        editor.putLong(KEY_USER_ID, user_id);
        editor.putString(KEY_USER_NICKNAME, user_nickname);
        editor.putString(KEY_ACCESS_TOKEN, access_token);
        editor.putString(KEY_REFRESH_TOKEN, refresh_token);
        editor.putLong(KEY_EXPIRES_IN, expires_in);
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
        return pref.getBoolean(KEY_IS_LOGIN, false);
    }

    /**
     * Clear session details
     */
    public void logoutUser(Activity activity){
        clear();
        startLogin();
        activity.finish();
    }

    public void clear() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    public void startLogin() {
        Intent i = new Intent(this.context, LoginMenuActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Staring Login Activity
        context.startActivity(i);
    }
}
