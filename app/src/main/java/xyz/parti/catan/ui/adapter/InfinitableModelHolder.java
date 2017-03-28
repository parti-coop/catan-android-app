package xyz.parti.catan.ui.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class InfinitableModelHolder<T> {
    private final T model;
    private final boolean isLoader;

    private InfinitableModelHolder(T model, boolean isLoader) {
        this.model = model;
        this.isLoader = isLoader;
    }

    public static <T> InfinitableModelHolder<T> forModel(T model) {
        return new InfinitableModelHolder(model, false);
    }

    public static <T> InfinitableModelHolder<T> forLoader() {
        return new InfinitableModelHolder(null, true);
    }

    public boolean isLoader() {
        return this.isLoader;
    }

    public T getModel() {
        return this.model;
    }

    public static <T> List<InfinitableModelHolder<T>> from(Collection<T> c) {
        List<InfinitableModelHolder<T>> result = new ArrayList<>();
        for (T item : c) {
            result.add(InfinitableModelHolder.forModel(item));
        }

        return result;
    }
}