package xyz.parti.catan.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;


public class MatchParentWidthImageView extends android.support.v7.widget.AppCompatImageView {
    public MatchParentWidthImageView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Drawable d = this.getDrawable();

        if (d == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = (int) Math.ceil(width * (float) d.getIntrinsicHeight() / d.getIntrinsicWidth());
        this.setMeasuredDimension(width, height);
    }
}
