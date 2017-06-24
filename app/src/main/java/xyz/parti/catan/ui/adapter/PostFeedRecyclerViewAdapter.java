package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class PostFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Post> {
    private static final int FORM_TYPE = 1000;
    private final LayoutInflater inflater;
    private final User currentUser;
    private PostFeedPresenter presenter;

    public PostFeedRecyclerViewAdapter(Context context, User currentUser) {
        this.inflater = LayoutInflater.from(context);
        this.currentUser = currentUser;

        this.prependCustomView(FORM_TYPE);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return FORM_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public long getItemId(int position) {
        Post model = getModel(position);
        if(model == null) {
            return super.getItemId(position);
        } else {
            return model.id;
        }
    }

    public LoadMoreRecyclerViewAdapter.BaseViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        if(viewType == FORM_TYPE) {
            return new PostLineFormViewHolder(inflater.inflate(R.layout.post_line_form, parent, false), presenter);
        } else {
            return super.onCreateCustomViewHolder(parent, viewType);
        }
    }

    @Override
    public PostFeedRecyclerViewAdapter.BaseViewHolder onCreateModelViewHolder(ViewGroup parent) {
        return new PostFeedRecyclerViewAdapter.PostViewHolder(inflater.inflate(R.layout.post, parent, false));
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
        Post model = getModel(position);
        if(model == null) return;
        ((PostFeedRecyclerViewAdapter.PostViewHolder) viewHolder).bindData(presenter, model);
    }

    @Override
    public void onBuildCustomViewHolder(BaseViewHolder viewHolder, int position) {
        if(getItemViewType(position) == FORM_TYPE){
            ((PostFeedRecyclerViewAdapter.PostLineFormViewHolder) viewHolder).bindData(currentUser);
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
                Post model = getModel(position);
                if(model == null) continue;
                postViewHolder.getPostBinder().rebindData(presenter, model, payload);
            }
        }
    }

    public void setPresenter(PostFeedPresenter presenter) {
        this.presenter = presenter;
    }

    public void clearAndAppendPostLineForm() {
        this.clearAndAppendCustomView(FORM_TYPE);
    }

    @Override
    public void onViewRecycled(BaseViewHolder holder) {
        holder.onViewRecycled();
        super.onViewRecycled(holder);
    }

    private static class PostViewHolder extends ModelViewHolder {
        private final PostBinder postBinder;

        PostViewHolder(android.view.View view) {
            super(view);
            this.postBinder = new PostBinder(view.getContext(), view, Constants.LIMIT_LAST_COMMENTS_COUNT_IN_POST_FEED);
        }

        void bindData(PostFeedPresenter presenter, Post post){
            this.postBinder.bind(presenter, post);
        }

        PostBinder getPostBinder() {
            return this.postBinder;
        }

        @Override
        public void onViewRecycled() {
            if(postBinder != null) {
                this.postBinder.unbind();
            }
            super.onViewRecycled();
        }
    }

    static class PostLineFormViewHolder extends ModelViewHolder {
        private PostFeedPresenter presenter;
        @BindView(R.id.draweeview_post_line_user_image)
        SimpleDraweeView userImageDraweeView;
        @BindView(R.id.textview_post_line_user_nickname)
        TextView userNicknameTextView;
        @BindView(R.id.layout_post_line_form)
        RelativeLayout postLineFormLayout;

        PostLineFormViewHolder(android.view.View view, PostFeedPresenter presenter) {
            super(view);
            ButterKnife.bind(this, view);
            this.presenter = presenter;
        }

        void bindData(User user){
            userImageDraweeView.setImageURI(user.image_url);
            userNicknameTextView.setText(String.format(Locale.getDefault(), presenter.getView().getContext().getResources().getString(R.string.new_post_placeholder), user.nickname));
        }

        @OnClick(R.id.layout_post_line_form)
        void onClickPostLineFormLayout() {
            if(presenter == null) return;
            presenter.showPostForm();
        }

        @Override
        public void onViewRecycled() {
            if(postLineFormLayout != null) {
                postLineFormLayout.setOnClickListener(null);
            }
            super.onViewRecycled();
        }
    }

}
