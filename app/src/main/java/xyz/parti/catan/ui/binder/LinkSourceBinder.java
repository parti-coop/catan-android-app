package xyz.parti.catan.ui.binder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.models.LinkSource;

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
    @BindView(R.id.imageview_image)
    ImageView imageImageView;

    public LinkSourceBinder(ViewGroup view) {
        ButterKnife.bind(this, view);
    }

    public void bindData(LinkSource linkSource) {
        if(linkSource.title != null) {
            titleTextView.setText(linkSource.title);
            titleTextView.setVisibility(View.VISIBLE);
        }
        if(linkSource.body != null) {
            bodyTextView.setText(linkSource.body);
            bodyTextView.setVisibility(View.VISIBLE);
        }
        if(linkSource.site_name != null) {
            siteNameTextView.setText(linkSource.site_name);
            siteNameTextView.setVisibility(View.VISIBLE);
        }
        if(linkSource.image_url != null) {
            ImageHelper.loadInto(imageImageView, linkSource.image_url);
            imageImageView.setVisibility(View.VISIBLE);
        }
    }
}
