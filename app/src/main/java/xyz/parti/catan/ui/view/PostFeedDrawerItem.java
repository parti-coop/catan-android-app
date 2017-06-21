package xyz.parti.catan.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;

/**
 * Created by dalikim on 2017. 6. 6..
 */

public class PostFeedDrawerItem extends BaseDrawerItem<PostFeedDrawerItem, PostFeedDrawerItem.PartiViewHolder> {
    private final int layoutRes;
    private final int type;
    private final boolean isDashboard;
    private String logoUrl;
    private boolean unreadMarked = false;

    private PostFeedDrawerItem(int layoutRes, int type, boolean isDashboard) {
        this.layoutRes = layoutRes;
        this.type = type;
        this.isDashboard = isDashboard;
    }

    public static PostFeedDrawerItem forParti() {
        return new PostFeedDrawerItem(R.layout.drawer_item_post_feed, R.id.drawer_item_parti, false);
    }

    public static PostFeedDrawerItem forDashboard() {
        return new PostFeedDrawerItem(R.layout.drawer_item_post_feed, R.id.drawer_item_dashbord, true);
    }

    public PostFeedDrawerItem withLogo(String url) {
        this.logoUrl = url;
        return this;
    }

    public PostFeedDrawerItem withUnreadMark(boolean unread) {
        this.unreadMarked = unread;
        return this;
    }

    @Override
    public void bindView(PartiViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        //bind the basic view parts
        bindViewHelper(viewHolder);

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView);
    }

    /**
     * a helper method to have the logic for all secondaryDrawerItems only once
     *
     * @param partiViewHolder
     */
    protected void bindViewHelper(PartiViewHolder partiViewHolder) {
        Context ctx = partiViewHolder.view.getContext();

        //set the identifier from the drawerItem here. It can be used to run tests
        partiViewHolder.view.setId(hashCode());

        //set the item selected if it is
        partiViewHolder.view.setSelected(isSelected());

        //set the item enabled if it is
        partiViewHolder.view.setEnabled(isEnabled());

        //
        partiViewHolder.view.setTag(this);

        //get the correct color for the background
        int selectedColor = getSelectedColor(ctx);
        //get the correct color for the text
        int color = getColor(ctx);
        ColorStateList selectedTextColor = getTextColorStateList(color, getSelectedTextColor(ctx));

        //get the correct color for the icon
//        int iconColor = getIconColor(ctx);
//        int selectedIconColor = getSelectedIconColor(ctx);

        //set the background for the item
        UIUtils.setBackground(partiViewHolder.view, UIUtils.getSelectableBackground(ctx, selectedColor, true));
        //set the text for the name
        StringHolder.applyTo(this.getName(), partiViewHolder.name);

        //define the typeface for our textViews
        if (getTypeface() != null) {
            partiViewHolder.name.setTypeface(getTypeface());
        }

        if (unreadMarked) {
            partiViewHolder.name.setPaintFlags(partiViewHolder.name.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            //set the colors for textViews
            partiViewHolder.name.setTextColor(ContextCompat.getColor(ctx, R.color.material_drawer_primary_text_unread));
        } else {
            partiViewHolder.name.setPaintFlags(partiViewHolder.name.getPaintFlags() & (~ Paint.FAKE_BOLD_TEXT_FLAG));
            //set the colors for textViews
            partiViewHolder.name.setTextColor(selectedTextColor);
        }

        if(logoUrl != null) {
            partiViewHolder.itemDashboardLogoImageView.setVisibility(View.GONE);
            partiViewHolder.itemPartiLogoImageView.setVisibility(View.GONE);

            ImageView logoImageView = null;
            if (getType() == R.id.drawer_item_dashbord) {
                logoImageView = partiViewHolder.itemDashboardLogoImageView;
            } else if (getType() == R.id.drawer_item_parti) {
                logoImageView = partiViewHolder.itemPartiLogoImageView;
            }

            if (logoImageView != null) {
                logoImageView.setVisibility(View.VISIBLE);
                logoImageView.setImageDrawable(null);
                new ImageHelper(logoImageView).loadInto(logoUrl, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
            }
        }

        //for android API 17 --> Padding not applied via xml
        DrawerUIUtils.setDrawerVerticalPadding(partiViewHolder.view, level);
        if(getType() == R.id.drawer_item_dashbord) {
            setDashboardDrawerMarginTop(partiViewHolder.itemView);
        }
    }

    public static void setDashboardDrawerMarginTop(View v) {
        int topMargin = v.getContext().getResources().getDimensionPixelSize(R.dimen.drawer_item_dashboard_margin_top);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) v.getLayoutParams();
        params.topMargin = topMargin;
        v.setLayoutParams(params);
    }

    @Override
    public PartiViewHolder getViewHolder(View v) {
        return new PartiViewHolder(v);
    }

    @Override
    public int getLayoutRes() {
        return layoutRes;
    }

    @Override
    public int getType() {
        return this.type;
    }

    public boolean isUnreadMarked() {
        return unreadMarked;
    }

    static class PartiViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageview_item_parti_logo)
        ImageView itemPartiLogoImageView;
        @BindView(R.id.imageview_item_dashboard_logo)
        ImageView itemDashboardLogoImageView;
        @BindView(R.id.material_drawer_name)
        TextView name;

        protected View view;

        PartiViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
        }
    }


}