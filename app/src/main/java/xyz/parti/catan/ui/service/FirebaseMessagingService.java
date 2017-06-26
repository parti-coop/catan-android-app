package xyz.parti.catan.ui.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.parceler.Parcels;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.dao.MessagesStatusDAO;
import xyz.parti.catan.data.model.PushMessage;
import xyz.parti.catan.data.preference.NotificationsPreference;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.PrefPushMessage;
import xyz.parti.catan.ui.activity.MainActivity;
import xyz.parti.catan.ui.receiver.CancelNotificationReceiver;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PRIVATE;

/**
 * Created by dalikim on 2017. 5. 13..
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final int MAX_SIZE = 6;
    private NotificationManagerCompat notificationManager;
    private NotificationsPreference notificationsRepository;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        PushMessage pushMessage = new PushMessage();
        String idData = data.get("id");
        if(idData != null) {
            pushMessage.id = Long.parseLong(idData);
        }
        pushMessage.title = data.get("title");
        pushMessage.body = data.get("body");
        pushMessage.type = data.get("type");
        pushMessage.param = data.get("param");
        pushMessage.priority = data.get("priority");
        pushMessage.url = data.get("url");
        String userIdData = data.get("user_id");
        if(userIdData != null) {
            pushMessage.user_id = Long.parseLong(userIdData);
        }
        pushMessage.timestamp = System.currentTimeMillis();

        CatanLog.d("Received id : " + data.get("id"));
        CatanLog.d("Received title : " + data.get("title"));
        CatanLog.d("Received body : " + data.get("body"));
        CatanLog.d("Received priority : " + data.get("priority"));
        CatanLog.d("Received type : " + data.get("type"));
        CatanLog.d("Received param : " + data.get("param"));
        CatanLog.d("Received url : " + data.get("url"));

        SessionManager session = new SessionManager(this);
        if(!session.isLoggedIn() || session.getCurrentUser().id != pushMessage.user_id) {
            CatanLog.d("Message : User NOT match");
            return;
        }

        if(!PrefPushMessage.isReceivable(this)) {
            CatanLog.d("Message : Disable by user");
            return;
        }

        updateNotifications(pushMessage);
        updateMessagesStatus(pushMessage);
    }

    private void updateMessagesStatus(PushMessage pushMessage) {
        try {
            Realm realm = Realm.getDefaultInstance();
            try {
                MessagesStatusDAO messagesStatusDAO = new MessagesStatusDAO(realm);
                messagesStatusDAO.saveServerCreatedMessageIdSyncIfNew(pushMessage.user_id, pushMessage.id);
            } finally {
                if(realm != null) realm.close();
            }
        } catch (Throwable e) {
            CatanLog.e(e);
            /* ignored */
        }
    }

    private void updateNotifications(PushMessage newPushMessage) {
        long maxTimestamp = 0;
        for(PushMessage message : getNotificationsRepository().fetchAllPushMessages()) {
            if(message.isSound) maxTimestamp = Math.max(maxTimestamp, message.timestamp);
        }
        newPushMessage.isSound = (newPushMessage.timestamp - maxTimestamp > 30 * 1000);

        HashMap<Integer, List<PushMessage>> currentNotifications = getNotificationsRepository().fetchAllSingles();
        if(currentNotifications.size() + 1 >= MAX_SIZE) {
            makeMergedNotification(newPushMessage);
            getNotificationsRepository().mergeAll(newPushMessage);

            for(Integer id : getNotificationsRepository().fetchAllSingles().keySet()) {
                getNotificationManager().cancel(id);
            }
            getNotificationsRepository().destroyAllSingles();
        } else {
            int id = makeNotification(newPushMessage);
            getNotificationsRepository().addSingle(id, newPushMessage);
        }
    }

    private int makeNotification(PushMessage pushMessage) {
        int id = makeNotificationID(pushMessage);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_push_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(pushMessage.title)
                .setContentText(pushMessage.body)
                .setTicker(pushMessage.body)
                .setAutoCancel(true)
                .setVisibility(VISIBILITY_PRIVATE)
                .setLights(173, 500, 2000)
                .setContentIntent(buildIntentForNotification(id, pushMessage))
                .setDeleteIntent(buildIntentForCancel(pushMessage));

        if(pushMessage.isSound) {
            notificationBuilder.setSound(defaultSoundUri);
        }

        getNotificationManager().notify(id, notificationBuilder.build());
        return id;
    }

    private void makeMergedNotification(PushMessage newPushMessage) {
        List<PushMessage> pushMessages = getNotificationsRepository().fetchAllPushMessages();
        pushMessages.add(newPushMessage);
        Collections.sort(pushMessages, new Comparator<PushMessage>() {
            @Override
            public int compare(PushMessage pushMessageOne, PushMessage pushMessageTwo) {
                return (int)(pushMessageTwo.id - pushMessageOne.id);
            }
        });

        String title = String.format(Locale.getDefault(), getResources().getString(R.string.merged_notification_title), pushMessages.size());
        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();

        int index = 0;
        for(PushMessage pushMessage : pushMessages) {
            inbox.addLine(pushMessage.title + " " + pushMessage.body);
            if(++index >= 10) {
                break;
            }
        }
        inbox.setBigContentTitle(title);
        inbox.setSummaryText("민주적 일상 커뮤니티 빠띠");

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_push_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setSubText("민주적 일상 커뮤니티 빠띠")
                .setNumber(pushMessages.size())
                .setTicker(title)
                .setAutoCancel(true)
                .setVisibility(VISIBILITY_PRIVATE)
                .setLights(173, 500, 2000)
                .setStyle(inbox)
                .setContentIntent(buildIntentForMergedNotification(Constants.MERGED_NOTIFICATION_ID))
                .setDeleteIntent(buildIntentForCancelAll());

        if(newPushMessage.isSound) {
            notificationBuilder.setSound(defaultSoundUri);
        }

        getNotificationManager().notify(Constants.MERGED_NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent buildIntentForMergedNotification(int notificationId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notificationId", notificationId);

        return PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private PendingIntent buildIntentForNotification(int notificationId, PushMessage pushMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("pushMessage", Parcels.wrap(pushMessage));
        intent.putExtra("notificationId", notificationId);

        return PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent buildIntentForCancel(PushMessage pushMessage) {
        int notificationID = makeNotificationID(pushMessage);

        Intent onCancelNotificationReceiver = new Intent(this, CancelNotificationReceiver.class);
        onCancelNotificationReceiver.putExtra(CancelNotificationReceiver.EXTRA_KEY_NOTIFICATION_ID, notificationID);
        return PendingIntent.getBroadcast(this.getApplicationContext(), notificationID,
                onCancelNotificationReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent buildIntentForCancelAll() {
        Intent onCancelNotificationReceiver = new Intent(this, CancelNotificationReceiver.class);
        onCancelNotificationReceiver.putExtra(CancelNotificationReceiver.EXTRA_KEY_NOTIFICATION_ID, Constants.MERGED_NOTIFICATION_ID);
        return PendingIntent.getBroadcast(this.getApplicationContext(), Constants.MERGED_NOTIFICATION_ID,
                onCancelNotificationReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private NotificationManagerCompat getNotificationManager() {
        if(notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(getApplicationContext());
        }
        return notificationManager;
    }

    private int makeNotificationID(PushMessage pushMessage) {
        return (int)(pushMessage.id % (Integer.MAX_VALUE - 1));
    }

    private NotificationsPreference getNotificationsRepository() {
        if(notificationsRepository == null) {
            notificationsRepository = new NotificationsPreference(getApplicationContext());
        }
        return notificationsRepository;
    }
}
