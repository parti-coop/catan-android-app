package xyz.parti.catan.helper;

import android.content.Context;
import android.widget.Toast;

import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class ReportHelper {
    public static void wtf(Context context, String msg) {
        Toast.makeText(context.getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
        CatanLog.d(msg);
    }

    public static void wtf(Context context, Throwable t) {
        Toast.makeText(context.getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
        CatanLog.e(t);
    }
}
