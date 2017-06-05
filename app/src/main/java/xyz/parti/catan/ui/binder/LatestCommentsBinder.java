package xyz.parti.catan.ui.binder;

import android.content.Context;
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
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ImageHelper;

/**
 * Created by dalikim on 2017. 4. 29..
 */

public class LatestCommentsBinder {
    @BindView(R.id.textview_comments_load_more)
    TextView loadMoreTextView;
    @BindView(R.id.layout_comments_list)
    LinearLayout commentsListLayout;
    @BindView(R.id.imageview_new_comment_user_image)
    CircleImageView newCommentUserImageView;
    @BindView(R.id.textview_new_comment_input)
    TextView newCommentInputTextView;

    private final PostBinder.PostBindablePresenter presenter;
    private final Context context;
    private final List<CommentBinder> commentBinders = new ArrayList<>();

    LatestCommentsBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        this.context = view.getContext();
        ButterKnife.bind(this, view);

        int commentCount = commentsListLayout.getChildCount();
        for(int i = 0; i < commentCount; i++) {
            View commentView = commentsListLayout.getChildAt(i);
            commentBinders.add(new CommentBinder(commentView, presenter));
        }
    }

    public void bindData(Post post) {
        if(post.hasMoreComments()) {
            loadMoreTextView.setVisibility(android.view.View.VISIBLE);
            loadMoreTextView.setText("" + post.comments_count + context.getText(R.string.load_more_comments));
            loadMoreTextView.setOnClickListener(view -> presenter.onClickMoreComments(post));
        } else {
            loadMoreTextView.setVisibility(android.view.View.GONE);
            loadMoreTextView.setOnClickListener(null);
        }

        List<Comment> lastComments = post.lastComments(commentBinders.size());
        for(CommentBinder binder : commentBinders) {
            binder.hideData();
        }
        for(int i = 0; i < lastComments.size(); i++) {
            bindComment(commentBinders.get(i), post, lastComments.get(i));
        }

        new ImageHelper(newCommentUserImageView).loadInto(presenter.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        newCommentInputTextView.setOnClickListener(view -> presenter.onClickNewComment(post));
    }

    private void bindComment(CommentBinder commentBinder, Post post, Comment comment) {
        commentBinder.bindData(post, comment);
    }

    void rebindComment(Post post, CommentDiff commentDiff) {
        Comment comment = commentDiff.getComment();
        Object payload = commentDiff.getPayload();

        for(CommentBinder commentBinder : commentBinders) {
            if(! commentBinder.isVisible()) continue;
            Comment taggingComment = commentBinder.getComment();
            if(taggingComment != null && taggingComment.id != null && taggingComment.id.equals(comment.id)) {
                commentBinder.rebindData(post, comment, payload);
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
