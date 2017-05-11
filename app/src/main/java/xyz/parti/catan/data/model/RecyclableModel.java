package xyz.parti.catan.data.model;

import android.content.Context;

import java.util.List;

/**
 * Created by dalikim on 2017. 5. 3..
 */

public interface RecyclableModel {
    boolean isSame(Object other);
    List<String> getPreloadImageUrls();
}
