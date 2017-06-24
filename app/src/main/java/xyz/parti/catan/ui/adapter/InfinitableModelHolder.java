package xyz.parti.catan.ui.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import xyz.parti.catan.data.model.RecyclableModel;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class InfinitableModelHolder<T extends RecyclableModel> {
    static int TYPE_MODEL = 0;
    static int TYPE_LOAD = 1;

    private T model;
    private int viewType;

    private InfinitableModelHolder(T model, int viewType) {
        this.model = model;
        this.viewType = viewType;
    }

    static <T extends RecyclableModel> InfinitableModelHolder<T> forModel(T model) {
        return new InfinitableModelHolder<>(model, TYPE_MODEL);
    }

    static <T extends RecyclableModel> InfinitableModelHolder<T> forLoader() {
        return new InfinitableModelHolder<>(null, TYPE_LOAD);
    }

    static <T extends RecyclableModel> InfinitableModelHolder<T> forCustomeView(int viewType) {
        return new InfinitableModelHolder<>(null, viewType);
    }

    public boolean isLoader() {
        return this.viewType == TYPE_LOAD;
    }

    public T getModel() {
        return this.model;
    }

    public int getViewType() {
        return this.viewType;
    }

    public static <T extends RecyclableModel> List<InfinitableModelHolder<T>> from(Collection<T> c) {
        List<InfinitableModelHolder<T>> result = new ArrayList<>();
        for (T item : c) {
            result.add(InfinitableModelHolder.forModel(item));
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