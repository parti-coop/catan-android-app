package xyz.parti.catan.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by dalikim on 2017. 5. 5..
 */

public class NetworkHelper {
    public static boolean isValidNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (null != activeNetwork && (activeNetwork.getType() == TYPE_WIFI || activeNetwork.getType() == TYPE_MOBILE));
    }
}
