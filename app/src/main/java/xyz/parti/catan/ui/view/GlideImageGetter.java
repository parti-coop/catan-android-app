package xyz.parti.catan.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import xyz.parti.catan.Constants;
import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 5. 19..
 */

public class GlideImageGetter implements Html.ImageGetter, View.OnAttachStateChangeListener, Drawable.Callback {

    private final Context mContext;

    private final TextView mTextView;

    /**
     * Weak {@link java.util.HashSet}.
     */
    private final Set<ViewTarget> mViewTargetSet = Collections.newSetFromMap(new WeakHashMap<>());

    public GlideImageGetter(Context context, TextView textView) {
        this.mContext = context;
        this.mTextView = textView;

        // save Drawable.Callback in TextView
        // and get back when finish fetching image
        // see https://github.com/goofyz/testGlide/pull/1 for more details
        mTextView.setTag(R.id.drawable_callback_tag, this);
        // add this listener in order to clean any pending images loading
        // and set drawable callback tag to null when detached from window
        mTextView.addOnAttachStateChangeListener(this);
    }

    /**
     * We display image depends on settings and Wi-Fi status,
     * but display emoticons at any time.
     */
    @Override
    public Drawable getDrawable(String url) {
        Log.d(Constants.TAG_TEST, "XXXXXXX");
        Log.d(Constants.TAG_TEST, url);
        UrlDrawable urlDrawable = new UrlDrawable();

        ImageGetterViewTarget imageGetterViewTarget = new ImageGetterViewTarget(mTextView,
                urlDrawable);
        Glide.with(mContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageGetterViewTarget);

        mViewTargetSet.add(imageGetterViewTarget);
        return urlDrawable;
    }

    @Override
    public void onViewAttachedToWindow(View v) {}

    @Override
    public void onViewDetachedFromWindow(View v) {
        // cancels any pending images loading
        for (ViewTarget viewTarget : mViewTargetSet) {
            Glide.clear(viewTarget);
        }
        mViewTargetSet.clear();
        v.removeOnAttachStateChangeListener(this);

        v.setTag(R.id.drawable_callback_tag, null);
    }

    /**
     * Implements {@link Drawable.Callback} in order to
     * redraw the TextView which contains the animated GIFs.
     */
    @Override
    public void invalidateDrawable(Drawable who) {
        mTextView.invalidate();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {}

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {}

    private static final class ImageGetterViewTarget extends ViewTarget<TextView, GlideDrawable> {

        private final UrlDrawable mDrawable;

        private Request mRequest;

        private ImageGetterViewTarget(TextView view, UrlDrawable drawable) {
            super(view);

            this.mDrawable = drawable;
        }

        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            // resize this drawable's width & height to fit its container
            final int resWidth = resource.getIntrinsicWidth();
            final int resHeight = resource.getIntrinsicHeight();
            int width, height;
            TextView textView = getView();
            if (textView.getWidth() >= resWidth) {
                width = resWidth;
                height = resHeight;
            } else {
                width = textView.getWidth();
                height = (int) (resHeight / ((float) resWidth / width));
            }

            Rect rect = new Rect(0, 0, width, height);
            resource.setBounds(rect);
            mDrawable.setBounds(rect);
            mDrawable.setDrawable(resource);

            if (resource.isAnimated()) {
                Drawable.Callback callback = (Drawable.Callback) textView.getTag(
                        R.id.drawable_callback_tag);
                // note: not sure whether callback would be null sometimes
                // when this Drawable' host view is detached from View
                if (callback != null) {
                    // set callback to drawable in order to
                    // signal its container to be redrawn
                    // to show the animated GIF
                    mDrawable.setCallback(callback);
                    resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
                    resource.start();
                }
            } else {
                textView.setTag(R.id.drawable_callback_tag, null);
            }

            // see http://stackoverflow.com/questions/7870312/android-imagegetter-images-overlapping-text#comment-22289166
            textView.setText(textView.getText());
        }

        /**
         * See https://github.com/bumptech/glide/issues/550#issuecomment-123693051
         *
         * @see com.bumptech.glide.GenericRequestBuilder#into(com.bumptech.glide.request.target.Target)
         */
        @Override
        public Request getRequest() {
            return mRequest;
        }

        @Override
        public void setRequest(Request request) {
            this.mRequest = request;
        }
    }
}
