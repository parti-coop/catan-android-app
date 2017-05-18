package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class PostFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Post> {
    private final LayoutInflater inflater;
    private PostFeedPresenter presenter;

    public PostFeedRecyclerViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public PostFeedRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent) {
        return new PostFeedRecyclerViewAdapter.PostViewHolder(inflater.inflate(R.layout.post, parent, false), presenter);
    }

    @Override
    public PostFeedRecyclerViewAdapter.BaseViewHolder onCreateLoaderHolder(ViewGroup parent) {
        return new LoadHolder(inflater.inflate(R.layout.dashboard_load, parent, false));
    }

    @Override
    public boolean isLoadMorePosition(int position) {
        return position >= getItemCount() - 1;
    }

    @Override
    public void onBuildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((PostFeedRecyclerViewAdapter.PostViewHolder)viewHolder).bindData(getModel(position));
    }

    @Override
    public void prepareChangedModel(InfinitableModelHolder<Post> holders) {
        if(presenter == null) return;
        for(String url : holders.getPreloadImageUrls()) {
            presenter.preloadImage(url);
        }
    }

    @Override
    public void onBindViewHolder(PostFeedRecyclerViewAdapter.BaseViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            if(holder.isLoader()) {
                return;
            }
            for (Object payload : payloads) {
                PostViewHolder postViewHolder = (PostViewHolder) holder;
                postViewHolder.getPostBinder().rebindData(getModel(position), payload);
            }
        }
    }

    public void setPresenter(PostFeedPresenter presenter) {
        this.presenter = presenter;
    }

    static class PostViewHolder extends ModelViewHolder {
        private final PostBinder postBinder;

        PostViewHolder(android.view.View view, PostFeedPresenter presenter) {
            super(view);
            this.postBinder = new PostBinder(view.getContext(), view, presenter);
        }

        void bindData(Post post){
            this.postBinder.bindData(post);
        }

        PostBinder getPostBinder() {
            return this.postBinder;
        }
    }

}
