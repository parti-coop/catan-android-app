package xyz.parti.catan.ui.view;

import android.content.Context;

/**
 * Created by dalikim on 2017. 3. 30..
 */

public class NavigationItem {
    private String title;

    public NavigationItem(Context context, int titleRestId) {
        this.title = context.getString(titleRestId);
    }
}
