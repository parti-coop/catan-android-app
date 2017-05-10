package xyz.parti.catan.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by dalikim on 2017. 4. 24..
 */

public class IntentHelper {
    private Context context;

    public IntentHelper(Context context) {
        this.context = context;
    }

    public void startPlayStore(String appPackage) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackage)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://play.google.com/store/apps/details?id=" + appPackage)));
        }
    }

}
