package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public abstract class LoadMoreRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int TYPE_MODEL = 0;
    private static int TYPE_LOAD = 1;

    private Context context;
    private final List<InfinitableModelHolder<T>> models;
    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;

    abstract RecyclerView.ViewHolder onCreateModelViewHolder(ViewGroup parent);
    abstract boolean isLoadMorePosition(int position);
    abstract void onBildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position);

    LoadMoreRecyclerViewAdapter(Context context, List<InfinitableModelHolder<T>> model) {
        this.context = context;
        this.models = model;
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        this.isMoreDataAvailable = moreDataAvailable;
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

    private class LoadHolder extends RecyclerView.ViewHolder{
        LoadHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    final public int getItemViewType(int position) {
        if(models.get(position).isLoader()){
            return TYPE_LOAD;
        }else{
            return TYPE_MODEL;
        }
    }

    @Override
    final public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == TYPE_MODEL) {
            return onCreateModelViewHolder(parent);
        } else {
            return new LoadHolder(inflater.inflate(R.layout.dashboard_load, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(getItemViewType(position) == TYPE_MODEL){
            onBildModelViewHolder(viewHolder, position);
        }

        if(isLoadMorePosition(position)){
            loadMore();
        }
    }

    @Override
    final public int getItemCount() {
        return models.size();
    }

    public void notifyAllDataChangedAndLoadFinished(){
        notifyDataSetChanged();
        isLoading = false;
    }

    public void notifyDataPrependedAndLoadFinished(int count) {
        notifyItemRangeInserted(0, count - 1);
        isLoading = false;
    }

    public void notifyLoadFinished() {
        isLoading = false;
    }

    public void clearData() {
        this.models.clear();
    }
}
