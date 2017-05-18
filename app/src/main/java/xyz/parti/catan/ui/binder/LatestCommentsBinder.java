package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private final LayoutInflater inflater;
    private final Context context;

    LatestCommentsBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        this.context = view.getContext();
        this.inflater =  LayoutInflater.from(view.getContext());
        ButterKnife.bind(this, view);
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


        commentsListLayout.removeAllViews();
        for(Comment comment: post.latest_comments) bindComment(post, comment);

        new ImageHelper(newCommentUserImageView).loadInto(presenter.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        newCommentInputTextView.setOnClickListener(view -> presenter.onClickNewComment(post));
    }

    private void bindComment(Post post, Comment comment) {
        LinearLayout commentLayout = (LinearLayout) inflater.inflate(R.layout.comment, commentsListLayout, false);
        CommentBinder binder = new CommentBinder(commentLayout, presenter);
        binder.bindData(post, comment);

        commentLayout.setTag(R.string.tag_latest_comment_binder, binder);
        commentLayout.setTag(R.string.tag_latest_comment, comment);
        commentsListLayout.addView(commentLayout);
    }

    void rebindComment(Post post, CommentDiff commentDiff) {
        Comment comment = commentDiff.getComment();
        Object payload = commentDiff.getPayload();

        int commentCount = commentsListLayout.getChildCount();
        for(int i = 0; i < commentCount; i++) {
            LinearLayout commentLayout = (LinearLayout) commentsListLayout.getChildAt(i);
            Comment taggingComment = (Comment) commentLayout.getTag(R.string.tag_latest_comment);
            if(taggingComment != null && taggingComment.id != null && taggingComment.id.equals(comment.id)) {
                CommentBinder commentBinder = (CommentBinder) commentLayout.getTag(R.string.tag_latest_comment_binder);
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
        public String getPayload() {
            return payload;
        }
    }
}
