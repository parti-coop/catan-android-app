package xyz.parti.catan.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.parti.catan.data.model.RecyclableModel;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public abstract class LoadMoreRecyclerViewAdapter<T extends RecyclableModel> extends RecyclerView.Adapter<LoadMoreRecyclerViewAdapter.BaseViewHolder> {
    private static int TYPE_MODEL = 0;
    private static int TYPE_LOAD = 1;

    private final List<InfinitableModelHolder<T>> holders;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;

    abstract LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent);
    abstract boolean isLoadMorePosition(int position);
    abstract void onBuildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position);
    abstract LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateLoaderHolder(ViewGroup parent);

    LoadMoreRecyclerViewAdapter() {
        holders = new ArrayList<>();
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        this.isMoreDataAvailable = moreDataAvailable;
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
        notifyDataPrepended(items.size() + 1);
    }

    public void changeModel(T model, Object payload) {
        for (int i = 0; i < holders.size(); i++) {
            if (model != null && model.isSame(getModel(i))) {
                holders.set(i, InfinitableModelHolder.forModel(model));
                notifyItemChanged(i, payload);
            }
        }
    }

    private void prepareChangedModels(List<InfinitableModelHolder<T>> holders) {
        if(holders == null) return;
        for(InfinitableModelHolder<T> holder : holders) {
            this.prepareChangedModel(holder);
        }
    }

    abstract protected void prepareChangedModel(InfinitableModelHolder<T> holder);

    public void appendLoader() {
        holders.add(InfinitableModelHolder.forLoader());
        notifyItemInserted(holders.size() - 1);
    }

    public void prependLoader() {
        holders.add(0, InfinitableModelHolder.forLoader());
        notifyItemInserted(0);
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
    final public int getItemViewType(int position) {
        if(holders.get(position).isLoader()){
            return TYPE_LOAD;
        }else{
            return TYPE_MODEL;
        }
    }

    @Override
    final public LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_MODEL) {
            return onCreateModelViewHolder(parent);
        } else {
            return onCreateLoaderHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(LoadMoreRecyclerViewAdapter.BaseViewHolder viewHolder, int position) {
        if(getItemViewType(position) == TYPE_MODEL){
            onBuildModelViewHolder(viewHolder, position);
        }

        if(isLoadMorePosition(position)){
            loadMore();
        }
    }

    @Override
    final public int getItemCount() {
        return holders.size();
    }

    private void notifyDataPrepended(int count) {
        notifyItemRangeInserted(0, count - 1);
    }

    public void setLoadFinished() {
        isLoading = false;
    }

    public void setLoadStarted() {
        isLoading = true;
    }

    public void clearData() {
        this.holders.clear();
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
        if(getItemCount() <= 0) return null;
        return getModel(0);
    }
    public T getLastModel() {
        return getModel(holders.size() - 1);
    }

    public InfinitableModelHolder<T> getFirstHolder() {
        return getHolder(0);
    }

    private InfinitableModelHolder<T> getHolder(int posistion) {
        return holders.get(posistion);
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
