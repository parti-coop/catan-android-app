package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.R;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.PostsService;
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
        TextView dashboardPostCreatedAt;
        @BindView(R.id.dashboardPostBody)
        TextView dashboardPostBody;

        public PostViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindData(Post post){
            dashboardPostPartiTitle.setText(post.parti.title);
            dashboardPostGroupTitle.setText(post.parti.group.name);
            dashboardPostUserNickname.setText(post.user.nickname);
            dashboardPostCreatedAt.setText(post.created_at);
            dashboardPostBody.setText(post.parsed_body);
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
