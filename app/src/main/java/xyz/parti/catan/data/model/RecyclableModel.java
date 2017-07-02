package xyz.parti.catan.data.model;

import java.util.List;


public interface RecyclableModel {
    boolean isSame(Object other);
    List<String> getPreloadImageUrls();
}
