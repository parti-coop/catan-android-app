package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
    @BindView(R.id.dashboardPostCommentsLoadMore)
    TextView loadMoreText;
    @BindView(R.id.dashboardPostCommentsList)
    LinearLayout listLayout;
    @BindView(R.id.commentFormUserImage)
    CircleImageView commentFormUserImageView;
    @BindView(R.id.commentFormInput)
    TextView commentFormInputText;

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
            loadMoreText.setVisibility(View.VISIBLE);
            loadMoreText.setText("" + post.comments_count + context.getText(R.string.load_more_comments));
            loadMoreText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickMoreComments(post);
                }
            });
        } else {
            loadMoreText.setVisibility(View.GONE);
            loadMoreText.setOnClickListener(null);
        }

        listLayout.removeAllViews();
        for(Comment comment: post.latest_comments) {
            bindComment(comment);
        }

        ImageHelper.loadInto(commentFormUserImageView, presenter.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);

        commentFormInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickMoreComments(post);
            }
        });
    }



    private void bindComment(Comment comment) {
        LinearLayout commentLayout = (LinearLayout) inflater.inflate(R.layout.comment, listLayout, false);
        new CommentBinder(commentLayout).bindData(comment);
        listLayout.addView(commentLayout);
    }
}
