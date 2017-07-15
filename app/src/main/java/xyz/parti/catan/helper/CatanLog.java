package xyz.parti.catan.helper;

import android.util.Log;

import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;


public class CatanLog {
    public static void d(String message) {
        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, message);
        }
    }

    public static void e(String message, Throwable e) {
        Log.e(Constants.TAG, message, e);
    }

    public static void e(String message) {
        Log.e(Constants.TAG, message);
    }

    public static void e(Throwable e) {
        Log.e(Constants.TAG, e.getMessage(), e);
    }

    public static void d(String message, Object... args) {
        if(BuildConfig.DEBUG) {
            d(String.format(message, args));
        }
    }
}
