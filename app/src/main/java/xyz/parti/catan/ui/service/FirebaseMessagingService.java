package xyz.parti.catan.ui.service;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;
import java.util.Map;

import xyz.parti.catan.CatanApplication;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.PushMessage;
import xyz.parti.catan.helper.PrefPushMessage;
import xyz.parti.catan.ui.activity.MainActivity;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PRIVATE;

/**
 * Created by dalikim on 2017. 5. 13..
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        PushMessage pushMessage = new PushMessage();
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
        Log.d(Constants.TAG_TEST, "Received title : " + data.get("title"));
        Log.d(Constants.TAG_TEST, "Received body : " + data.get("body"));
        Log.d(Constants.TAG_TEST, "Received priority : " + data.get("priority"));
        Log.d(Constants.TAG_TEST, "Received type : " + data.get("type"));
        Log.d(Constants.TAG_TEST, "Received param : " + data.get("param"));
        Log.d(Constants.TAG_TEST, "Received url : " + data.get("url"));

        SessionManager session = new SessionManager(this);
        if(!session.isLoggedIn() || session.getCurrentUser().id != pushMessage.user_id) {
            Log.d(Constants.TAG, "Message : User NOT match");
            return;
        }

        if(!PrefPushMessage.isReceivable(this)) {
            Log.d(Constants.TAG, "Message : Disable by user");
            return;
        }

        addNotification(pushMessage);
    }

    private void addNotification(PushMessage pushMessage) {
        // 빠띠 앱이 실행 중인 경우
        boolean isForeground = getApplication() instanceof CatanApplication && ((CatanApplication) getApplication()).isForground();
        if(isForeground) {
            Log.d(Constants.TAG_TEST, "isForeground");
            if(! "post".equals(pushMessage.type)) {
                notify(pushMessage);
            } else {
                if ("high".equals(pushMessage.priority)) {
                    notify(pushMessage);
                }
            }
        }
        // 홈 런쳐가 실행 중인 경우
        else if(isUserIsOnHomeScreen()) {
            Log.d(Constants.TAG_TEST, "isUserIsOnHomeScreen");
            notify(pushMessage);
        }
        // 다른 앱이 실행 중인 경우
        else {
            Log.d(Constants.TAG_TEST, "elseelseelseelse");
            notify(pushMessage);
        }
    }

    private void notify(PushMessage pushMessage) {
        Intent intent = buildIntentForNotification(pushMessage);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, MainActivity.REQUEST_PUSH_MESSAGE /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(pushMessage.title)
                .setContentText(pushMessage.body)
                .setTicker(pushMessage.body)
                .setAutoCancel(true)
                .setVisibility(VISIBILITY_PRIVATE)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setLights(173, 500, 2000)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(makeNotificationID(), notificationBuilder.build());
    }

    @NonNull
    private Intent buildIntentForNotification(PushMessage pushMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("pushMessage", Parcels.wrap(pushMessage));
        return intent;
    }

    public boolean isUserIsOnHomeScreen() {
        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processes) {
            return process.pkgList[0].equalsIgnoreCase("com.android.launcher");
        }
        return false;
    }

    private int makeNotificationID() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        return Integer.valueOf(last4Str);
    }
}
