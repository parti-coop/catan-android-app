package xyz.parti.catan.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.TextHelper;
import xyz.parti.catan.ui.view.LooselyRelativeTimeTextView;
import xyz.parti.catan.ui.view.ZoomableDraweeView;


public class PostImagesViewActivity extends BaseActivity {
    Post post;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.textview_post_created_at)
    LooselyRelativeTimeTextView postCreatedAtTextView;
    @BindView(R.id.textview_post_desc)
    TextView postDescTextView;
    @BindView(R.id.textview_post_user_nickname)
    TextView userNicknameTextView;
    private ImageFragmentPagerAdapter imageFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_images_view);
        ButterKnife.bind(PostImagesViewActivity.this);

        if(getIntent() == null) {
            finish();
            return;
        }
        this.post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        if(this.post == null) {
            finish();
            return;
        }

        imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager(), post.getImageFileSources());
        viewPager.setAdapter(imageFragmentPagerAdapter);
        viewPager.setCurrentItem(0);

        this.postDescTextView.setText(TextHelper.converToHtml("<strong>" + this.post.specific_desc_striped_tags + "</strong>"));
        this.postCreatedAtTextView.setReferenceTime(this.post.created_at.getTime());
        this.userNicknameTextView.setText(this.post.user.nickname);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(viewPager != null) {
            viewPager.setAdapter(null);
        }
        if(this.imageFragmentPagerAdapter != null) {
            imageFragmentPagerAdapter = null;
        }

    }

    private class ImageFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private List<FileSource> imageFileSources;

        ImageFragmentPagerAdapter(android.support.v4.app.FragmentManager fm, List<FileSource> imageFileSources) {
            super(fm);
            this.imageFileSources = imageFileSources;
        }

        @Override
        public int getCount() {
            return imageFileSources.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url = imageFileSources.get(position).attachment_lg_url;
            return SwipeFragment.newInstance(url);
        }
    }

    public static class SwipeFragment extends Fragment {
        @BindView(R.id.imageview)
        ZoomableDraweeView imageView;
        private Unbinder unbinder;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup view,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.post_image_item, view, false);
            unbinder = ButterKnife.bind(SwipeFragment.this, swipeView);

            Bundle bundle = getArguments();
            String url = bundle.getString("url");
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(url)
                    .setAutoPlayAnimations(false)
                    .build();
            imageView.setController(controller);
            return swipeView;
        }

        static SwipeFragment newInstance(String url) {
            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }

        @Override public void onDestroyView() {
            super.onDestroyView();
            if(unbinder != null) unbinder.unbind();
        }
    }
}
