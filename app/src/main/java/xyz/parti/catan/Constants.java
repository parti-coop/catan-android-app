package xyz.parti.catan;

/**
 * Created by dalikim on 2017. 3. 26..
 */

public class Constants {
    public static final String TAG = "Catan";
    public static final String TAG_TEST = "Catan-Local";

    public static final String STORAGE_PROVIDER_AUTHORITY = "xyz.parti.catan.fileprovider";

    public static final String PREF_VALUE_KEY_RECEIVE_PUSH_MESSAGE = "pref_receive_push_message";
    public static final String PREF_VALUE_KEY_NOTIFICATION_MAP = "pref_notification_map";
    public static final String PREF_VALUE_KEY_NOTIFICATION_MERGED = "pref_notification_merged";
    public static final String PREF_VALUE_KEY_LAST_POST_FEED = "pref_last_post_feed";
    public static final String PREF_VALUE_KEY_CHANGED_AT_TIMESTAMPE = "pref_changed_at_timestamp";
    public static final String PREF_VALUE_KEY_CURRENT_TIMESTAMPE = "pref_current_timestamp";

    public static final String PREF_NAME_RECEIVABLE_PUSH_MESSAGE_CHECKER = "xyz.parti.catan.RECEIVABLE_PUSH_MESSAGE_CHECKER";
    public static final String PREF_NAME_SESSION = "xyz.parti.catan.SESSION";
    public static final String PREF_NAME_VERSION_CHECKER = "xyz.parti.catan.VERSION_CHECKER";
    public static final String PREF_NAME_NOTIFICATIONS = "xyz.parti.catan.NOTIFICATIONS";
    public static final String PREF_NAME_LAST_POST_FEED = "xyz.parti.catan.LAST_POST_FEED";
    public static final String PREF_NAME_JOINED_PARTIES = "xyz.parti.catan.JOINED_PARTIES";

    public static final int NO_NOTIFICATION_ID = -1;
    public static final int MERGED_NOTIFICATION_ID = 0;
    public final static long POST_FEED_DASHBOARD = 0;
    public static final int LIMIT_LAST_COMMENTS_COUNT_IN_POST_ACTIVITY = 5;
    public static final int LIMIT_LAST_COMMENTS_COUNT_IN_POST_FEED = 3;
}
