package xyz.parti.catan.ui.view;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;

import com.github.curioustechizen.ago.RelativeTimeTextView;


public class LooselyRelativeTimeTextView extends RelativeTimeTextView {
    public LooselyRelativeTimeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LooselyRelativeTimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setReferenceTime(long referenceTime) {
        if(System.currentTimeMillis() - referenceTime < DateUtils.MINUTE_IN_MILLIS) {
            super.setReferenceTime(System.currentTimeMillis() - 20);
        } else {
            super.setReferenceTime(referenceTime);
        }
    }
}
