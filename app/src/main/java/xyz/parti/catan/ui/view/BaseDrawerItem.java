package xyz.parti.catan.ui.view;

import android.view.View;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.model.AbstractBadgeableDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;

/**
 * Created by dalikim on 2017. 6. 6..
 */

public class BaseDrawerItem extends AbstractBadgeableDrawerItem<BaseDrawerItem> {
    private final int layoutRes;
    private final int type;
    private String logoUrl;

    private BaseDrawerItem(int layoutRes, int type) {
        this.layoutRes = layoutRes;
        this.type = type;
    }

    public static BaseDrawerItem forParti() {
        return new BaseDrawerItem(R.layout.drawer_item_parti, R.id.drawer_item_parti_post_feed);
    }

    public static BaseDrawerItem forPostFeed() {
        return new BaseDrawerItem(R.layout.drawer_item_post_feed, R.id.drawer_item_my_post_feed);
    }

    public BaseDrawerItem withLogo(String url) {
        this.logoUrl = url;
        return this;
    }

    @Override
    protected void bindViewHelper(BaseViewHolder viewHolder) {
        super.bindViewHelper(viewHolder);
        PartiViewHolder partiViewHolder = (PartiViewHolder) viewHolder;
        if(logoUrl != null) {
            partiViewHolder.itemLogoImageView.setImageDrawable(null);
            new ImageHelper(partiViewHolder.itemLogoImageView).loadInto(logoUrl, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        }
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new PartiViewHolder(v);
    }

    @Override
    public int getLayoutRes() {
        return layoutRes;
    }

    @Override
    public int getType() {
        return type;
    }

    static class PartiViewHolder extends ViewHolder {
        @BindView(R.id.imageview_item_logo)
        ImageView itemLogoImageView;

        protected View view;

        PartiViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
        }
    }


}