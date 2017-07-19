package xyz.parti.catan.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.CatanLog;


public class CommentView extends LinearLayout implements View.OnCreateContextMenuListener {
    private Post post;
    private Comment comment;
    private WeakReference<Presenter> presenter = new WeakReference<>(null);

    @BindView(R.id.draweeview_comment_user_image)
    SimpleDraweeView userImageDraweeView;
    @BindView(R.id.textview_comment_user_nickname)
    TextView userNicknameTextView;
    @BindView(R.id.smarttextview_comment_body)
    SmartTextView bodySmartTextView;
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
    @BindView(R.id.layout_comment_line)
    LinearLayout commentLineLayout;
    @BindView(R.id.layout_blind_comment)
    LinearLayout blindCommentLayout;
    @BindView(R.id.imageview_blind)
    CircleImageView blindImageView;

    private boolean isHighlighted = false;
    private MenuItem destroyMenu;

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

    private void init(@NonNull final Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_comment, this);
        setOrientation(LinearLayout.VERTICAL);
        ButterKnife.bind(this);
        DrawableCompat.setTint(blindImageView.getDrawable(), ContextCompat.getColor(getContext(), R.color.text_muted));
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

    public void highlight() {
        if(isHighlighted) return;
        isHighlighted = true;

        Drawable originalBgDrawable = CommentView.this.getBackground();
        if(originalBgDrawable == null) originalBgDrawable = new ColorDrawable(Color.TRANSPARENT);
        Drawable highlightBgDrawable = ContextCompat.getDrawable(getContext(), R.drawable.highlight_comment_bg);
        Drawable[] color = {highlightBgDrawable, originalBgDrawable};
        TransitionDrawable trans = new TransitionDrawable(color);
        trans.setCrossFadeEnabled(true);
        CommentView.this.setBackground(trans);
        trans.startTransition(3000);
    }

    public void bindData(final Post post, final Comment comment, boolean isLineVisible) {
        clearListeners();

        if(comment.is_blinded) {
            blindCommentLayout.setVisibility(VISIBLE);
            commentLineLayout.setVisibility(GONE);
            commentLineLayout.setOnCreateContextMenuListener(null);
        } else {
            blindCommentLayout.setVisibility(GONE);
            commentLineLayout.setVisibility(VISIBLE);
            if(comment.is_destroyable) {
                commentLineLayout.setOnCreateContextMenuListener(this);
            } else {
                commentLineLayout.setOnCreateContextMenuListener(null);
            }

            this.post = post;
            this.comment = comment;
            setTag(R.id.tag_comment, comment);

            userImageDraweeView.setImageURI(comment.user.image_url);
            userNicknameTextView.setText(comment.user.nickname);
            bodySmartTextView.setRichText(comment.body, comment.truncated_body);
            createdAtTextView.setReferenceTime(comment.created_at.getTime());

            if (isLineVisible) {
                dividerView.setVisibility(View.VISIBLE);
            } else {
                dividerView.setVisibility(View.GONE);
            }
            newCommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (presenter.get() == null) return;
                    presenter.get().onClickNewComment(post, comment);
                }
            });
            bindLike(post, comment);
        }
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        String destroyMenuName = getContext().getResources().getString(R.string.comment_context_menu);
        destroyMenu = menu.add(Menu.NONE, 1, 1, destroyMenuName);
        destroyMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (presenter.get() == null) return false;
                presenter.get().onClickDestroyComment(post, comment);
                return true;
            }
        });
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
            showLikesButton.setText(String.format(Locale.getDefault(), "{fa-heart} %d", comment.upvotes_count));
            showLikesButton.setVisibility(View.VISIBLE);
        } else {
            showLikesButton.setVisibility(View.GONE);
        }
    }

    public void rebindData(Post post, Comment comment, Object payload) {
        if(payload.equals(Comment.PAYLOAD_IS_UPVOTED_BY_ME)) {
            bindLike(post, comment);
        } else {
            CatanLog.d("CommentView : invalid payload");
        }
    }

    public void clearData() {
        unbind();
        setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return getVisibility() == View.VISIBLE;
    }

    public Comment getComment() {
        return comment;
    }

    public interface Presenter {
        void onClickLikeComment(Post post, Comment comment);
        void onClickNewComment(Post post, Comment comment);
        void onClickDestroyComment(final Post post, final Comment comment);
    }

    public void unbind() {
        setTag(R.id.tag_comment, null);
        if(comment != null) {
            comment = null;
        }
        if(post != null) {
            post = null;
        }
        clearListeners();
    }

    private void clearListeners() {
        if(newCommentButton != null) {
            newCommentButton.setOnClickListener(null);
        }
        if(likeButton != null) {
            likeButton.setOnClickListener(null);
        }
        if(commentLineLayout != null) {
            commentLineLayout.setOnLongClickListener(null);
        }
        if(destroyMenu != null) {
            destroyMenu.setOnMenuItemClickListener(null);
        }
    }
}
