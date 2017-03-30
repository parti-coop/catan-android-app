package xyz.parti.catan.ui.adapter;

import android.content.Context;

/**
 * Created by dalikim on 2017. 3. 30..
 */

public class NavigationItem {
    String title;

    public NavigationItem(Context context, int titleRestId) {
        this.title = context.getString(titleRestId);
    }
}
