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
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.SmartHtmlTextViewHelper;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Poll;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.models.Survey;
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
        @BindView(R.id.dashboardPostPartiLogo)
        ImageView dashboardPostPartiLogo;
        @BindView(R.id.dashboardPostPartiTitle)
        TextView dashboardPostPartiTitle;
        @BindView(R.id.dashboardPostGroupTitle)
        TextView dashboardPostGroupTitle;
        @BindView(R.id.dashboardPostUserNickname)
        TextView dashboardPostUserNickname;
        @BindView(R.id.dashboardPostUserImage)
        CircleImageView dashboardPostUserImage;
        @BindView(R.id.dashboardPostCreatedAt)
        RelativeTimeTextView dashboardPostCreatedAt;
        @BindView(R.id.dashboardPostBody)
        TextView dashboardPostBody;
        @BindView(R.id.dashboardPostTitle)
        TextView dashboardPostTitle;
        @BindView(R.id.dashboardPostPrefixGroupTitle)
        TextView dashboardPostPrefixGroupTitle;
        @BindView(R.id.dashboardPostReferences)
        LinearLayout dashboardPostReferences;
        @BindView(R.id.dashbardPostLikeButton)
        Button dashbardPostLikeButton;
        @BindView(R.id.dashbardPostNewCommentButton)
        Button dashbardPostNewCommentButton;
        @BindView(R.id.dashbardPostShowLikesButton)
        Button dashbardPostShowLikesButton;
        @BindView(R.id.dashboardPostComments)
        LinearLayout commentsLayout;

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
            new LatestCommentsBinder(presenter, commentsLayout).bindData(post);
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
            dashboardPostReferences.removeAllViews();
            dashboardPostReferences.setVisibility(ViewGroup.GONE);
        }

        private void bindLinkSources(final Post post) {
            if(post.link_source != null) {
                LinearLayout linkSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_link_source, dashboardPostReferences, true);
                linkSourcesLayout.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {
                        presenter.onClickLinkSource(post.link_source.url);
                    }

                });
                new LinkSourceBinder(linkSourcesLayout).bindData(post.link_source);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindFileSources(Post post) {
            if(post.file_sources != null) {
                LinearLayout fileSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_file_sources, dashboardPostReferences, true);
                new FileSourcesBinder(presenter, fileSourcesLayout).bindData(post);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindPoll(final Post post) {
            if(post.poll != null) {
                LinearLayout pollLayout = (LinearLayout) inflater.inflate(R.layout.references_poll, dashboardPostReferences, true);
                new PollBinder(presenter, pollLayout).bindData(post);
                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindSurvey(final Post post) {
            if(post.survey != null) {
                LinearLayout surveyLayout = (LinearLayout) inflater.inflate(R.layout.references_survey, dashboardPostReferences, true);
                new SurveyBinder(presenter, surveyLayout).bindData(post);
                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindLike(final Post post) {
            if(post.is_upvoted_by_me) {
                dashbardPostLikeButton.setTypeface(null, Typeface.BOLD);
                dashbardPostLikeButton.setTextColor(ContextCompat.getColor(context, R.color.style_color_accent));
            } else {
                dashbardPostLikeButton.setTypeface(null, Typeface.NORMAL);
                dashbardPostLikeButton.setTextColor(ContextCompat.getColor(context, R.color.post_button_text));
            }
            dashbardPostLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickLike(post);
                }
            });

            if(post.upvotes_count > 0) {
                dashbardPostShowLikesButton.setText(String.format("{fa-heart} %d", post.upvotes_count));
                dashbardPostShowLikesButton.setVisibility(View.VISIBLE);
            } else {
                dashbardPostShowLikesButton.setVisibility(View.GONE);
            }
        }

        private void bindBasic(final Post post) {
            ImageHelper.loadInto(dashboardPostPartiLogo, post.parti.logo_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
            dashboardPostPartiTitle.setText(post.parti.title);
            if(post.parti.group.isIndie()) {
                dashboardPostPrefixGroupTitle.setVisibility(android.view.View.GONE);
                dashboardPostGroupTitle.setVisibility(android.view.View.GONE);
            } else {
                dashboardPostPrefixGroupTitle.setVisibility(android.view.View.VISIBLE);
                dashboardPostGroupTitle.setVisibility(android.view.View.VISIBLE);
                dashboardPostGroupTitle.setText(post.parti.group.title);
            }
            ImageHelper.loadInto(dashboardPostUserImage, post.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
            dashboardPostUserNickname.setText(post.user.nickname);
            dashboardPostCreatedAt.setReferenceTime(post.created_at.getTime());

            if(TextUtils.isEmpty(post.parsed_title)) {
                dashboardPostTitle.setVisibility(android.view.View.GONE);
            } else {
                dashboardPostTitle.setVisibility(android.view.View.VISIBLE);
                SmartHtmlTextViewHelper.setTextViewHTML(itemView.getContext(), dashboardPostTitle, post.parsed_title);
            }

            if(TextUtils.isEmpty(post.parsed_body)) {
                dashboardPostBody.setVisibility(android.view.View.GONE);
            } else {
                dashboardPostBody.setVisibility(android.view.View.VISIBLE);
                SmartHtmlTextViewHelper.setTextViewHTML(itemView.getContext(), dashboardPostBody, post.parsed_body);
            }

            dashbardPostNewCommentButton.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    presenter.onClickNewComment(post);
                }
            });
        }
    }

}
