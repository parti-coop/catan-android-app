package xyz.parti.catan.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.NetworkHelper;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class BaseActivity extends AppCompatActivity {
    boolean isNetworkReceiverRegistered = false;
    public static final String ACTION_LOGOUT = "parti.xyz.catan.session.LogOut";
    public static final String ACTION_NETWORK_DISCONNECT = "parti.xyz.catan.session.NetworkDisconnect";

    protected void onCreate(Bundle savedInstanceState) {
        if (!Fabric.isInitialized()) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET);
            Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
        }

        super.onCreate(savedInstanceState);
        ensureValidNetwork();
        LocalBroadcastManager.getInstance(this).registerReceiver(logOutReceiver, new IntentFilter(ACTION_LOGOUT));
    }

    private void ensureValidNetwork() {
        boolean isValidNetwork = new NetworkHelper(this).isValidNetwork();
        if(isValidNetwork) {
            if(willFinishIfNetworkConnect()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }
        } else {
            if(willFinishIfNetworkDisconnect()) {
                Intent intent = new Intent(this, DisconnectActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }
        }
    }

    private boolean willFinishIfNetworkConnect() {
        return !willFinishIfNetworkDisconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureValidNetwork();
        if (!isNetworkReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(networkDisconnectReceiver, new IntentFilter(ACTION_NETWORK_DISCONNECT));
            isNetworkReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (isNetworkReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDisconnectReceiver);
            isNetworkReceiverRegistered = false;
        }
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logOutReceiver);
        networkDisconnectReceiver = null;
        logOutReceiver = null;
        super.onDestroy();
    }

    private BroadcastReceiver logOutReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            //In my case the LoginActivity is visible after logout, so i don't finish the Login Activity
            if(willFinishIfLogOut()){
                CatanLog.d("Skip destroying after logout");
            }else{
                CatanLog.d("Destroying after logout");
                finish();
            }
        }
    };

    private BroadcastReceiver networkDisconnectReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            //In my case the LoginActivity is visible after logout, so i don't finish the Login Activity
            if (willFinishIfNetworkDisconnect()) {
                CatanLog.d("Skip destroying after disconnect");
            } else {
                CatanLog.d("Destroying after disconnect");
                finish();
            }

            Intent intentShowDisconnect = new Intent(context, DisconnectActivity.class);
            intentShowDisconnect.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intentShowDisconnect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentShowDisconnect);
        }
    };

    public boolean willFinishIfLogOut() {
        return true;
    }
    public boolean willFinishIfNetworkDisconnect() {
        return true;
    }

    public void reportError(String message) {
        if(new NetworkHelper(this).isValidNetwork()) {
            Toast.makeText(this.getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
            CatanLog.d(message);
        }
    }

    public void reportError(Throwable error) {
        if(new NetworkHelper(this).isValidNetwork()) {
            Toast.makeText(this.getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
            CatanLog.e(error);
        }
    }

    public void reportInfo(String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void reportInfo(@StringRes int idRes) {
        Toast.makeText(this.getApplicationContext(), getResources().getText(idRes), Toast.LENGTH_LONG).show();
    }
}
