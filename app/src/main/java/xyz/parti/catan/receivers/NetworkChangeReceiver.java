package xyz.parti.catan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import xyz.parti.catan.Constants;
import xyz.parti.catan.helper.NetworkHelper;
import xyz.parti.catan.ui.activity.BaseActivity;
import xyz.parti.catan.ui.activity.DisconnectActivity;

/**
 * Created by dalikim on 2017. 5. 5..
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!NetworkHelper.isValidNetwork(context)) {
            Log.d(Constants.TAG_LOCAL, "INVALID");
            Intent intentCleanUp = new Intent(BaseActivity.ACTION_NETWORK_DISCONNECT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCleanUp);

            Intent intentShowDisconnect = new Intent(context, DisconnectActivity.class);
            intentShowDisconnect.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentShowDisconnect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentShowDisconnect);
        } else {
            Log.d(Constants.TAG_LOCAL, "VALID");
            Intent intentCleanUp = new Intent(DisconnectActivity.ACTION_NETWORK_RECONNECT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCleanUp);
        }
    }

}
