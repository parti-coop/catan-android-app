package xyz.parti.catan.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class AppVersionHelper {
    private Context context;

    public AppVersionHelper(Context context) {
        this.context = context;
    }

    public String getCurrentVerion() {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo =  packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return packageInfo.versionName;
    }
}
