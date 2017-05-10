package xyz.parti.catan.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import xyz.parti.catan.Constants;
import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class ReportHelper {
    public static void wtf(Context context, String msg) {
        Toast.makeText(context.getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
        Log.wtf(Constants.TAG, msg);
    }

    public static void wtf(Context context, Throwable t) {
        Toast.makeText(context.getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
        Log.wtf(Constants.TAG, t);
    }
}
