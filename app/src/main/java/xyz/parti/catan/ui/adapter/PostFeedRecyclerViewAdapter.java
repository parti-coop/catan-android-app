package xyz.parti.catan.ui.adapter;

import android.content.Context;
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
import xyz.parti.catan.ui.binder.FileSourcesBinder;
import xyz.parti.catan.ui.binder.LatestCommentsBinder;
import xyz.parti.catan.ui.binder.LinkSourceBinder;
import xyz.parti.catan.ui.binder.PollBinder;
import xyz.parti.catan.ui.binder.SurveyBinder;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class PostFeedRecyclerViewAdapter extends LoadMoreRecyclerViewAdapter<Post> {
    private final Context context;
    private List<InfinitableModelHolder<Post>> posts;
    private PostFeedPresenter presenter;

    public PostFeedRecyclerViewAdapter(Context context, List<InfinitableModelHolder<Post>> posts) {
        super(context, posts);
        this.context = context;
        this.posts = posts;
    }

    @Override
    public RecyclerView.ViewHolder onCreateModelViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new PostFeedRecyclerViewAdapter.PostViewHolder(inflater.inflate(R.layout.dashboard_post, parent, false));
    }

    @Override
    public boolean isLoadMorePosition(int position) {
        return position >= getItemCount() - 1;
    }

    @Override
    public void onBildModelViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((PostFeedRecyclerViewAdapter.PostViewHolder)viewHolder).bindData(posts.get(position).getModel());
    }

    public void setPresenter(PostFeedPresenter presenter) {
        this.presenter = presenter;
    }

    public void rebindData(Post newPost) {
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i).getModel();
            if (post.id != null && post.id.equals(newPost.id)) {
                notifyItemChanged(i);
            }
        }
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
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

        PostViewHolder(View view) {
            super(view);
            this.inflater =  LayoutInflater.from(itemView.getContext());
            ButterKnife.bind(this, view);
        }

        void bindData(Post post){
            bindBasic(post);
            bindComments(post);
            bindReferences(post);
        }

        private void bindComments(Post post) {
            new LatestCommentsBinder(presenter, commentsLayout).bindData(post);
        }

        private void bindReferences(Post post) {
            dashboardPostReferences.removeAllViews();
            dashboardPostReferences.setVisibility(ViewGroup.GONE);

            bindFileSources(post);
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
//                        itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.link_source.attachment_url)));
                        presenter.onClickLinkSource(post.link_source.url);
                    }

                });
                new LinkSourceBinder(linkSourcesLayout).bindData(post.link_source);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindFileSources(Post post) {
            if(post.file_sources != null) {
                LinearLayout fileSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_file_sources, dashboardPostReferences, true);
                new FileSourcesBinder(presenter, fileSourcesLayout).bindData(post);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindPoll(final Post post) {
            if(post.poll != null) {
                LinearLayout pollLayout = (LinearLayout) inflater.inflate(R.layout.references_poll, dashboardPostReferences, true);
                new PollBinder(presenter, pollLayout).bindData(post);
                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }

        private void bindSurvey(final Post post) {
            if(post.survey != null) {
                LinearLayout surveyLayout = (LinearLayout) inflater.inflate(R.layout.references_survey, dashboardPostReferences, true);
                new SurveyBinder(presenter, surveyLayout).bindData(post);
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
