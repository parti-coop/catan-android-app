package xyz.parti.catan.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.parti.catan.data.model.RecyclableModel;
import xyz.parti.catan.helper.CatanLog;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public abstract class LoadMoreRecyclerViewAdapter<T extends RecyclableModel> extends RecyclerView.Adapter<LoadMoreRecyclerViewAdapter.BaseViewHolder> {
    private final List<InfinitableModelHolder<T>> holders;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;

    abstract LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent);
    abstract boolean isLoadMorePosition(int position);
    abstract void onBuildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position);
    abstract LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateLoaderHolder(ViewGroup parent);
    abstract protected void prepareChangedModel(InfinitableModelHolder<T> holder);


    LoadMoreRecyclerViewAdapter() {
        holders = new ArrayList<>();
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        this.isMoreDataAvailable = moreDataAvailable;
    }

    public void clearAndAppendModels(List<T> items) {
        clearAndAppendModels(items, 0);
    }

    public void clearAndAppendModels(List<T> items, int offset) {
        if(this.holders.size() > 0) {
            this.holders.subList(offset, this.holders.size()).clear();
        }
        List<InfinitableModelHolder<T>> holders =
                InfinitableModelHolder.from(items);
        this.holders.addAll(holders);
        prepareChangedModels(holders);
        notifyDataSetChanged();
    }

    public void appendModels(List<T> items) {
        List<InfinitableModelHolder<T>> holders =
                InfinitableModelHolder.from(items);
        this.holders.addAll(holders);
        prepareChangedModels(holders);
        notifyDataSetChanged();
    }

    public void appendModel(T item) {
        InfinitableModelHolder<T> holder = InfinitableModelHolder.forModel(item);
        this.holders.add(holder);
        prepareChangedModel(holder);
        notifyItemInserted(this.holders.size() - 1);
    }

    public void prependModels(List<T> items) {
        List<InfinitableModelHolder<T>> holders = InfinitableModelHolder.from(items);
        this.holders.addAll(0, holders);
        prepareChangedModels(holders);
        notifyDataPrepended(items.size());
    }

    public void prependModel(T item) {
        InfinitableModelHolder<T> holder = InfinitableModelHolder.forModel(item);
        this.holders.add(0, holder);
        prepareChangedModel(holder);
        notifyDataPrepended(1);
    }

    public void addModel(int position, T item) {
        InfinitableModelHolder<T> holder = InfinitableModelHolder.forModel(item);
        this.holders.add(position, holder);
        prepareChangedModel(holder);
        notifyItemInserted(position);
    }

    public void changeModel(T model, Object payload) {
        if(model == null) return;

        for (int i = 0; i < holders.size(); i++) {
            if (model.isSame(getModel(i))) {
                holders.set(i, InfinitableModelHolder.forModel(model));
                notifyItemChanged(i, payload);
                return;
            }
        }
        CatanLog.d("Not found model in LoadMoreRecyclerView : " + model.toString());
    }

    private void prepareChangedModels(List<InfinitableModelHolder<T>> holders) {
        if(holders == null) return;
        for(InfinitableModelHolder<T> holder : holders) {
            this.prepareChangedModel(holder);
        }
    }

    public void appendLoader() {
        holders.add(InfinitableModelHolder.<T>forLoader());
        notifyItemInserted(holders.size() - 1);
    }

    public void prependLoader() {
        holders.add(0, InfinitableModelHolder.<T>forLoader());
        notifyItemInserted(0);
    }

    public void addLoader(int position) {
        holders.add(position, InfinitableModelHolder.<T>forLoader());
        notifyItemInserted(position);
    }

    public void prependCustomView(int viewType) {
        holders.add(InfinitableModelHolder.<T>forCustomeView(viewType));
        notifyItemInserted(0);
    }

    public void clearAndAppendCustomView(int viewType) {
        if(this.holders.size() > 0) {
            this.holders.clear();
        }
        holders.add(InfinitableModelHolder.<T>forCustomeView(viewType));
        notifyDataSetChanged();
    }

    public void clear() {
        if(this.holders.size() > 0) {
            this.holders.clear();
        }
        notifyDataSetChanged();
    }

    public void removeMoldelHolderAt(int position) {
        holders.remove(position);
        notifyItemRemoved(position);
    }

    public void removeFirstMoldelHolder() {
        holders.remove(0);
        notifyItemRemoved(0);
    }

    public void removeLastMoldelHolder() {
        holders.remove(holders.size() - 1);
        notifyItemRemoved(holders.size() - 1);
    }

    public boolean isEmpty() {
        return holders.isEmpty();
    }

    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    private boolean isLoadable() {
        return isMoreDataAvailable && !isLoading && loadMoreListener != null;
    }

    private void loadMore() {
        if(!isLoadable()) {
            return;
        }
        isLoading = true;
        loadMoreListener.onLoadMore();
    }

    @Override
    public int getItemViewType(int position) {
        if(holders.get(position).isLoader()){
            return InfinitableModelHolder.TYPE_LOAD;
        }else{
            return InfinitableModelHolder.TYPE_MODEL;
        }
    }

    @Override
    final public LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == InfinitableModelHolder.TYPE_MODEL) {
            return onCreateModelViewHolder(parent);
        } else if(viewType == InfinitableModelHolder.TYPE_LOAD) {
            return onCreateLoaderHolder(parent);
        } else {
            return onCreateCustomViewHolder(parent, viewType);
        }
    }

    public LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        throw new UnsupportedOperationException("Not supported viewType : " + viewType);
    }

    @Override
    public void onBindViewHolder(LoadMoreRecyclerViewAdapter.BaseViewHolder viewHolder, int position) {
        if(getItemViewType(position) == InfinitableModelHolder.TYPE_MODEL){
            onBuildModelViewHolder(viewHolder, position);
        } else {
            onBuildCustomViewHolder(viewHolder, position);
        }

        if(isLoadMorePosition(position)){
            loadMore();
        }
    }

    protected void onBuildCustomViewHolder(BaseViewHolder viewHolder, int position) {
    }

    @Override
    public int getItemCount() {
        return holders.size();
    }

    public int getModelItemCount() {
        if(holders == null) return 0;

        int result = 0;
        for(InfinitableModelHolder<T> holder : holders) {
            if(holder.getViewType() == InfinitableModelHolder.TYPE_MODEL) result++;
        }

        return result;
    }

    private void notifyDataPrepended(int count) {
        notifyItemRangeInserted(0, count);
    }

    public void setLoadFinished() {
        isLoading = false;
    }

    public void setLoadStarted() {
        isLoading = true;
    }

    T getModel(int position) {
        InfinitableModelHolder<T> result = getHolder(position);
        if(result == null) {
            return null;
        } else {
            return result.getModel();
        }
    }

    public T getFirstModel() {
        if(getModelItemCount() <= 0) return null;
        int i = 0;
        T result = null;
        do {
            if(getItemCount() <= i) break;
            result = getModel(i);
            i++;
        } while(result == null);

        return result;
    }
    public T getLastModel() {
        if(getModelItemCount() <= 0) return null;
        int i = holders.size() - 1;
        T result = null;
        do {
            if(0 > i) break;
            result = getModel(i);
            i--;
        } while(result == null);

        return result;
    }

    public InfinitableModelHolder<T> getHolder(int position) {
        return holders.get(position);
    }

    public int getLastPosition() {
        return holders.size() - 1;
    }

    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        BaseViewHolder(View itemView) {
            super(itemView);
        }
        abstract boolean isLoader();
    }

    static abstract class ModelViewHolder extends BaseViewHolder {
        ModelViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        boolean isLoader() {
            return false;
        }
    }

    static class LoadHolder extends BaseViewHolder {
        LoadHolder(View itemView) {
            super(itemView);
        }

        @Override
        boolean isLoader() {
            return true;
        }
    }
}
