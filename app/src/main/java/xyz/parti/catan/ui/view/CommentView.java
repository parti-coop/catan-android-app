package xyz.parti.catan.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.TextHelper;

/**
 * Created by dalikim on 2017. 6. 14..
 */

public class CommentView extends LinearLayout {
    private Comment comment;
    private WeakReference<Presenter> presenter = new WeakReference<Presenter>(null);

    @BindView(R.id.imageview_comment_user_image)
    CircleImageView userImageImageView;
    @BindView(R.id.textview_comment_user_nickname)
    TextView userNicknameTextView;
    @BindView(R.id.textview_comment_body)
    TextView bodyTextView;
    @BindView(R.id.textview_comment_created_at)
    LooselyRelativeTimeTextView createdAtTextView;
    @BindView(R.id.button_new_comment)
    Button newCommentButton;
    @BindView(R.id.view_comment_divider)
    View dividerView;
    @BindView(R.id.button_comment_like)
    Button likeButton;
    @BindView(R.id.button_show_comment_likes)
    Button showLikesButton;

    public CommentView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommentView(Context context) {
        super(context);
        init(context);
        LinearLayout.LayoutParams parmas = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(parmas);
    }

    private void init(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_comment, this);
        setOrientation(LinearLayout.VERTICAL);
        ButterKnife.bind(this);
        this.presenter = new WeakReference<>(null);
    }

    public void attachPresenter(Presenter presenter) {
        this.presenter = new WeakReference<>(presenter);
    }

    public void detachPresenter() {
        if(this.presenter != null && this.presenter.get() != null) {
            this.presenter.clear();
            this.presenter = null;
        }
    }

    public void bindData(Post post, Comment comment) {
        bindData(post, comment, true);
    }

    public void bindData(final Post post, final Comment comment, boolean isLineVisible) {
        this.comment = comment;
        setTag(R.id.tag_comment, comment);

        new ImageHelper(userImageImageView).loadInto(comment.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        userNicknameTextView.setText(comment.user.nickname);
        new TextHelper(this.getContext()).setTextViewHTML(bodyTextView, comment.body, comment.truncated_body);
        createdAtTextView.setReferenceTime(comment.created_at.getTime());

        if(isLineVisible) {
            dividerView.setVisibility(View.VISIBLE);
        } else {
            dividerView.setVisibility(View.GONE);
        }
        newCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(presenter.get() == null) return;
                presenter.get().onClickNewComment(post, comment);
            }
        });

        bindLike(post, comment);

        setVisibility(View.VISIBLE);
    }

    private void bindLike(final Post post, final Comment comment) {
        if(comment.is_upvoted_by_me) {
            likeButton.setTypeface(null, Typeface.BOLD);
            likeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.style_color_accent));
        } else {
            likeButton.setTypeface(null, Typeface.NORMAL);
            likeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.comment_button_text));
        }
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(presenter.get() == null) return;
                presenter.get().onClickLikeComment(post, comment);
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
            Log.d(Constants.TAG, "CommentView : invalid playload");
        }
    }

    public void hideData() {
        this.comment = null;
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public Comment getComment() {
        return comment;
    }

    /**
     * Created by dalikim on 2017. 5. 18..
     */

    public interface Presenter {
        void onClickLikeComment(Post post, Comment comment);
        void onClickNewComment(Post post, Comment comment);
    }
}
