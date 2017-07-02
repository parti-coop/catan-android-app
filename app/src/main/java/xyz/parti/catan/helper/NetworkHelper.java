package xyz.parti.catan.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;


public class NetworkHelper {
    private Context context;

    public NetworkHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isValidNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (null != activeNetwork && activeNetwork.isConnected() && (activeNetwork.getType() == TYPE_WIFI || activeNetwork.getType() == TYPE_MOBILE));
    }
}
