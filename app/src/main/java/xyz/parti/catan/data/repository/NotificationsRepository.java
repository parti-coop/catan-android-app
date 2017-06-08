package xyz.parti.catan.data.repository;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.PushMessage;

/**
 * Created by dalikim on 2017. 5. 31..
 */

public class NotificationsRepository {
    private SharedPreferences preferences;

    public NotificationsRepository(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    private HashMap<Integer, List<PushMessage>> resetSingles(HashMap<Integer, List<PushMessage>> map) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(map);
        editor.putString(Constants.PREF_VALUE_KEY_NOTIFICATION_MAP, json);
        editor.apply();

        return map;
    }

    private List<PushMessage> resetMerged(List<PushMessage> pushMessages) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(pushMessages);
        editor.putString(Constants.PREF_VALUE_KEY_NOTIFICATION_MERGED, json);
        editor.apply();

        return pushMessages;
    }

    public HashMap<Integer, List<PushMessage>> fetchAllSingles() {
        String json = preferences.getString(Constants.PREF_VALUE_KEY_NOTIFICATION_MAP, "");
        Gson gson = new Gson();
        if (json.isEmpty()) {
            return new HashMap<>();
        } else {
            Type type = new TypeToken<HashMap<Integer, List<PushMessage>>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
    }

    public HashMap<Integer, List<PushMessage>> addSingle(int id, PushMessage message) {
        HashMap<Integer, List<PushMessage>> current = fetchAllSingles();
        List<PushMessage> pushMessages = new ArrayList<>();
        pushMessages.add(message);
        current.put(id, pushMessages);
        resetSingles(current);
        return current;
    }

    public HashMap<Integer, List<PushMessage>> destroySingle(int id) {
        HashMap<Integer, List<PushMessage>> current = fetchAllSingles();
        current.remove(new Integer(id));
        resetSingles(current);
        return current;
    }

    public void destroy(int id) {
        if(id == Constants.MERGED_NOTIFICATION_ID) {
            destroyMerged();
        } else {
            destroySingle(id);
        }
    }

    public void destroyAllSingles() {
        resetSingles(new HashMap<Integer, List<PushMessage>>());
    }

    public void destroyMerged() {
        resetMerged(new ArrayList<PushMessage>());
    }

    public void mergeAll(PushMessage newPushMessage) {
        List<PushMessage> current = fetchAllPushMessages();
        current.add(newPushMessage);
        resetMerged(current);
    }

    public List<PushMessage> fetchAllPushMessages() {
        List<PushMessage> result = new ArrayList<>();

        HashMap<Integer, List<PushMessage>> current = fetchAllSingles();
        for(List<PushMessage> messages : current.values()) {
            for(PushMessage pushMessage : messages) {
                if(!existsPushMessageIn(result, pushMessage)) {
                    result.add(pushMessage);
                }
            }
        }
        for(PushMessage pushMessage : fetchMergedPushMessages()) {
            if(!existsPushMessageIn(result, pushMessage)) {
                result.add(pushMessage);
            }
        }
        return result;
    }

    private boolean existsPushMessageIn(List<PushMessage> pushMessages, PushMessage targetMessage) {
        for(PushMessage pushMessage : pushMessages) {
            if(targetMessage.id == pushMessage.id) {
                return true;
            }
        }
        return false;
    }

    private List<PushMessage> fetchMergedPushMessages() {
        String json = preferences.getString(Constants.PREF_VALUE_KEY_NOTIFICATION_MERGED, "");
        Gson gson = new Gson();
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            Type type = new TypeToken<List<PushMessage>>() {
            }.getType();
            return gson.fromJson(json, type);
        }
    }
}
