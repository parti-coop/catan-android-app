package xyz.parti.catan.ui.binder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.LinkSource;

/**
 * Created by dalikim on 2017. 4. 5..
 */

public class LinkSourceBinder {
    @BindView(R.id.layout_references_link_source)
    LinearLayout referencesLinkSourceLayout;
    @BindView(R.id.textview_link_title)
    TextView titleTextView;
    @BindView(R.id.textview_link_body)
    TextView bodyTextView;
    @BindView(R.id.textview_link_site_name)
    TextView siteNameTextView;
    @BindView(R.id.layout_link_image)
    FrameLayout imageLayout;
    @BindView(R.id.draweeview_link_image)
    SimpleDraweeView imageSimpleDraweeView;
    @BindView(R.id.textview_link_video_sign)
    ImageView videoSignTextView;

    public LinkSourceBinder(ViewGroup view) {
        LayoutInflater.from(view.getContext()).inflate(R.layout.references_link_source, view);
        ButterKnife.bind(this, view);
    }

    public void bind(LinkSource linkSource) {
        unbind();
        if(linkSource.title_or_url != null) {
            titleTextView.setText(linkSource.title_or_url);
            titleTextView.setVisibility(View.VISIBLE);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
        if(linkSource.body != null && !TextUtils.isEmpty(linkSource.body)) {
            bodyTextView.setText(linkSource.body);
            bodyTextView.setVisibility(View.VISIBLE);
        } else {
            bodyTextView.setVisibility(View.GONE);
        }
        if(linkSource.site_name != null && !TextUtils.isEmpty(linkSource.site_name)) {
            siteNameTextView.setText(linkSource.site_name);
        } else {
            siteNameTextView.setText(R.string.references_link_source_site_name_fallback);
        }
        siteNameTextView.setVisibility(View.VISIBLE);
        if(linkSource.image_url != null) {
            imageSimpleDraweeView.setImageURI(linkSource.image_url);
            if(linkSource.is_video) {
                videoSignTextView.setVisibility(View.VISIBLE);
            } else {
                videoSignTextView.setVisibility(View.GONE);
            }
            imageLayout.setVisibility(View.VISIBLE);
        } else {
            imageLayout.setVisibility(View.GONE);
        }
    }

    public View getRootView() {
        return referencesLinkSourceLayout;
    }

    public void setVisibility(int visibility) {
        getRootView().setVisibility(visibility);
    }

    public void unbind() {
        /* ignored */
    }
}
