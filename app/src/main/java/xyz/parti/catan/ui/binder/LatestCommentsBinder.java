package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.Constants;
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

    private final PostBinder.PostBindablePresenter presenter;
    private final Context context;
    private ViewGroup view;
    private final List<CommentView> commentViews = new ArrayList<>();

    LatestCommentsBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        this.context = view.getContext();
        this.view = view;
        ButterKnife.bind(this, view);
    }

    public void bindData(final Post post) {
        if(post.hasMoreComments()) {
            loadMoreTextView.setVisibility(android.view.View.VISIBLE);
            loadMoreTextView.setText("" + post.comments_count + context.getText(R.string.load_more_comments));
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

        List<Comment> lastComments = post.lastComments(3);
        for(CommentView commentView : commentViews) {
            commentView.hideData();
        }
        for(int i = 0; i < lastComments.size(); i++) {
            CommentView commentView;
            if(i >= commentViews.size()) {
                commentView = new CommentView(context);
                commentView.attachPresenter(presenter);
                LinearLayout.LayoutParams parmas = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                commentView.setLayoutParams(parmas);
                commentView.setOrientation(LinearLayout.VERTICAL);
                commentViews.add(commentView);
                view.addView(commentView, 1 + i);
            } else {
                commentView = commentViews.get(i);
            }
            bindComment(commentView, post, lastComments.get(i));
        }

        new ImageHelper(newCommentUserImageView).loadInto(presenter.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        newCommentInputTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickNewComment(post);
            }
        });
    }

    private void bindComment(CommentView commentView, Post post, Comment comment) {
        commentView.bindData(post, comment);
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

    public static class CommentDiff {
        private final Comment comment;
        private final String payload;

        public CommentDiff(Comment comment, String payload) {
            this.comment = comment;
            this.payload = payload;
        }

        public Comment getComment() {
            return comment;
        }
        String getPayload() {
            return payload;
        }
    }
}
