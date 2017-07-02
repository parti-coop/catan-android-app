package xyz.parti.catan.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;


public class ShimmerLayout extends FrameLayout {
    public ShimmerLayout(Context context) {
        super(context);
    }

    public ShimmerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ShimmerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (VISIBLE == visibility) {
            startLoading();
        } else {
            stopLoading();
        }
    }

    public void stopLoading() {
        for(ShimmerLoaderView v : getAllShimmerChildren(this)) {
            v.stopLoading();
        }
    }

    public void startLoading() {
        for(ShimmerLoaderView v : getAllShimmerChildren(this)) {
            v.startLoading();
        }
    }

    private ArrayList<ShimmerLoaderView> getAllShimmerChildren(View v) {

        if (v instanceof ShimmerLoaderView) {
            ArrayList<ShimmerLoaderView> viewArrayList = new ArrayList<>();
            viewArrayList.add((ShimmerLoaderView) v);
            return viewArrayList;
        }

        ArrayList<ShimmerLoaderView> result = new ArrayList<>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<ShimmerLoaderView> viewArrayList = new ArrayList<>();
            viewArrayList.addAll(getAllShimmerChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }
}
