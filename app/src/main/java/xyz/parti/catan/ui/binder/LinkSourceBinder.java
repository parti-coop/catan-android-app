package xyz.parti.catan.ui.binder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.LinkSource;
import xyz.parti.catan.helper.ImageHelper;

/**
 * Created by dalikim on 2017. 4. 5..
 */

public class LinkSourceBinder {
    @BindView(R.id.textview_title)
    TextView titleTextView;
    @BindView(R.id.textview_body)
    TextView bodyTextView;
    @BindView(R.id.textview_site_name)
    TextView siteNameTextView;
    @BindView(R.id.layout_image)
    RelativeLayout imageLayout;
    @BindView(R.id.imageview_image)
    ImageView imageImageView;
    @BindView(R.id.textview_video_sign)
    IconTextView videoSignTextView;

    public LinkSourceBinder(ViewGroup view) {
        ButterKnife.bind(this, view);
    }

    public void bindData(LinkSource linkSource) {
        if(linkSource.title_or_url != null) {
            titleTextView.setText(linkSource.title_or_url);
            titleTextView.setVisibility(View.VISIBLE);
        }
        if(linkSource.body != null) {
            bodyTextView.setText(linkSource.body);
            bodyTextView.setVisibility(View.VISIBLE);
        }
        if(linkSource.site_name != null) {
            siteNameTextView.setText(linkSource.site_name);
        } else {
            siteNameTextView.setText(R.string.references_link_source_site_name_fallback);
        }
        siteNameTextView.setVisibility(View.VISIBLE);
        if(linkSource.image_url != null) {
            new ImageHelper(imageImageView).loadInto(linkSource.image_url);
            if(linkSource.is_video) {
                videoSignTextView.setVisibility(View.VISIBLE);
            } else {
                videoSignTextView.setVisibility(View.GONE);
            }
            imageLayout.setVisibility(View.VISIBLE);
        }
    }
}
