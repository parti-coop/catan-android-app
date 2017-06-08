package xyz.parti.catan.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.holder.ColorHolder;
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
    private String logoUrl;
    private boolean unreadMarked = false;

    private PostFeedDrawerItem(int layoutRes, int type) {
        this.layoutRes = layoutRes;
        this.type = type;
    }

    public static PostFeedDrawerItem forParti() {
        return new PostFeedDrawerItem(R.layout.drawer_item_parti, R.id.drawer_item_parti_post_feed);
    }

    public static PostFeedDrawerItem forPostFeed() {
        return new PostFeedDrawerItem(R.layout.drawer_item_post_feed, R.id.drawer_item_dashboard);
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

        Context ctx = viewHolder.itemView.getContext();
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
            partiViewHolder.name.setTextColor(DrawerUIUtils.getTextColorStateList(ContextCompat.getColor(ctx, (R.color.material_drawer_primary_text_unread)), ContextCompat.getColor(ctx, (R.color.material_drawer_selected_text))));
        } else {
            partiViewHolder.name.setPaintFlags(partiViewHolder.name.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            //set the colors for textViews
            partiViewHolder.name.setTextColor(selectedTextColor);
        }

        if(logoUrl != null) {
            partiViewHolder.itemLogoImageView.setImageDrawable(null);
            new ImageHelper(partiViewHolder.itemLogoImageView).loadInto(logoUrl, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        }

        //for android API 17 --> Padding not applied via xml
        DrawerUIUtils.setDrawerVerticalPadding(partiViewHolder.view, level);
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
        return type;
    }

    static class PartiViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageview_item_logo)
        ImageView itemLogoImageView;
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