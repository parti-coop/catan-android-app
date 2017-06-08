package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.TextHelper;
import xyz.parti.catan.ui.view.LooselyRelativeTimeTextView;

/**
 * Created by dalikim on 2017. 4. 29..
 */

public class CommentBinder {
    @BindView(R.id.imageview_user_image)
    CircleImageView userImageImageView;
    @BindView(R.id.textview_user_nickname)
    TextView userNicknameTextView;
    @BindView(R.id.textview_body)
    TextView bodyTextView;
    @BindView(R.id.textview_created_at)
    LooselyRelativeTimeTextView createdAtTextView;
    @BindView(R.id.button_new_comment)
    Button newCommentButton;
    @BindView(R.id.view_divider)
    View dividerView;
    @BindView(R.id.button_like)
    Button likeButton;
    @BindView(R.id.button_show_likes)
    Button showLikesButton;

    private final View view;
    private final Context context;
    private final CommentLikablePresenter presenter;
    private Comment comment;

    public CommentBinder(View view, CommentLikablePresenter presenter) {
        this.view = view;
        this.context = view.getContext();
        this.presenter = presenter;
        ButterKnife.bind(this, view);

        Object commentTagData = view.getTag(R.id.tag_latest_comment);
        if(commentTagData != null) {
            comment = (Comment) commentTagData;
        }
    }

    public void bindData(Post post, Comment comment) {
        this.comment = comment;
        view.setTag(R.id.tag_latest_comment, comment);

        bindData(post, comment, true);
        view.setVisibility(View.VISIBLE);
    }
    public void bindData(final Post post, final Comment comment, boolean isLineVisible) {
        new ImageHelper(userImageImageView).loadInto(comment.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        userNicknameTextView.setText(comment.user.nickname);
        new TextHelper(context).setTextViewHTML(bodyTextView, comment.body, comment.truncated_body);
        createdAtTextView.setReferenceTime(comment.created_at.getTime());

        if(isLineVisible) {
            dividerView.setVisibility(View.VISIBLE);
        } else {
            dividerView.setVisibility(View.GONE);
        }
        newCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickNewComment(post, comment);
            }
        });

        bindLike(post, comment);
    }

    private void bindLike(final Post post, final Comment comment) {
        if(comment.is_upvoted_by_me) {
            likeButton.setTypeface(null, Typeface.BOLD);
            likeButton.setTextColor(ContextCompat.getColor(context, R.color.style_color_accent));
        } else {
            likeButton.setTypeface(null, Typeface.NORMAL);
            likeButton.setTextColor(ContextCompat.getColor(context, R.color.comment_button_text));
        }
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickLike(post, comment);
            }
        });
        if(comment.upvotes_count > 0) {
            showLikesButton.setText(String.format("{fa-heart} %d", comment.upvotes_count));
            showLikesButton.setVisibility(View.VISIBLE);
        } else {
            showLikesButton.setVisibility(View.GONE);
        }
    }

    public void rebindData(Post post, Comment comment, Object payload) {
        if(payload.equals(Comment.IS_UPVOTED_BY_ME)) {
            bindLike(post, comment);
        } else {
            Log.d(Constants.TAG, "CommentBinder : invalid playload");
        }
    }

    public void hideData() {
        this.comment = null;
        view.setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return view.getVisibility() == View.VISIBLE;
    }

    public Comment getComment() {
        return comment;
    }

    /**
     * Created by dalikim on 2017. 5. 18..
     */

    public interface CommentLikablePresenter {
        void onClickLike(Post post, Comment comment);
        void onClickNewComment(Post post, Comment comment);
    }
}
