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
    @BindView(R.id.imageview_user_image)
    CircleImageView userImageImageView;
    @BindView(R.id.textview_user_nickname)
    TextView userNicknameTextView;
    @BindView(R.id.textview_body)
    TextView bodyTextView;
    @BindView(R.id.textview_created_at)
    RelativeTimeTextView createdAtTextView;
    @BindView(R.id.view_divider)
    View dividerView;

    private final Context context;

    public CommentBinder(View view) {
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    public void bindData(Comment comment) {
        bindData(comment, true);
    }
    public void bindData(Comment comment, boolean isLineVisible) {
        ImageHelper.loadInto(userImageImageView, comment.user.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        userNicknameTextView.setText(comment.user.nickname);
        SmartHtmlTextViewHelper.setTextViewHTML(context, bodyTextView, comment.body);
        createdAtTextView.setReferenceTime(comment.created_at.getTime());

        if(isLineVisible) {
            dividerView.setVisibility(View.VISIBLE);
        } else {
            dividerView.setVisibility(View.GONE);
        }
    }
}
