package xyz.parti.catan.helper;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import xyz.parti.catan.Constants;
import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 4. 5..
 */

public class ImageHelper {
    public static Target<GlideDrawable> loadInto(ImageView imageView, String url) {
        return loadInto(imageView, url, ImageView.ScaleType.CENTER_CROP);
    }

    public static Target<GlideDrawable> loadInto(ImageView imageView, String url, ImageView.ScaleType scaleType) {
        Context context = imageView.getContext();

        return Glide.with(context)
                .load(url)
                .crossFade()
                .error(R.drawable.ic_image_brand_gray)
                .into(new ScaleImageViewTarget(imageView, scaleType, ImageView.ScaleType.CENTER_INSIDE));
    }

    public static Target<GlideDrawable> loadInto(ImageView imageView, String url, ImageView.ScaleType successScaleType, ImageView.ScaleType errorScaleType) {
        Context context = imageView.getContext();

        return Glide.with(context)
                .load(url)
                .crossFade()
                .error(R.drawable.error)
                .into(new ScaleImageViewTarget(imageView, successScaleType, errorScaleType));
    }
}
