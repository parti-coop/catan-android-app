package xyz.parti.catan.ui.receivers;

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
        Log.d(Constants.TAG_TEST, "NetworkChangeReceiver");
        if(new NetworkHelper(context).isValidNetwork() == false) {
            Log.d(Constants.TAG_TEST, "not isValidNetwork");
            Intent intentCleanUp = new Intent(BaseActivity.ACTION_NETWORK_DISCONNECT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCleanUp);
        } else {
            Intent intentCleanUp = new Intent(DisconnectActivity.ACTION_NETWORK_RECONNECT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCleanUp);
        }
    }


}
