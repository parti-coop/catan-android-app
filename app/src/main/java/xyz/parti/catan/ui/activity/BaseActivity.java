package xyz.parti.catan.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.sessions.SessionManager;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class BaseActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        registerReceiver(receiver, new IntentFilter(SessionManager.LOGOUT_ACTION));
    }

    protected void onDestroy(){
        //this is very important, you have to unregister the
        //receiver before the activity is destroyed.
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            //In my case the LoginActivity is visible after logout, so i don't finish the Login Activity
            if(BaseActivity.this instanceof LoginMenuActivity){
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "Skip destroying after logout");
                }
                return;
            }else{
                finish();
            }
        }
    };
}
