package xyz.parti.catan.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.parti.catan.R;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.binder.CommentBinder;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class CommentFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Comment> {
    private Activity activity;
    private final List<InfinitableModelHolder<Comment>> comments;
    private final SessionManager session;

    public CommentFeedRecyclerViewAdapter(Activity activity, List<InfinitableModelHolder<Comment>> comments, SessionManager session) {
        super(activity, comments);
        this.activity = activity;
        this.comments = comments;
        this.session = session;
    }

    @Override
    RecyclerView.ViewHolder onCreateModelViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        return new CommentFeedRecyclerViewAdapter.CommentViewHolder(inflater.inflate(R.layout.comment, parent, false), this.session);
    }

    @Override
    boolean isLoadMorePosition(int position) {
        return position <= 0;
    }

    @Override
    void onBildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((CommentFeedRecyclerViewAdapter.CommentViewHolder) viewHolder).bindData(comments.get(position).getModel(), position);
    }

    private boolean isLastPosition(int position) {
        return (comments.size() - 1) == position;
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private final SessionManager session;

        public CommentViewHolder(View view, SessionManager session) {
            super(view);
            this.view = view;
            this.session = session;
        }

        public void bindData(Comment comment, int position) {
            boolean isLineVisible = !isLastPosition(position);
            new CommentBinder(view, session).bindData(comment, isLineVisible);
        }
    }
}
