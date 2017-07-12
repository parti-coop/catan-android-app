package xyz.parti.catan.ui.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.ui.view.LooselyRelativeTimeTextView;
import xyz.parti.catan.ui.view.SmartTextView;

public class WikiBinder {
    @BindView(R.id.layout_references_wiki)
    LinearLayout referencesWikiLayout;
    @BindView(R.id.layout_wiki_content)
    LinearLayout wikiContentLayout;
    @BindView(R.id.textview_wiki_title)
    TextView wikiTitleTextView;
    @BindView(R.id.draweeview_wiki_thumbnail)
    SimpleDraweeView wikiThumbnailDraweeView;
    @BindView(R.id.layout_wiki_authors_wrapper)
    LinearLayout wikiAuthorsWrapperLayout;
    @BindView(R.id.layout_wiki_authors)
    LinearLayout wikiAuthorsLayout;
    @BindView(R.id.layout_wiki_latest_activity)
    LinearLayout wikiLatestActivityLayout;
    @BindView(R.id.smarttextview_wiki_latest_activity_body)
    SmartTextView wikiLatestActivityBodySmartTextView;
    @BindView(R.id.textview_wiki_latest_activity_at)
    LooselyRelativeTimeTextView wikiLatestActivityAtTextView;

    public WikiBinder(ViewGroup view) {
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        inflater.inflate(R.layout.references_wiki, view);
        ButterKnife.bind(this, view);
    }

    public void bind(final PostBinder.PostBindablePresenter presenter, final Post post) {
        unbind();

        wikiTitleTextView.setText(post.wiki.title);
        wikiThumbnailDraweeView.setAspectRatio(post.wiki.image_ratio);
        wikiThumbnailDraweeView.setImageURI(post.wiki.thumbnail_md_url);

        if(post.wiki.authors != null && post.wiki.authors.length > 0) {
            wikiAuthorsLayout.removeAllViews();
            for (User user : post.wiki.authors) {
                bindAuthor(user);
            }
            wikiAuthorsWrapperLayout.setVisibility(View.VISIBLE);
        } else {
            wikiAuthorsWrapperLayout.setVisibility(View.GONE);
        }

        if(post.wiki.latest_activity_body != null && post.wiki.latest_activity_at != null) {
            wikiLatestActivityBodySmartTextView.setNoImageRichText(post.wiki.latest_activity_body);
            wikiLatestActivityAtTextView.setReferenceTime(post.wiki.latest_activity_at.getTime());
            wikiLatestActivityLayout.setVisibility(View.VISIBLE);
        } else {
            wikiLatestActivityLayout.setVisibility(View.GONE);
        }

        wikiContentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickWikiContent(post);
            }
        });
    }

    private void bindAuthor(User user) {
        SimpleDraweeView imageView = new SimpleDraweeView(wikiAuthorsLayout.getContext());

        int size = wikiAuthorsLayout.getContext().getResources().getDimensionPixelSize(R.dimen.wiki_author_user_size);
        int margin = wikiAuthorsLayout.getContext().getResources().getDimensionPixelSize(R.dimen.wiki_author_user_margin);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.setMargins(margin, 0, margin, 0);
        imageView.setLayoutParams(layoutParams);
        imageView.getHierarchy().setFailureImage(R.drawable.ic_account_circle_gray_24dp);

        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        imageView.getHierarchy().setRoundingParams(roundingParams);

        imageView.setImageURI(user.image_url);
        wikiAuthorsLayout.addView(imageView);
    }

    public void unbind() {
        if(wikiContentLayout != null) wikiContentLayout.setOnClickListener(null);
    }

    public void setVisibility(int visibility) {
        referencesWikiLayout.setVisibility(visibility);
    }
}
