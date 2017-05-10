package xyz.parti.catan.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.RecyclableModel;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public abstract class LoadMoreRecyclerViewAdapter<T extends RecyclableModel<T>> extends RecyclerView.Adapter<LoadMoreRecyclerViewAdapter.BaseViewHolder> {
    private static int TYPE_MODEL = 0;
    private static int TYPE_LOAD = 1;

    private final List<InfinitableModelHolder<T>> holders;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;

    abstract LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent);
    abstract boolean isLoadMorePosition(int position);
    abstract void onBildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position);
    abstract LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateLoaderHolder(ViewGroup parent);

    LoadMoreRecyclerViewAdapter() {
        holders = new ArrayList<>();
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        this.isMoreDataAvailable = moreDataAvailable;
    }

    public void appendModels(List<T> items) {
        this.holders.addAll(InfinitableModelHolder.from(items));
        notifyDataSetChanged();
    }

    public void appendModel(T item) {
        this.holders.add(InfinitableModelHolder.forModel(item));
        notifyItemInserted(holders.size() - 1);
    }

    public void prependModels(List<T> items) {
        this.holders.addAll(0, InfinitableModelHolder.from(items));
        notifyDataPrepended(items.size() + 1);
    }

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
//        LayoutInflater inflater = LayoutInflater.from(context);
//            return new LoadHolder(inflater.inflate(R.layout.dashboard_load, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(LoadMoreRecyclerViewAdapter.BaseViewHolder viewHolder, int position) {
        if(getItemViewType(position) == TYPE_MODEL){
            onBildModelViewHolder(viewHolder, position);
        }

        if(isLoadMorePosition(position)){
            loadMore();
        }
    }

    @Override
    final public int getItemCount() {
        if(holders == null) {
            return 0;
        }
        return holders.size();
    }

    public void notifyDataPrepended(int count) {
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

    public T getModel(int position) {
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

    public InfinitableModelHolder<T> getHolder(int posistion) {
        return holders.get(posistion);
    }

    public void changeModel(T model) {
        changeModel(model, null);
    }

    public void changeModel(T model, Object payload) {
        for (int i = 0; i < holders.size(); i++) {
            if(model instanceof Post) {
                Post item = (Post) getModel(i);
            }

            if (model != null && model.isSame(getModel(i))) {
                holders.set(i, InfinitableModelHolder.forModel(model));
                notifyItemChanged(i, payload);
            }
        }
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
