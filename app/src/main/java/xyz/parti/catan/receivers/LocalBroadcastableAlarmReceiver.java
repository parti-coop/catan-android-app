package xyz.parti.catan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by dalikim on 2017. 3. 29..
 */

public class LocalBroadcastableAlarmReceiver extends BroadcastReceiver {
    public static final String INTENT_EXTRA_ACTION = "action";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(intent.getStringExtra(INTENT_EXTRA_ACTION));
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

}
