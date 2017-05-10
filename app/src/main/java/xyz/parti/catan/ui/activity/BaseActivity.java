package xyz.parti.catan.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.stetho.Stetho;

import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.helper.IntentHelper;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class BaseActivity extends AppCompatActivity {
    public static final String ACTION_LOGOUT = "parti.xyz.catan.session.LogOut";
    public static final String ACTION_NETWORK_DISCONNECT = "parti.xyz.catan.session.NetworkDisconnect";
    public static final String ACTION_NEW_APP_VERSION_AVAILABLE = "parti.xyz.catan.session.NewAppVersionAvailable";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Stetho.initializeWithDefaults(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(logOutReceiver, new IntentFilter(ACTION_LOGOUT));
        LocalBroadcastManager.getInstance(this).registerReceiver(networkDisconnectReceiver, new IntentFilter(ACTION_NETWORK_DISCONNECT));
        LocalBroadcastManager.getInstance(this).registerReceiver(newAppVersionAvailable, new IntentFilter(ACTION_NEW_APP_VERSION_AVAILABLE));
    }

    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logOutReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDisconnectReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newAppVersionAvailable);
        super.onDestroy();
    }

    private BroadcastReceiver logOutReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            //In my case the LoginActivity is visible after logout, so i don't finish the Login Activity
            if(willFinishIfLogOut()){
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "Skip destroying after logout");
                }
            }else{
                Log.d(Constants.TAG, "Destroying after logout");
                finish();
            }
        }
    };

    private BroadcastReceiver networkDisconnectReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            //In my case the LoginActivity is visible after logout, so i don't finish the Login Activity
            if(willFinishIfNetworkDisconnect()){
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "Skip destroying after disconnect");
                }
            }else{
                Log.d(Constants.TAG, "Destroying after disconnect");
                finish();
            }
        }
    };

    private BroadcastReceiver newAppVersionAvailable = new BroadcastReceiver(){
        public void onReceive(final Context context, Intent intent){
            Snackbar.make(getWindow().getDecorView().getRootView(), "xx", Snackbar.LENGTH_LONG).setAction("확인", view -> new IntentHelper(BaseActivity.this).startPlayStore(context.getPackageName()));
        }
    };

    public boolean willFinishIfLogOut() {
        return true;
    }
    public boolean willFinishIfNetworkDisconnect() {
        return true;
    }

}
