package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.presenter.CommentFeedPresenter;
import xyz.parti.catan.ui.view.CommentView;


public class CommentFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Comment> {
    private final LayoutInflater inflater;
    private final Post post;
    private CommentFeedPresenter presenter;

    public CommentFeedRecyclerViewAdapter(Context context, Post post) {
        inflater = LayoutInflater.from(context);
        this.post = post;
    }

    @Override
    LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent) {
        return new CommentFeedRecyclerViewAdapter.CommentViewHolder(inflater.inflate(R.layout.comment, parent, false), this, presenter);
    }

    @Override
    BaseViewHolder onCreateLoaderHolder(ViewGroup parent) {
        return new LoadHolder(inflater.inflate(R.layout.dashboard_load, parent, false));
    }

    @Override
    boolean isLoadMorePosition(int position) {
        return position <= 0;
    }

    @Override
    void onBuildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Comment model = getModel(position);
        if(model == null) return;
        ((CommentFeedRecyclerViewAdapter.CommentViewHolder) viewHolder).bindData(post, model, position);
    }

    @Override
    public void onBindViewHolder(PostFeedRecyclerViewAdapter.BaseViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            for (Object payload : payloads) {
                if (holder instanceof CommentViewHolder) {
                    Comment model = getModel(position);
                    if(model == null) continue;
                    ((CommentViewHolder)holder).getCommentView().rebindData(post, model, payload);
                }
            }
        }
    }

    public void setPresenter(CommentFeedPresenter presenter) {
        this.presenter = presenter;
    }
    
    private boolean isLastPosition(int position) {
        return getLastPosition() == position;
    }

    private static class CommentViewHolder extends ModelViewHolder {
        private CommentView commentView;
        private CommentFeedRecyclerViewAdapter adapter;

        CommentViewHolder(View view, CommentFeedRecyclerViewAdapter adapter, CommentView.Presenter presenter) {
            super(view);
            this.adapter = adapter;
            commentView = (CommentView)view;
            commentView.attachPresenter(presenter);
        }

        @Override
        boolean isLoader() {
            return false;
        }

        @Override
        public void onViewRecycled() {
            commentView.unbind();
            super.onViewRecycled();
        }

        public void bindData(Post post, Comment comment, int position) {
            boolean isLineVisible = !adapter.isLastPosition(position);
            commentView.bindData(post, comment, isLineVisible);
        }

        CommentView getCommentView() {
            return commentView;
        }
    }
}
