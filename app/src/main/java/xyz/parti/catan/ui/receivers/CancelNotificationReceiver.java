package xyz.parti.catan.ui.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import xyz.parti.catan.Constants;
import xyz.parti.catan.data.repository.NotificationsRepository;

/**
 * Created by dalikim on 2017. 5. 31..
 */

public class CancelNotificationReceiver extends BroadcastReceiver {
    public static final String EXTRA_KEY_NOTIFICATION_ID = "notificationId";
    private NotificationsRepository notificationsRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_KEY_NOTIFICATION_ID, Constants.MERGED_NOTIFICATION_ID);
        getPushMessagesRepository(context).destroy(notificationId);
    }

    private NotificationsRepository getPushMessagesRepository(Context context) {
        if(notificationsRepository == null) {
            notificationsRepository = new NotificationsRepository(context.getSharedPreferences(Constants.PREF_NAME_NOTIFICATIONS, Context.MODE_PRIVATE));
        }
        return notificationsRepository;
    }
}