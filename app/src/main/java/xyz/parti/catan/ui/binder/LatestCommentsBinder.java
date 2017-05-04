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
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 29..
 */

public class LatestCommentsBinder {
    @BindView(R.id.textview_comments_load_more)
    TextView loadMoreTextView;
    @BindView(R.id.layout_comments_list)
    LinearLayout listLayout;
    @BindView(R.id.imageview_new_comment_user_image)
    CircleImageView newCommentUserImageView;
    @BindView(R.id.textview_new_comment_input)
    TextView newCommentInputTextView;

    private final PostFeedPresenter presenter;
    private final Context context;
    private LayoutInflater inflater;

    public LatestCommentsBinder(PostFeedPresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        this.context = view.getContext();
        this.inflater =  LayoutInflater.from(view.getContext());
        ButterKnife.bind(this, view);
    }

    public void bindData(final Post post) {
        if(post.hasMoreComments()) {
            loadMoreTextView.setVisibility(android.view.View.VISIBLE);
            loadMoreTextView.setText("" + post.comments_count + context.getText(R.string.load_more_comments));
            loadMoreTextView.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View view) {
                    presenter.onClickMoreComments(post);
                }
            });
        } else {
            loadMoreTextView.setVisibility(android.view.View.GONE);
            loadMoreTextView.setOnClickListener(null);
        }

        listLayout.removeAllViews();
        for(Comment comment: post.latest_comments) {
            bindComment(comment);
        }

        ImageHelper.loadInto(newCommentUserImageView, presenter.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);

        newCommentInputTextView.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                presenter.onClickNewComment(post);
            }
        });
    }



    private void bindComment(Comment comment) {
        LinearLayout commentLayout = (LinearLayout) inflater.inflate(R.layout.comment, listLayout, false);
        new CommentBinder(commentLayout).bindData(comment);
        listLayout.addView(commentLayout);
    }
}
