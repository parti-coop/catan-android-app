package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.SmartHtmlTextViewHelper;
import xyz.parti.catan.models.Comment;

/**
 * Created by dalikim on 2017. 4. 29..
 */

public class CommentBinder {
    @BindView(R.id.commentUserImage)
    CircleImageView userImageiew;
    @BindView(R.id.commentUserNickname)
    TextView userNicknameText;
    @BindView(R.id.commentBody)
    TextView bodyText;
    @BindView(R.id.commentCreatedAt)
    RelativeTimeTextView createdAtText;
    @BindView(R.id.commentLine)
    View commentLineView;

    private final Context context;

    public CommentBinder(View view) {
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    public void bindData(Comment comment) {
        bindData(comment, true);
    }
    public void bindData(Comment comment, boolean isLineVisible) {
        ImageHelper.loadInto(userImageiew, comment.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        userNicknameText.setText(comment.user.nickname);
        SmartHtmlTextViewHelper.setTextViewHTML(context, bodyText, comment.body);
        createdAtText.setReferenceTime(comment.created_at.getTime());

        if(isLineVisible) {
            commentLineView.setVisibility(View.VISIBLE);
        } else {
            commentLineView.setVisibility(View.GONE);
        }
    }
}
