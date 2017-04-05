package xyz.parti.catan.helper;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
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
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Context context = imageView.getContext();

        //AnimationDrawable is required when you are using transition drawables
        //You can directly send resource id to glide if your placeholder is static
        //However if you are using GIFs, it is better to create a transition drawable in xml
        //& use it as shown in this example
        AnimationDrawable animationDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            animationDrawable=(AnimationDrawable)context.getDrawable(R.drawable.progress_animation);
        else
            animationDrawable=(AnimationDrawable)context.getResources().getDrawable(R.drawable.progress_animation);
        animationDrawable.start();

        return Glide.with(context)
                .load(url)
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.error)
                .listener(new BasicRequestListener(imageView))
                .into(imageView);
    }

    private static class BasicRequestListener implements RequestListener<String, GlideDrawable> {
        private ImageView imageView;

        public BasicRequestListener(ImageView imageView) {
            this.imageView = imageView;
        }
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            Log.e(Constants.TAG, e.getMessage(), e);
            // important to return false so the error placeholder can be placed
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            imageView.setImageResource(0);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return false;
        }
    };
}
