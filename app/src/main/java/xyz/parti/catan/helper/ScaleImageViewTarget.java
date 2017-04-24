package xyz.parti.catan.helper;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

/**
 * Created by dalikim on 2017. 4. 20..
 */

public class ScaleImageViewTarget extends GlideDrawableImageViewTarget
{
    ImageView.ScaleType successScaleType;
    ImageView.ScaleType errorScaleType;

    public ScaleImageViewTarget(ImageView view, ImageView.ScaleType successScaleType, ImageView.ScaleType errorScaleType) {
        super(view);
        this.successScaleType = successScaleType;
        this.errorScaleType = errorScaleType;
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        ImageView imageView = getView();
        imageView.setScaleType(errorScaleType);
        super.onLoadFailed(e, errorDrawable);
    }

    @Override
    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
        ImageView imageView = getView();
        imageView.setScaleType(successScaleType);
        super.onResourceReady(resource, animation);
    }
}