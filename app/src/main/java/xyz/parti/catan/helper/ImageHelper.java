package xyz.parti.catan.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

import xyz.parti.catan.Constants;
import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 4. 5..
 */

public class ImageHelper {
    private ImageView imageView;

    public ImageHelper(ImageView imageView) {
        this.imageView = imageView;
    }

    public Target<GlideDrawable> loadInto(String url) {
        return loadInto(url, ImageView.ScaleType.CENTER_CROP);
    }

    public Target<GlideDrawable> loadInto(String url, ImageView.ScaleType scaleType) {
        return loadInto(url, scaleType, ImageView.ScaleType.CENTER_INSIDE);
    }

    public Target<GlideDrawable> loadInto(String url, ImageView.ScaleType successScaleType, ImageView.ScaleType errorScaleType) {
        if(url == null) return null;

        Context context = imageView.getContext();

        if(url.startsWith("data:image/png;base64,")) {
            Log.d(Constants.TAG_TEST, "base64");
            url = url.replace("data:image/png;base64,","");
            byte[] imageByteArray = Base64.decode(url, Base64.DEFAULT);

            return Glide.with(context)
                    .load(imageByteArray)
                    .crossFade()
                    .error(R.drawable.ic_image_brand_gray)
                    .into(new ScaleImageViewTarget(imageView, successScaleType, errorScaleType));
        } else {
            return Glide.with(context)
                    .load(url)
                    .crossFade()
                    .error(R.drawable.ic_image_brand_gray)
                    .into(new ScaleImageViewTarget(imageView, successScaleType, errorScaleType));
        }
    }

    /**
     * Created by dalikim on 2017. 4. 20..
     */

    private class ScaleImageViewTarget extends GlideDrawableImageViewTarget
    {
        ImageView.ScaleType successScaleType;
        ImageView.ScaleType errorScaleType;

        ScaleImageViewTarget(ImageView view, ImageView.ScaleType successScaleType, ImageView.ScaleType errorScaleType) {
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
}
