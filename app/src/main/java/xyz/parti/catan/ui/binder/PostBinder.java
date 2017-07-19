package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.Poll;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.Survey;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.ui.presenter.BasePostBindablePresenter;
import xyz.parti.catan.ui.view.CommentView;
import xyz.parti.catan.ui.view.LooselyRelativeTimeTextView;
import xyz.parti.catan.ui.view.ReferenceView;
import xyz.parti.catan.ui.view.SmartTextView;


public class PostBinder {
    private Context context;
    private final LatestCommentsBinder latestCommentsBinder;

    @BindView(R.id.draweeview_post_parti_logo)
    SimpleDraweeView partiLogoDraweeView;
    @BindView(R.id.textview_post_parti_title)
    TextView partiTitleTextView;
    @BindView(R.id.textview_post_group_title)
    TextView groupTitleTextView;
    @BindView(R.id.layout_post_latest_activity)
    LinearLayout postLatestActivityLayout;
    @BindView(R.id.smarttextview_post_latest_activity_body)
    SmartTextView postLatestActivityBodySmartTextView;
    @BindView(R.id.textview_post_latest_activity_at)
    LooselyRelativeTimeTextView postLatestActivityAtTextView;
    @BindView(R.id.layout_post_user)
    LinearLayout postUserLayout;
    @BindView(R.id.textview_post_user_nickname)
    TextView userNicknameTextView;
    @BindView(R.id.draweeview_post_user_image)
    SimpleDraweeView userImageDraweeView;
    @BindView(R.id.textview_post_created_at)
    LooselyRelativeTimeTextView createdAtTextView;
    @BindView(R.id.textview_post_body)
    SmartTextView bodyTextView;
    @BindView(R.id.smarttextview_post_title)
    SmartTextView titleSmartTextView;
    @BindView(R.id.textview_prefix_group_title)
    TextView prefixGroupTitleTextView;
    @BindView(R.id.button_post_like)
    Button likeButton;
    @BindView(R.id.button_new_post_comment)
    Button newCommentButton;
    @BindView(R.id.button_show_post_likes)
    Button showLikesButton;
    @BindView(R.id.layout_comments_section)
    LinearLayout commentsSectionLayout;
    @BindView(R.id.referenceview)
    ReferenceView referenceview;

    public PostBinder(Context context, View view, int lastCommentsCount) {
        this(context, view, true, lastCommentsCount);
    }

    public PostBinder(Context context, View view, boolean withCommentForm, int lastCommentsCountLimit) {
        this.context = context.getApplicationContext();
        ButterKnife.bind(this, view);

        latestCommentsBinder = new LatestCommentsBinder(commentsSectionLayout, withCommentForm, lastCommentsCountLimit);
    }

    public void bind(PostBindablePresenter presenter, Post post) {
        unbind();
        bindBasic(presenter, post);
        bindLike(presenter, post);
        bindComments(presenter, post);
        referenceview.bindData(presenter, post);
    }

    private void bindComments(PostBindablePresenter presenter, Post post) {
        latestCommentsBinder.bind(presenter, post);
    }

