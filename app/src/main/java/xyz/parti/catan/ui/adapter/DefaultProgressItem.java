package xyz.parti.catan.ui.adapter;

import com.mikepenz.fastadapter_extensions.items.ProgressItem;

import xyz.parti.catan.R;


public class DefaultProgressItem extends ProgressItem {
    @Override
    public int getType() {
        return R.id.default_progress_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.default_progress_item;
    }
}
