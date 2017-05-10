package xyz.parti.catan.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.NetworkHelper;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.helper.TextHelper;

/**
 * Created by dalikim on 2017. 5. 5..
 */

public class DisconnectActivity extends BaseActivity {
    public static final String ACTION_NETWORK_RECONNECT = "parti.xyz.catan.session.NetworkReconnect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnect);
        ButterKnife.bind(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(networkReconnectReceiver, new IntentFilter(DisconnectActivity.ACTION_NETWORK_RECONNECT));
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkReconnectReceiver);
        super.onDestroy();
    }

    @OnClick(R.id.button_check)
    public void check(Button checkButton) {
        if(new NetworkHelper(this).isValidNetwork()) {
            cleanUp();
        } else {
            Toast.makeText(this.getApplicationContext(), R.string.fail_to_connect, Toast.LENGTH_LONG).show();
        }
    }

    private void cleanUp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }

    @Override
    public boolean willFinishIfLogOut() {
        return false;
    }

    @Override
    public boolean willFinishIfNetworkDisconnect() {
        return false;
    }

    private BroadcastReceiver networkReconnectReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent){
            cleanUp();
        }
    };
}
