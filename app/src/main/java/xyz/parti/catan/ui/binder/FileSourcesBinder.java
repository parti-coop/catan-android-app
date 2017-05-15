package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.ui.view.CropTopImageView;
import xyz.parti.catan.ui.view.MatchParentWidthImageView;

/**
 * Created by dalikim on 2017. 4. 4..
 */

public class FileSourcesBinder {
    private final static int HALF_IMAGE_SPACE = 5;
    private final PostBinder.PostBindablePresenter presenter;
    private final Context context;

    @BindView(R.id.layout_docs)
    ViewGroup docsLayout;
    @BindView(R.id.layout_images)
    ViewGroup imagesLayout;

    public FileSourcesBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    public void bindData(Post post) {
        imagesLayout.removeAllViews();
        docsLayout.removeAllViews();
        drawImageFileSources(post.getImageFileSources(), post);
        drawDocFileSources(post.getDocFileSources(), post);
    }

    private void drawImageFileSources(List<FileSource> imageFileSources, final Post post) {
        int row = 0;
        for(List<FileSource> imageFileSourcesRow: splitImageFileSources(imageFileSources)) {
            LinearLayout rowLayout = new LinearLayout(context);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if(row != 0) {
                rowLayout.setBackgroundResource(R.color.dashboard_image_border);
                rowLayout.setPadding(0, HALF_IMAGE_SPACE * 2, 0, 0);
            }
            rowLayout.setLayoutParams(layoutParams);

            int col = 0;
            for(FileSource fileSource: imageFileSourcesRow) {
                android.view.View imageView = makeImageCell(context, fileSource.attachment_md_url, fileSource.attachment_sm_url, imageFileSourcesRow.size(), col);
                imageView.setOnClickListener(view -> presenter.onClickImageFileSource(post));
                rowLayout.addView(imageView);
                col++;
            }

            imagesLayout.addView(rowLayout);

            row++;
        }
    }

    private List<List<FileSource>> splitImageFileSources(List<FileSource> imageFileSources) {
        final int DEFAULT_COL_COUNT = 2;

        List<List<FileSource>> imageFileSourcesRows = new ArrayList<>();
        if(imageFileSources.size() <= 2) {
            imageFileSourcesRows.add(imageFileSources);
        } else {
            int index = 0;
            boolean nextRow = true;
            List<FileSource> currentRow = null;
            int currentRowNum = 0;
            for(FileSource fileSource: imageFileSources) {
                if(nextRow) {
                    currentRow = new ArrayList<>();
                    imageFileSourcesRows.add(currentRow);
                    currentRowNum++;
                }

                currentRow.add(fileSource);

                nextRow = false;
                if(currentRowNum == 1) {
                    nextRow = true;
                } else {
                    int currentColCount = imageFileSourcesRows.get(currentRowNum - 1).size();
                    if(currentColCount >= DEFAULT_COL_COUNT) {
                        nextRow = true;
                    }
                }

                index++;
                int remainCount = imageFileSources.size() - index;
                if(remainCount <= 1) {
                    nextRow = false;
                }
            }
        }
        return imageFileSourcesRows;
    }

    private android.view.View makeImageCell(Context context, String md_url, String sm_url, int col_size, int current_col) {
        final ImageView imageView = (col_size <= 1 ? new MatchParentWidthImageView(context) : new CropTopImageView(context));
        
        int height = 300;
        if(col_size == 1) {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        if(col_size == 1) {
            new ImageHelper(imageView).loadInto(md_url, ImageView.ScaleType.CENTER_CROP);
        } else {
            new ImageHelper(imageView).loadInto(sm_url, ImageView.ScaleType.MATRIX);
        }

        LinearLayout rowBgLayout = new LinearLayout(context);
        rowBgLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.dashboard_image_border));
        LinearLayout.LayoutParams bgLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        rowBgLayout.setLayoutParams(bgLayoutParams);

        if(col_size > 1) {
            if (current_col == 0) {
                rowBgLayout.setPadding(0, 0, HALF_IMAGE_SPACE, 0);
            } else if (current_col == col_size - 1) {
                rowBgLayout.setPadding(HALF_IMAGE_SPACE, 0, 0, 0);
            } else {
                rowBgLayout.setPadding(HALF_IMAGE_SPACE, 0, HALF_IMAGE_SPACE, 0);
            }
        }
        rowBgLayout.addView(imageView);

        return rowBgLayout;
    }

    private void drawDocFileSources(List<FileSource> docFileSources, final Post post) {
        for(final FileSource docFileSource: docFileSources) {
            LayoutInflater inflater = LayoutInflater.from(context);
            CardView fileSourcesLayout = (CardView) inflater.inflate(R.layout.references_doc_file_source, docsLayout, false);
            new DocFileSourceHolder(fileSourcesLayout).bindData(docFileSource);
            docsLayout.addView(fileSourcesLayout);

            fileSourcesLayout.setOnClickListener(view -> presenter.onClickDocFileSource(post, docFileSource));
        }
    }

    static class DocFileSourceHolder {
        @BindView(R.id.textview_name)
        TextView nameTextView;
        @BindView(R.id.textview_size)
        TextView sizeTextView;

        DocFileSourceHolder(ViewGroup view) {
            ButterKnife.bind(this, view);
        }

        public void bindData(FileSource docFileSource) {
            nameTextView.setText(docFileSource.name);
            sizeTextView.setText(docFileSource.human_file_size);
        }
    }
}
