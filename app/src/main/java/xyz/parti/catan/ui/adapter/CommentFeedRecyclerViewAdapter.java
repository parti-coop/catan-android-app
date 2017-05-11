package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.ui.binder.CommentBinder;
import xyz.parti.catan.ui.presenter.CommentFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class CommentFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Comment> {
    private final LayoutInflater inflater;
    private CommentFeedPresenter presenter;

    public CommentFeedRecyclerViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent) {
        return new CommentFeedRecyclerViewAdapter.CommentViewHolder(inflater.inflate(R.layout.comment, parent, false), this);
    }

    @Override
    BaseViewHolder onCreateLoaderHolder(ViewGroup parent) {
        return new LoadHolder(inflater.inflate(R.layout.dashboard_load, parent, false));
    }

    @Override
    protected void prepareChangedModel(InfinitableModelHolder<Comment> holders) {
        if(presenter == null) return;
        for(String url : holders.getPreloadImageUrls()) {
            presenter.preloadImage(url);
        }
    }

    @Override
    boolean isLoadMorePosition(int position) {
        return position <= 0;
    }

    @Override
    void onBuildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((CommentFeedRecyclerViewAdapter.CommentViewHolder) viewHolder).bindData(getModel(position), position);
    }

    public void setPresenter(CommentFeedPresenter presenter) {
        this.presenter = presenter;
    }
    
    private boolean isLastPosition(int position) {
        return getLastPosition() == position;
    }

    private static class CommentViewHolder extends ModelViewHolder {
        private View view;
        private CommentFeedRecyclerViewAdapter adapter;

        CommentViewHolder(View view, CommentFeedRecyclerViewAdapter adapter) {
            super(view);
            this.view = view;
            this.adapter = adapter;
        }

        @Override
        boolean isLoader() {
            return false;
        }

        public void bindData(Comment comment, int position) {
            boolean isLineVisible = !adapter.isLastPosition(position);
            new CommentBinder(view).bindData(comment, isLineVisible);
        }
    }
}
