package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.view.CommentView;


class LatestCommentsBinder {
    @BindView(R.id.textview_comments_load_more)
    TextView loadMoreTextView;
    @BindView(R.id.draweeview_new_comment_user_image)
    SimpleDraweeView newCommentUserDraweeView;
    @BindView(R.id.textview_new_comment_input)
    TextView newCommentInputTextView;
    @BindView(R.id.layout_new_comment_form)
    LinearLayout newCommentFormLayout;

    private Context context;
    private ViewGroup view;
    private boolean withNewCommentForm;
    private int lastCommentsCountLimit;
    private final List<CommentView> commentViews = new ArrayList<>();

    LatestCommentsBinder(ViewGroup view, boolean withNewCommentForm, int lastCommentsCountLimit) {
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

    public void bind(final PostBinder.PostBindablePresenter presenter, final Post post) {
        unbind();

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

        newCommentUserDraweeView.setImageURI(presenter.getCurrentUser().image_url);
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

    void highlightComment(final ScrollView scrollView, Comment comment) {
        if(commentViews == null) return;

        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);

        for(final CommentView commentView : commentViews) {
            if(commentView.getComment() != null && commentView.getComment().id.equals(comment.id)) {
                if (commentView.getLocalVisibleRect(scrollBounds)) {
                    commentView.highlight();
                }

                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Point childOffset = new Point();
                        getDeepChildOffset(scrollView, commentView.getParent(), commentView, childOffset);
                        scrollView.smoothScrollTo(0, childOffset.y);
                        commentView.highlight();
                    }
                }, 1500);
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

    void unbind() {
        if(loadMoreTextView != null) loadMoreTextView.setOnClickListener(null);
        if(newCommentInputTextView != null) newCommentInputTextView.setOnClickListener(null);
        for(CommentView commentView : commentViews) {
            if(commentView != null) commentView.unbind();
        }
    }
}
