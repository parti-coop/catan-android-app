package xyz.parti.catan.ui.adapter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.RecyclableModel;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class InfinitableModelHolder<T extends RecyclableModel> {
    private final T model;
    private final boolean isLoader;

    private InfinitableModelHolder(T model, boolean isLoader) {
        this.model = model;
        this.isLoader = isLoader;
    }

    public static <T extends RecyclableModel> InfinitableModelHolder<T> forModel(T model) {
        return new InfinitableModelHolder<>(model, false);
    }

    public static <T extends RecyclableModel> InfinitableModelHolder<T> forLoader() {
        return new InfinitableModelHolder<>(null, true);
    }

    public boolean isLoader() {
        return this.isLoader;
    }

    public T getModel() {
        return this.model;
    }

    public static <T extends RecyclableModel> List<InfinitableModelHolder<T>> from(Collection<T> c) {
        List<InfinitableModelHolder<T>> result = new ArrayList<>();
        for (T item : c) {
            result.add(InfinitableModelHolder.forModel(item));
        }

        return result;
    }

    public static <T extends RecyclableModel> List<T> asModels(Collection<InfinitableModelHolder<T>> c) {
        List<T> result = new ArrayList<>();
        for (InfinitableModelHolder<T> item : c) {
            result.add(item.getModel());
        }

        return result;
    }

    public List<String> getPreloadImageUrls() {
        if(isLoader()) {
            return new ArrayList<>();
        }
        List<String> result = getModel().getPreloadImageUrls();
       if (result == null) {
           return new ArrayList<>();
       }
        return result;
    }
}