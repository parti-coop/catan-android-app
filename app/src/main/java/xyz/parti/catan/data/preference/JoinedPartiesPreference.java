package xyz.parti.catan.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import xyz.parti.catan.Constants;

/**
 * Created by dalikim on 2017. 6. 9..
 */

public class JoinedPartiesPreference {
    private SharedPreferences preferences;

    public JoinedPartiesPreference(Context context) {
        this.preferences = context.getSharedPreferences(Constants.PREF_NAME_JOINED_PARTIES, Context.MODE_PRIVATE);
    }

    public void sync() {
        saveCurrent(fetchChangedAt());
    }

    public void saveChangedAt(long lastTimestamp) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_VALUE_KEY_CHANGED_AT_TIMESTAMPE, String.valueOf(lastTimestamp));
        editor.apply();
    }

    private void saveCurrent(long lastTimestamp) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_VALUE_KEY_CURRENT_TIMESTAMPE, String.valueOf(lastTimestamp));
        editor.apply();
    }

    public long fetchChangedAt() {
        return Long.parseLong(preferences.getString(Constants.PREF_VALUE_KEY_CHANGED_AT_TIMESTAMPE, "0"));
    }

    public long fetchCurrent() {
        return Long.parseLong(preferences.getString(Constants.PREF_VALUE_KEY_CURRENT_TIMESTAMPE, "-1"));
    }

    public boolean needToUpgrade() {
        return fetchCurrent() < fetchChangedAt();
    }

    public void reset() {
        saveCurrent(-1);
    }
}
