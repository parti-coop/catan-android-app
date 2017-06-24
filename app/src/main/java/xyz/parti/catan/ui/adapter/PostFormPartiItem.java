package xyz.parti.catan.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Parti;

/**
 * Created by dalikim on 2017. 5. 24..
 */

public class PostFormPartiItem extends AbstractItem<PostFormPartiItem, PostFormPartiItem.ViewHolder> {
    private Parti parti;

    public PostFormPartiItem(Parti parti) {
        this.parti = parti;
    }

    //The unique ID for this type of item
    @Override
    public int getType() {
        return R.id.post_form_parti;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.post_form_parti;
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        //call super so the selection is already handled for you
        super.bindView(viewHolder, payloads);

        //bind our data
        //set the text for the name
        viewHolder.title.setText(parti.title);
        //set the text for the description or hide
        viewHolder.logo.setImageURI(parti.logo_url);
    }

    //reset the view here (this is an optional method, but recommended)
    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    public Parti getParti() {
        return parti;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textview_parti_title)
        TextView title;
        @BindView(R.id.draweeview_parti_logo)
        SimpleDraweeView logo;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
