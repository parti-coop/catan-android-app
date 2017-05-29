package xyz.parti.catan.ui.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import java.io.Serializable;

/**
 * Created by dalikim on 2017. 5. 12..
 */

public class CropTopImageView extends android.support.v7.widget.AppCompatImageView implements Serializable {
    public CropTopImageView(Context context) {
        super(context);
    }

    public CropTopImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropTopImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        recomputeImgMatrix();
        return super.setFrame(l, t, r, b);
    }

    private void recomputeImgMatrix() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final Matrix matrix = getImageMatrix();

        float scale;
        final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            scale = (float) viewHeight / (float) drawableHeight;
        } else {
            scale = (float) viewWidth / (float) drawableWidth;
        }

        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
    }
}
