package xyz.parti.catan.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.PushMessage;


public class LastPostFeedPreference {
    private SharedPreferences preferences;

    public LastPostFeedPreference(Context context) {
        this.preferences = context.getSharedPreferences(Constants.PREF_NAME_LAST_POST_FEED, Context.MODE_PRIVATE);
    }

    public void save(long postFeedId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_VALUE_KEY_LAST_POST_FEED, String.valueOf(postFeedId));
        editor.apply();
    }

    public boolean isNewbie() {
        return !preferences.contains(Constants.PREF_VALUE_KEY_LAST_POST_FEED);
    }

    public long fetch() {
        return Long.parseLong(preferences.getString(Constants.PREF_VALUE_KEY_LAST_POST_FEED, "" + Constants.POST_FEED_DASHBOARD));
    }
}
