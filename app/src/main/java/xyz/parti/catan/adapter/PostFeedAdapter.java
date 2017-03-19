package xyz.parti.catan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;

public class PostFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int itemCount;

    public PostFeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.dashboard_post, parent, false);
        return new CellFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CellFeedViewHolder holder = (CellFeedViewHolder) viewHolder;
        holder.dashboardPostPartiTitle.setText("빠띠이름");
        holder.dashboardPostGroupTitle.setText("그룹이름");
        holder.dashboardPostUserNickname.setText("글쓴이");
        holder.dashboardPostCreatedAt.setText("3분 전");
        holder.dashboardPostBody.setText("글 내용");
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public void updateItems() {
        itemCount = 10;
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
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

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
