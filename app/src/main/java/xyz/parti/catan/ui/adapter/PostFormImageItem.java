package xyz.parti.catan.ui.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.ui.view.CropTopImageView;

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

        holder.previewImageView.setAdjustViewBounds(true);
        new ImageHelper(holder.previewImageView).loadInto(url.toString(), ImageView.ScaleType.MATRIX);
    }

    @Override
    public PostFormImageItem.ViewHolder getViewHolder(View v) {
        return new PostFormImageItem.ViewHolder(v);
    }

    public Uri getUrl() {
        return url;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageview_preview)
        CropTopImageView previewImageView;
        @BindView(R.id.textview_remove)
        public TextView removeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
