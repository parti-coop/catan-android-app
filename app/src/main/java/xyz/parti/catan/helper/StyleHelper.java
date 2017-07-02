package xyz.parti.catan.helper;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import xyz.parti.catan.R;


public class StyleHelper {
    public static void forSnackbar(Context context, Snackbar snackbar) {
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.style_color_primary_light));
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.brand_gray_light));
    }
}
