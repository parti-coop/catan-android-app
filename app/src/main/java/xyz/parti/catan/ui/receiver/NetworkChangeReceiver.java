package xyz.parti.catan.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import xyz.parti.catan.helper.NetworkHelper;
import xyz.parti.catan.ui.activity.BaseActivity;
import xyz.parti.catan.ui.activity.DisconnectActivity;


public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(new NetworkHelper(context).isValidNetwork() == false) {
            Intent intentCleanUp = new Intent(BaseActivity.ACTION_NETWORK_DISCONNECT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCleanUp);
        } else {
            Intent intentCleanUp = new Intent(DisconnectActivity.ACTION_NETWORK_RECONNECT);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentCleanUp);
        }
    }


}
