package xyz.parti.catan.helper;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dalikim on 2017. 6. 24..
 */

public class ClosableClickableList {
    final List<View> views = new ArrayList<>();

    public void add(View view) {
        views.add(view);
    }

    public void clear() {
        for(View view : views) {
            if(view != null) view.setOnClickListener(null);
        }
    }

}
