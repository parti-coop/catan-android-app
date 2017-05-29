package xyz.parti.catan.ui.view;

import android.content.Context;
import android.graphics.Point;

import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.UncapableCause;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;

import java.util.HashSet;
import java.util.Set;

import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 5. 25..
 */

public class SizeFilter extends Filter {
    private int mMaxSize;

    public SizeFilter(int maxSizeInBytes) {
        mMaxSize = maxSizeInBytes;
    }

    @Override
    public Set<MimeType> constraintTypes() {
        return new HashSet<MimeType>() {{
            add(MimeType.GIF);
            add(MimeType.PNG);
            add(MimeType.JPEG);
        }};
    }

    @Override
    public UncapableCause filter(Context context, Item item) {
        if (!needFiltering(context, item))
            return null;

        if (item.size > mMaxSize) {
            return new UncapableCause(UncapableCause.DIALOG, context.getString(R.string.error_gif, String.valueOf(PhotoMetadataUtils.getSizeInMB(mMaxSize))));
        }
        return null;
    }
}