    private void bindLike(final PostBindablePresenter presenter, final Post post) {
        if(post.is_upvoted_by_me) {
            likeButton.setTypeface(null, Typeface.BOLD);
            likeButton.setTextColor(ContextCompat.getColor(context, R.color.style_color_accent));
        } else {
            likeButton.setTypeface(null, Typeface.NORMAL);
            likeButton.setTextColor(ContextCompat.getColor(context, R.color.post_button_text));
        }
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickLike(post);
            }
        });

        if(post.upvotes_count > 0) {
            showLikesButton.setText(String.format(Locale.getDefault(), "{fa-heart} %d", post.upvotes_count));
            showLikesButton.setVisibility(View.VISIBLE);
        } else {
            showLikesButton.setVisibility(View.GONE);
        }
    }

    private void bindBasic(final PostBindablePresenter presenter, final Post post) {
        partiLogoDraweeView.setImageURI(post.parti.logo_url);
        partiTitleTextView.setText(post.parti.title);
        if(post.parti.group.isIndie()) {
            prefixGroupTitleTextView.setVisibility(android.view.View.GONE);
            groupTitleTextView.setVisibility(android.view.View.GONE);
        } else {
            prefixGroupTitleTextView.setVisibility(android.view.View.VISIBLE);
            groupTitleTextView.setVisibility(android.view.View.VISIBLE);
            groupTitleTextView.setText(post.parti.group.title);
        }

        if(post.latest_stroked_activity != null && post.last_stroked_at != null) {
            postLatestActivityBodySmartTextView.setNoImageRichText(post.latest_stroked_activity);
            postLatestActivityAtTextView.setReferenceTime(post.last_stroked_at.getTime());
            postLatestActivityLayout.setVisibility(View.VISIBLE);
        } else {
            postLatestActivityLayout.setVisibility(View.GONE);
        }

        if(post.wiki == null) {
            postUserLayout.setVisibility(View.VISIBLE);
            userImageDraweeView.setImageURI(post.user.image_url);
            userNicknameTextView.setText(post.user.nickname);
            createdAtTextView.setReferenceTime(post.created_at.getTime());
            createdAtTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickCreatedAt(post);
                }
            });
        } else {
            postUserLayout.setVisibility(View.GONE);
        }

        if(TextUtils.isEmpty(post.parsed_title)) {
            titleSmartTextView.setVisibility(android.view.View.GONE);
        } else {
            titleSmartTextView.setVisibility(android.view.View.VISIBLE);
            titleSmartTextView.setNoImageRichText(post.parsed_title);
        }

        if(TextUtils.isEmpty(post.parsed_body)) {
            bodyTextView.setVisibility(android.view.View.GONE);
        } else {
            bodyTextView.setVisibility(android.view.View.VISIBLE);
            bodyTextView.setRichText(post.parsed_body, post.truncated_parsed_body);
        }

        newCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickNewComment(post);
            }
        });
    }

    public void rebindData(PostBindablePresenter presenter, Post post, Object payload) {
        if (Post.PAYLOAD_LATEST_COMMENT.equals(payload)) {
            this.bindComments(presenter, post);
        } else if (Post.PAYLOAD_IS_UPVOTED_BY_ME.equals(payload)) {
            this.bindLike(presenter, post);
        } else if(payload instanceof Survey) {
            referenceview.bindData(presenter, post);
        } else if(payload instanceof Poll) {
            referenceview.bindData(presenter, post);
        } else if(payload instanceof CommentDiff) {
            latestCommentsBinder.rebindComment(post, (CommentDiff) payload);
        } else {
            CatanLog.d("PostFeedRecyclerView bind : invalid payload");
        }
    }

    public void  highlightComment(final ScrollView scrollView, final Comment comment) {
        if(latestCommentsBinder == null) return;
        latestCommentsBinder.highlightComment(scrollView, comment);
    }

    public void unbind() {
        if(likeButton != null) likeButton.setOnClickListener(null);
        if(newCommentButton != null) newCommentButton.setOnClickListener(null);
        if(createdAtTextView != null) createdAtTextView.setOnClickListener(null);
        if(latestCommentsBinder != null) latestCommentsBinder.unbind();
        if(referenceview != null) referenceview.unbind();
    }

    public interface PostBindablePresenter extends CommentView.Presenter {
        void onClickLinkSource(Post post);
        void onClickNewComment(Post post);
        void onClickLike(Post post);
        void onClickSurveyOption(Post post, Option option, boolean b);
        void onClickPollAgree(Post post);
        void onClickPollDisgree(Post post);
        void onClickMoreComments(Post post);
        void onClickImageFileSource(Post post);
        void onClickDocFileSource(Post post, FileSource docFileSource);
        void onClickCreatedAt(Post post);
        void onClickNewOption(Post post);
        void onClickWikiContent(Post post);
        void reloadPost(Post post, final BasePostBindablePresenter.ReloadCallBack callback);
        User getCurrentUser();
    }
}
