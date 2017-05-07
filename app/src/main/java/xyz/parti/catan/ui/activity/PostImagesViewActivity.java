package xyz.parti.catan.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.TextHelper;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;

/**
 * Created by dalikim on 2017. 4. 13..
 */

public class PostImagesViewActivity extends BaseActivity {
    Post post;
    private ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.textview_post_created_at)
    RelativeTimeTextView postCreatedAtTextView;
    @BindView(R.id.textview_post_desc)
    TextView postDescTextView;
    @BindView(R.id.textview_user_nickname)
    TextView userNicknameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_images_view);
        ButterKnife.bind(PostImagesViewActivity.this);

        this.post = Parcels.unwrap(getIntent().getParcelableExtra("post"));

        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager(), post.getImageFileSources());
        viewPager.setAdapter(imageFragmentPagerAdapter);

        this.postDescTextView.setText(TextHelper.converToHtml("<strong>" + this.post.specific_desc_striped_tags + "</strong>"));
        this.postCreatedAtTextView.setReferenceTime(this.post.created_at.getTime());
        this.userNicknameTextView.setText(this.post.user.nickname);
    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {
        private final List<FileSource> imageFileSources;

        public ImageFragmentPagerAdapter(FragmentManager fm, List<FileSource> imageFileSources) {
            super(fm);
            this.imageFileSources = imageFileSources;
        }

        @Override
        public int getCount() {
            return imageFileSources.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url = imageFileSources.get(position).attachment_url;
            SwipeFragment fragment = new SwipeFragment();
            return SwipeFragment.newInstance(url);
        }
    }

    public static class SwipeFragment extends Fragment {
        @BindView(R.id.imageview)
        ImageView imageView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup view,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.post_image_item, view, false);
            ButterKnife.bind(SwipeFragment.this, swipeView);

            Bundle bundle = getArguments();
            String url = bundle.getString("attachment_url");
            ImageHelper.loadInto(imageView, url, ImageView.ScaleType.CENTER_INSIDE);
            return swipeView;
        }

        static SwipeFragment newInstance(String url) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("attachment_url", url);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }
    }
}
