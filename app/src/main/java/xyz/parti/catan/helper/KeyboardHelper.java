package xyz.parti.catan.helper;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


public class KeyboardHelper {
    public static void hideKey(Context context, final View view) {
        final InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        }, 50);
    }

    public static void showKey(Context context, final View view) {
        final InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 50);
    }
}
