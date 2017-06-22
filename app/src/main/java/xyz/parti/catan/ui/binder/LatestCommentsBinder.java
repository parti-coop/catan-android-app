package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.ui.view.CommentView;

/**
 * Created by dalikim on 2017. 4. 29..
 */

public class LatestCommentsBinder {
    @BindView(R.id.textview_comments_load_more)
    TextView loadMoreTextView;
    @BindView(R.id.imageview_new_comment_user_image)
    CircleImageView newCommentUserImageView;
    @BindView(R.id.textview_new_comment_input)
    TextView newCommentInputTextView;
    @BindView(R.id.layout_new_comment_form)
    LinearLayout newCommentFormLayout;

    private final PostBinder.PostBindablePresenter presenter;
    private final Context context;
    private ViewGroup view;
    private boolean withNewCommentForm;
    private int lastCommentsCountLimit;
    private final List<CommentView> commentViews = new ArrayList<>();

    LatestCommentsBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view, boolean withNewCommentForm, int lastCommentsCountLimit) {
        this.presenter = presenter;
        this.context = view.getContext();
        this.view = view;
        this.withNewCommentForm = withNewCommentForm;
        this.lastCommentsCountLimit = lastCommentsCountLimit;
        ButterKnife.bind(this, view);

        if(withNewCommentForm) {
            newCommentFormLayout.setVisibility(View.VISIBLE);
        } else {
            newCommentFormLayout.setVisibility(View.GONE);
        }
    }

    public void bindData(final Post post) {
        if (post.hasMoreComments(lastCommentsCountLimit)) {
            loadMoreTextView.setVisibility(android.view.View.VISIBLE);
            loadMoreTextView.setText(String.format(Locale.getDefault(), context.getResources().getString(R.string.load_more_comments), String.valueOf(post.comments_count)));
            loadMoreTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickMoreComments(post);
                }
            });
        } else {
            loadMoreTextView.setVisibility(android.view.View.GONE);
            loadMoreTextView.setOnClickListener(null);
        }

        List<Comment> lastComments = post.lastComments(lastCommentsCountLimit);
        for (CommentView commentView : commentViews) {
            commentView.hideData();
        }

        for (int i = 0; i < lastComments.size(); i++) {
            CommentView commentView;
            if (i >= commentViews.size()) {
                commentView = new CommentView(context);
                commentView.attachPresenter(presenter);
                commentView.setPadding(context.getResources().getDimensionPixelSize(R.dimen.post_card_padding), 0, context.getResources().getDimensionPixelSize(R.dimen.post_card_padding), 0);
                commentViews.add(commentView);
                view.addView(commentView, 1 + i);
            } else {
                commentView = commentViews.get(i);
            }
            boolean isLineVisible = withNewCommentForm || (i != lastComments.size() - 1);
            bindComment(commentView, post, lastComments.get(i), isLineVisible);
        }

        new ImageHelper(newCommentUserImageView).loadInto(presenter.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        newCommentInputTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickNewComment(post);
            }
        });
    }

    private void bindComment(CommentView commentView, Post post, Comment comment, boolean isLineVisible) {
        commentView.bindData(post, comment, isLineVisible);
    }

    void rebindComment(Post post, CommentDiff commentDiff) {
        Comment comment = commentDiff.getComment();
        Object payload = commentDiff.getPayload();

        for(CommentView commentView : commentViews) {
            if(! commentView.isVisible()) continue;
            Comment taggingComment = commentView.getComment();
            if(taggingComment != null && taggingComment.id != null && taggingComment.id.equals(comment.id)) {
                commentView.rebindData(post, comment, payload);
            }
        }
    }

    public void scrollToComment(ScrollView scrollView, Comment comment) {
        if(commentViews == null) return;
        for(CommentView commentView : commentViews) {
            if(commentView.getComment() != null && commentView.getComment().id.equals(comment.id)) {
                Point childOffset = new Point();
                getDeepChildOffset(scrollView, view.getParent(), view, childOffset);
                // Scroll to child.
                scrollView.smoothScrollTo(0, childOffset.y);
                return;
            }
        }
    }

    private void getDeepChildOffset(ScrollView mainParent, ViewParent parent, ViewGroup child, Point childOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        childOffset.x += child.getLeft();
        childOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, childOffset);
    }
}
