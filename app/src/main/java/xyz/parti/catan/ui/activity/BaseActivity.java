package xyz.parti.catan.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.stetho.Stetho;

import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class BaseActivity extends AppCompatActivity {
    public static final String ACTION_LOGOUT = "parti.xyz.catan.session.LogOut";
    public static final String ACTION_NETWORK_DISCONNECT = "parti.xyz.catan.session.NetworkDisconnect";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(logOutReceiver, new IntentFilter(ACTION_LOGOUT));
        LocalBroadcastManager.getInstance(this).registerReceiver(networkDisconnectReceiver, new IntentFilter(ACTION_NETWORK_DISCONNECT));
    }

    protected void onDestroy(){
        //this is very important, you have to unregister the
        //logOutReceiver before the activity is destroyed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logOutReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDisconnectReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver logOutReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            //In my case the LoginActivity is visible after logout, so i don't finish the Login Activity
            if(willFinishIfLogOut()){
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "Skip destroying after logout");
                }
                return;
            }else{
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
                return;
            }else{
                finish();
            }
        }
    };

    public boolean willFinishIfLogOut() {
        return true;
    }
    public boolean willFinishIfNetworkDisconnect() {
        return true;
    }

}
