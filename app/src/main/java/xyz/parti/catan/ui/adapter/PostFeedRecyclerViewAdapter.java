package xyz.parti.catan.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.SmartHtmlTextViewHelper;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.sessions.SessionManager;

public class PostFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Post> {
    private Activity activity;
    private ProgressDialog downloadProgressDialog;
    List<InfinitableModelHolder<Post>> posts;
    private SessionManager session;

    public PostFeedRecyclerViewAdapter(Activity activity, ProgressDialog downloadProgressDialog, List<InfinitableModelHolder<Post>> posts, SessionManager session) {
        super(activity, posts);
        this.activity = activity;
        this.downloadProgressDialog = downloadProgressDialog;
        this.posts = posts;
        this.session = session;
    }

    @Override
    public RecyclerView.ViewHolder onCreateModelViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        return new PostFeedRecyclerViewAdapter.PostViewHolder(inflater.inflate(R.layout.dashboard_post, parent, false), this.session, downloadProgressDialog);
    }

    @Override
    public boolean isLoadMorePosition(int position) {
        return position >= getItemCount() - 1;
    }

    @Override
    public void onBildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((PostFeedRecyclerViewAdapter.PostViewHolder)viewHolder).bindData(this.activity, posts.get(position).getModel());
    }

    static public class PostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.dashboardPostPartiTitle)
        TextView dashboardPostPartiTitle;
        @BindView(R.id.dashboardPostGroupTitle)
        TextView dashboardPostGroupTitle;
        @BindView(R.id.dashboardPostUserNickname)
        TextView dashboardPostUserNickname;
        @BindView(R.id.dashboardPostCreatedAt)
        RelativeTimeTextView dashboardPostCreatedAt;
        @BindView(R.id.dashboardPostBody)
        TextView dashboardPostBody;
        @BindView(R.id.dashboardPostTitle)
        TextView dashboardPostTitle;
        @BindView(R.id.dashboardPostPrefixGroupTitle)
        TextView dashboardPostPrefixGroupTitle;
        @BindView(R.id.dashboardPostReferences)
        LinearLayout dashboardPostReferences;
        @BindView(R.id.dashboardPostComments)
        LinearLayout commentsLayout;

        private LayoutInflater inflater;

        private SessionManager session;
        private final ProgressDialog downloadProgressDialog;

        public PostViewHolder(View view, SessionManager session, ProgressDialog downloadProgressDialog) {
            super(view);
            this.session = session;
            this.downloadProgressDialog = downloadProgressDialog;
            this.inflater =  LayoutInflater.from(itemView.getContext());
            ButterKnife.bind(this, view);
        }

        void bindData(Activity activity, Post post){
            bindBasic(post);
            bindComments(post);
            bindReferences(activity, post);
        }

        private void bindComments(Post post) {
            new LatestCommentsBinder(commentsLayout, session).bindData(post);
        }

        private void bindReferences(Activity activity, Post post) {
            dashboardPostReferences.removeAllViews();
            dashboardPostReferences.setVisibility(ViewGroup.GONE);

            bindFileSources(activity, post);
            bindLinkSources(post);
            bindPoll(post);
            bindSurvey(post);
            bindComments(post);
        }

        private void bindLinkSources(final Post post) {
            if(post.link_source != null) {
                LinearLayout linkSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_link_source, dashboardPostReferences, true);
                linkSourcesLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.link_source.url)));
                    }
                });
                new LinkSourceBinder(linkSourcesLayout).bindData(post.link_source);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindFileSources(Activity activity, Post post) {
            if(post.file_sources != null) {
                LinearLayout fileSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_file_sources, dashboardPostReferences, true);
                new FileSourcesBinder(activity, downloadProgressDialog, fileSourcesLayout, session).bindData(post);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindPoll(final Post post) {
            if(post.poll != null) {
                LinearLayout pollLayout = (LinearLayout) inflater.inflate(R.layout.references_poll, dashboardPostReferences, true);
                new PollBinder(pollLayout, session).bindData(post);
                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindSurvey(final Post post) {
            if(post.survey != null) {
                LinearLayout surveyLayout = (LinearLayout) inflater.inflate(R.layout.references_survey, dashboardPostReferences, true);
                new SurveyBinder(surveyLayout, session).bindData(post);
                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindBasic(Post post) {
            dashboardPostPartiTitle.setText(post.parti.title);
            if(post.parti.group.isIndie()) {
                dashboardPostPrefixGroupTitle.setVisibility(View.GONE);
                dashboardPostGroupTitle.setVisibility(View.GONE);
            } else {
                dashboardPostPrefixGroupTitle.setVisibility(View.VISIBLE);
                dashboardPostGroupTitle.setVisibility(View.VISIBLE);
                dashboardPostGroupTitle.setText(post.parti.group.title);
            }
            dashboardPostUserNickname.setText(post.user.nickname);
            dashboardPostCreatedAt.setReferenceTime(post.created_at.getTime());

            if(TextUtils.isEmpty(post.parsed_title)) {
                dashboardPostTitle.setVisibility(View.GONE);
            } else {
                dashboardPostTitle.setVisibility(View.VISIBLE);
                SmartHtmlTextViewHelper.setTextViewHTML(itemView.getContext(), dashboardPostTitle, post.parsed_title);
            }

            if(TextUtils.isEmpty(post.parsed_body)) {
                dashboardPostBody.setVisibility(View.GONE);
            } else {
                dashboardPostBody.setVisibility(View.VISIBLE);
                SmartHtmlTextViewHelper.setTextViewHTML(itemView.getContext(), dashboardPostBody, post.parsed_body);
            }
        }
    }
}
