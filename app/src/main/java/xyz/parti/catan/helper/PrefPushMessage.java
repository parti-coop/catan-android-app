package xyz.parti.catan.helper;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;

import xyz.parti.catan.Constants;


public class PrefPushMessage {
    public static boolean isReceivable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.PREF_VALUE_KEY_RECEIVE_PUSH_MESSAGE, false);
    }

    public static void setReceivable(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Constants.PREF_VALUE_KEY_RECEIVE_PUSH_MESSAGE, true).commit();
    }
}
