package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.helper.TextHelper;

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
    RelativeTimeTextView createdAtTextView;
    @BindView(R.id.button_new_comment)
    Button newCommentButton;
    @BindView(R.id.view_divider)
    View dividerView;

    private final Context context;
    private final PostBinder.PostBindablePresenter presenter;

    public CommentBinder(View view, PostBinder.PostBindablePresenter presenter) {
        this.context = view.getContext();
        this.presenter = presenter;
        ButterKnife.bind(this, view);
    }

    public void bindData(Post post, Comment comment) {
        bindData(post, comment, true);
    }
    public void bindData(Post post, Comment comment, boolean isLineVisible) {
        new ImageHelper(userImageImageView).loadInto(comment.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        userNicknameTextView.setText(comment.user.nickname);
        new TextHelper(context).setTextViewHTML(bodyTextView, comment.body);
        createdAtTextView.setReferenceTime(comment.created_at.getTime());

        if(isLineVisible) {
            dividerView.setVisibility(View.VISIBLE);
        } else {
            dividerView.setVisibility(View.GONE);
        }
        newCommentButton.setOnClickListener(view -> presenter.onClickNewComment(post, comment));
    }
}
