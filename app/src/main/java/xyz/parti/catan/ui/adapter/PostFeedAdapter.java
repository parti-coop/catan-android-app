package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.SmartHtmlTextViewHelper;
import xyz.parti.catan.helper.TextHelper;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.sessions.SessionManager;

public class PostFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final int TYPE_MODEL = 0;
    public final int TYPE_LOAD = 1;

    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    private SessionManager session;
    private Context context;
    List<InfinitableModelHolder<Post>> posts;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading = false, isMoreDataAvailable = true;

    public PostFeedAdapter(Context context, SessionManager session, List<InfinitableModelHolder<Post>> posts) {
        this.context = context;
        this.session = session;
        this.posts = posts;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == TYPE_MODEL) {
            return new PostViewHolder(inflater.inflate(R.layout.dashboard_post, parent, false));
        } else {
            return new LoadHolder(inflater.inflate(R.layout.dashboard_load, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null){
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if(getItemViewType(position) == TYPE_MODEL){
            ((PostViewHolder)viewHolder).bindData(posts.get(position).getModel());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(posts.get(position).isLoader()){
            return TYPE_LOAD;
        }else{
            return TYPE_MODEL;
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class LoadHolder extends RecyclerView.ViewHolder{
        public LoadHolder(View itemView) {
            super(itemView);
        }
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

        public PostViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(Post post){
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

            dashboardPostReferences.removeAllViews();
            dashboardPostReferences.setVisibility(ViewGroup.GONE);

            if(post.file_sources != null) {
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                LinearLayout fileSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_file_sources, dashboardPostReferences, true);
                new FileSourcesBinder(fileSourcesLayout).bindData(post.file_sources);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }

            if(post.link_source != null) {
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                LinearLayout linkSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_link_source, dashboardPostReferences, true);
                new LinkSourceBinder(linkSourcesLayout).bindData(post.link_source);

                dashboardPostReferences.setVisibility(ViewGroup.VISIBLE);
            }
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    public boolean getMoreDataAvailable() {
        return isMoreDataAvailable;
    }

    public void notifyDataChanged(){
        notifyDataSetChanged();
        isLoading = false;
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void clear() {
        this.posts.clear();
        notifyDataSetChanged();
    }
}
