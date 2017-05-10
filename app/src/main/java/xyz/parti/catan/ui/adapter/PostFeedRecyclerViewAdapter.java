package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Poll;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.Survey;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.TextHelper;
import xyz.parti.catan.ui.binder.FileSourcesBinder;
import xyz.parti.catan.ui.binder.LatestCommentsBinder;
import xyz.parti.catan.ui.binder.LinkSourceBinder;
import xyz.parti.catan.ui.binder.PollBinder;
import xyz.parti.catan.ui.binder.SurveyBinder;
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
        return new PostFeedRecyclerViewAdapter.PostViewHolder(inflater.inflate(R.layout.dashboard_post, parent, false), presenter);
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
    public void onBildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((PostFeedRecyclerViewAdapter.PostViewHolder)viewHolder).bindData(getModel(position));
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
                if (Post.PLAYLOAD_LATEST_COMMENT.equals(payload)) {
                    postViewHolder.bindComments(getModel(position));
                } else if (Post.IS_UPVOTED_BY_ME.equals(payload)) {
                    postViewHolder.bindLike(getModel(position));
                } else if(payload instanceof Survey) {
                    postViewHolder.bindReferences(getModel(position));
                } else if(payload instanceof Poll) {
                    postViewHolder.bindReferences(getModel(position));
                } else {
                    Log.d(Constants.TAG, "PostFeedRecyclerView bind : invalid playload");
                }
            }
        }
    }

    public void setPresenter(PostFeedPresenter presenter) {
        this.presenter = presenter;
    }

    static class PostViewHolder extends ModelViewHolder {
        @BindView(R.id.imageview_parti_logo)
        ImageView partiLogoImageView;
        @BindView(R.id.textview_parti_title)
        TextView partiTitleTextView;
        @BindView(R.id.textview_group_title)
        TextView groupTitleTextView;
        @BindView(R.id.textview_user_nickname)
        TextView userNicknameTextView;
        @BindView(R.id.imageview_user_image)
        CircleImageView userImageImageView;
        @BindView(R.id.textview_created_at)
        RelativeTimeTextView createdAtTextView;
        @BindView(R.id.textview_body)
        TextView bodyTextView;
        @BindView(R.id.textview_title)
        TextView titleTextView;
        @BindView(R.id.textview_prefix_group_title)
        TextView prefixGroupTitleTextView;
        @BindView(R.id.layout_references)
        LinearLayout referencesLayout;
        @BindView(R.id.button_like)
        Button likeButton;
        @BindView(R.id.button_new_comment)
        Button newCommentButton;
        @BindView(R.id.button_show_likes)
        Button showLikesButton;
        @BindView(R.id.layout_comments_section)
        LinearLayout commentsSectionLayout;

        private final PostFeedPresenter presenter;
        private final Context context;
        private LayoutInflater inflater;

        PostViewHolder(android.view.View view, PostFeedPresenter presenter) {
            super(view);
            this.context = view.getContext();
            this.presenter = presenter;
            this.inflater =  LayoutInflater.from(itemView.getContext());
            ButterKnife.bind(this, view);
        }

        void bindData(Post post){
            bindBasic(post);
            bindLike(post);
            bindComments(post);
            bindReferences(post);
        }

        private void bindComments(Post post) {
            new LatestCommentsBinder(presenter, commentsSectionLayout).bindData(post);
        }

        private void bindReferences(Post post) {
            resetReferences();

            bindFileSources(post);
            bindLinkSources(post);
            bindPoll(post);
            bindSurvey(post);
            bindComments(post);
        }

        private void resetReferences() {
            referencesLayout.removeAllViews();
            referencesLayout.setVisibility(ViewGroup.GONE);
        }

        private void bindLinkSources(final Post post) {
            if(post.link_source != null) {
                LinearLayout linkSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_link_source, referencesLayout, true);
                linkSourcesLayout.setOnClickListener(view -> presenter.onClickLinkSource(post));
                new LinkSourceBinder(linkSourcesLayout).bindData(post.link_source);

                referencesLayout.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindFileSources(Post post) {
            if(post.file_sources != null) {
                LinearLayout fileSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_file_sources, referencesLayout, true);
                new FileSourcesBinder(presenter, fileSourcesLayout).bindData(post);

                referencesLayout.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindPoll(final Post post) {
            if(post.poll != null) {
                LinearLayout pollLayout = (LinearLayout) inflater.inflate(R.layout.references_poll, referencesLayout, true);
                new PollBinder(presenter, pollLayout).bindData(post);
                referencesLayout.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindSurvey(final Post post) {
            if(post.survey != null) {
                LinearLayout surveyLayout = (LinearLayout) inflater.inflate(R.layout.references_survey, referencesLayout, true);
                new SurveyBinder(presenter, surveyLayout).bindData(post);
                referencesLayout.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindLike(final Post post) {
            if(post.is_upvoted_by_me) {
                likeButton.setTypeface(null, Typeface.BOLD);
                likeButton.setTextColor(ContextCompat.getColor(context, R.color.style_color_accent));
            } else {
                likeButton.setTypeface(null, Typeface.NORMAL);
                likeButton.setTextColor(ContextCompat.getColor(context, R.color.post_button_text));
            }
            likeButton.setOnClickListener(view -> presenter.onClickLike(post));

            if(post.upvotes_count > 0) {
                showLikesButton.setText(String.format("{fa-heart} %d", post.upvotes_count));
                showLikesButton.setVisibility(View.VISIBLE);
            } else {
                showLikesButton.setVisibility(View.GONE);
            }
        }

        private void bindBasic(final Post post) {
            new ImageHelper(partiLogoImageView).loadInto(post.parti.logo_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
            partiTitleTextView.setText(post.parti.title);
            if(post.parti.group.isIndie()) {
                prefixGroupTitleTextView.setVisibility(android.view.View.GONE);
                groupTitleTextView.setVisibility(android.view.View.GONE);
            } else {
                prefixGroupTitleTextView.setVisibility(android.view.View.VISIBLE);
                groupTitleTextView.setVisibility(android.view.View.VISIBLE);
                groupTitleTextView.setText(post.parti.group.title);
            }
            new ImageHelper(partiLogoImageView).loadInto(post.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
            userNicknameTextView.setText(post.user.nickname);
            createdAtTextView.setReferenceTime(post.created_at.getTime());

            if(TextUtils.isEmpty(post.parsed_title)) {
                titleTextView.setVisibility(android.view.View.GONE);
            } else {
                titleTextView.setVisibility(android.view.View.VISIBLE);
                new TextHelper(context).setTextViewHTML(titleTextView, post.parsed_title);
            }

            if(TextUtils.isEmpty(post.parsed_body)) {
                bodyTextView.setVisibility(android.view.View.GONE);
            } else {
                bodyTextView.setVisibility(android.view.View.VISIBLE);
                new TextHelper(context).setTextViewHTML(bodyTextView, post.parsed_body);
            }

            newCommentButton.setOnClickListener(view -> presenter.onClickNewComment(post));
        }
    }

}
