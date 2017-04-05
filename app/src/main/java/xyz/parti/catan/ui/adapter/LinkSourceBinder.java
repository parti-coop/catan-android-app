package xyz.parti.catan.ui.adapter;

import android.content.Context;
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
    @BindView(R.id.referenceLinkSourceTitle)
    TextView referenceLinkSourceTitle;
    @BindView(R.id.referenceLinkSourceBody)
    TextView referenceLinkSourceBody;
    @BindView(R.id.referenceLinkSourceSiteName)
    TextView referenceLinkSourceSiteName;
    @BindView(R.id.referenceLinkSourceImage)
    ImageView referenceLinkSourceImage;

    private final Context context;

    public LinkSourceBinder(ViewGroup view) {
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    public void bindData(LinkSource linkSource) {
        if(linkSource.title != null) {
            referenceLinkSourceTitle.setText(linkSource.title);
            referenceLinkSourceTitle.setVisibility(View.VISIBLE);
        }
        if(linkSource.body != null) {
            referenceLinkSourceBody.setText(linkSource.body);
            referenceLinkSourceBody.setVisibility(View.VISIBLE);
        }
        if(linkSource.site_name != null) {
            referenceLinkSourceSiteName.setText(linkSource.site_name);
            referenceLinkSourceSiteName.setVisibility(View.VISIBLE);
        }
        if(linkSource.image_url != null) {
            ImageHelper.loadInto(referenceLinkSourceImage, linkSource.image_url);
            referenceLinkSourceImage.setVisibility(View.VISIBLE);
        }
    }
}
