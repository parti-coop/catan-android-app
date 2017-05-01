package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.activity.AllCommentsActivity;
import xyz.parti.catan.ui.activity.PostImagesViewActivity;

/**
 * Created by dalikim on 2017. 4. 29..
 */

class CommentsBinder {
    @BindView(R.id.dashboardPostCommentsLoadMore)
    TextView loadMoreText;
    @BindView(R.id.dashboardPostCommentsList)
    LinearLayout listLayout;
    @BindView(R.id.commentFormUserImage)
    CircleImageView commentFormUserImageView;
    @BindView(R.id.commentFormInput)
    TextView commentFormInputText;

    private final Context context;
    private ViewGroup view;
    private final SessionManager session;
    private LayoutInflater inflater;

    public CommentsBinder(ViewGroup view, SessionManager session) {
        this.context = view.getContext();
        this.view = view;
        this.session = session;
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
                    Intent intent = new Intent(context, AllCommentsActivity.class);
                    intent.putExtra("post", Parcels.wrap(post));
                    context.startActivity(intent);
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

        ImageHelper.loadInto(commentFormUserImageView, session.getCurrentUser().image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
    }

    private void bindComment(Comment comment) {
        LinearLayout commentLayout = (LinearLayout) inflater.inflate(R.layout.comment, listLayout, false);
        new CommentBinder(commentLayout, session).bindData(comment);
        listLayout.addView(commentLayout);
    }
}
