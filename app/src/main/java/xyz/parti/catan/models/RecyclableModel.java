package xyz.parti.catan.models;

/**
 * Created by dalikim on 2017. 5. 3..
 */

public interface RecyclableModel<T> {
    boolean isSame(T other);
}
