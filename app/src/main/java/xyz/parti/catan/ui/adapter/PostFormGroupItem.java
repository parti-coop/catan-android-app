package xyz.parti.catan.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Group;


public class PostFormGroupItem extends AbstractItem<PostFormGroupItem, PostFormGroupItem.ViewHolder> {
    Group group;

    public PostFormGroupItem(Group group) {
        this.group = group;
    }

    @Override
    public int getType() {
        return R.id.post_form_group;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.post_form_group;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        holder.textView.setText(group.title);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textview_group_title)
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}