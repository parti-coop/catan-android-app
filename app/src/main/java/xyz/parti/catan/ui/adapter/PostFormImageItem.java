package xyz.parti.catan.ui.adapter;

import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 5. 26..
 */

public class PostFormImageItem extends AbstractItem<PostFormImageItem, PostFormImageItem.ViewHolder> {
    private Uri url;

    public PostFormImageItem(Uri url) {
        this.url = url;
    }

    @Override
    public int getType() {
        return R.id.post_form_image;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.post_form_image;
    }

    @Override
    public void bindView(PostFormImageItem.ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        holder.previewDraweeView.setAdjustViewBounds(true);
        holder.previewDraweeView.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0f));
        holder.previewDraweeView.setImageURI(url);
    }

    @Override
    public PostFormImageItem.ViewHolder getViewHolder(View v) {
        return new PostFormImageItem.ViewHolder(v);
    }

    public Uri getUrl() {
        return url;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.darweeview_preview)
        SimpleDraweeView previewDraweeView;
        @BindView(R.id.textview_remove)
        public ImageView removeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
