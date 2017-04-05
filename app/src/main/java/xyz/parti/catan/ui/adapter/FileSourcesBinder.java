package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.models.FileSource;

/**
 * Created by dalikim on 2017. 4. 4..
 */

public class FileSourcesBinder {
    final static int HALF_IMAGE_SPACE = 5;
    private final Context context;

    @BindView(R.id.referencesDocsLayout)
    ViewGroup referencesDocsLayout;
    @BindView(R.id.referencesImagesLayout)
    ViewGroup referencesImagesLayout;

    public FileSourcesBinder(ViewGroup view) {
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    public void bindData(FileSource[] fileSources) {
        referencesImagesLayout.removeAllViews();
        referencesDocsLayout.removeAllViews();

        List<FileSource> imageFileSources = new ArrayList();
        List<FileSource> docFileSources = new ArrayList();
        for(FileSource fileSource: fileSources) {
            if(fileSource.isImage()) {
                imageFileSources.add(fileSource);
            }
            if(fileSource.isDoc()) {
                docFileSources.add(fileSource);
            }
        }

        drawImageFileSources(imageFileSources);
        drawDocFileSources(docFileSources);
    }

    private void drawImageFileSources(List<FileSource> imageFileSources) {
        int row = 0;
        for(List<FileSource> imageFileSourcesRow: splitImageFileSources(imageFileSources)) {
            LinearLayout rowLayout = new LinearLayout(context);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if(row != 0) {
                rowLayout.setBackgroundResource(R.color.dashboard_image_border_color);
                rowLayout.setPadding(0, HALF_IMAGE_SPACE * 2, 0, 0);
            }
            rowLayout.setLayoutParams(layoutParams);

            int col = 0;
            for(FileSource fileSource: imageFileSourcesRow) {
                View imageView = makeImageView(context, fileSource.attachment_url, imageFileSourcesRow.size(), col);
                if (imageView != null) {
                    rowLayout.addView(imageView);
                }
                col++;
            }

            referencesImagesLayout.addView(rowLayout);

            row++;
        }
    }

    private List<List<FileSource>> splitImageFileSources(List<FileSource> imageFileSources) {
        List<List<FileSource>> imageFileSourcesRows = new ArrayList<>();
        if(imageFileSources.size() <= 2) {
            imageFileSourcesRows.add(imageFileSources);
        } else {
            int index = 0;
            boolean nextRow = true;
            List<FileSource> currentRow = null;
            for(FileSource fileSource: imageFileSources) {
                if(nextRow) {
                    currentRow = new ArrayList();
                    imageFileSourcesRows.add(currentRow);
                }
                nextRow = false;

                int remainCount = imageFileSources.size() - index;
                if(index == 0) {
                    nextRow = true;
                } else if(remainCount <= 2) {
                } else {
                    int previousCount = imageFileSourcesRows.get(imageFileSourcesRows.size() - 2).size();
                    if(previousCount == 3) {
                        if(currentRow.size() >= 2) {
                            nextRow = true;
                        }
                    } else {
                        if(currentRow.size() >= 3) {
                            nextRow = true;
                        }
                    }
                }
                currentRow.add(fileSource);
                index++;
            }
        }
        return imageFileSourcesRows;
    }

    private View makeImageView(Context context, String url, int col_size, int current_col) {
        final ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        imageView.setLayoutParams(params);

        Glide.with(context)
                .load(url)
                .listener(requestListener)
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.error)
                .crossFade().into(imageView);

        LinearLayout rowBgLayout = new LinearLayout(context);
        rowBgLayout.setBackgroundColor(context.getResources().getColor(R.color.dashboard_image_border_color));
        LinearLayout.LayoutParams bgLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
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

    private RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            Log.e(Constants.TAG, e.getMessage(), e);

            // important to return false so the error placeholder can be placed
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    private void drawDocFileSources(List<FileSource> docFileSources) {
        for(FileSource docFileSource: docFileSources) {
            LayoutInflater inflater = LayoutInflater.from(context);
            CardView fileSourcesLayout = (CardView) inflater.inflate(R.layout.references_doc_file_source, referencesDocsLayout, false);
            new DocFileSourceHolder(fileSourcesLayout).bindData(docFileSource);
            referencesDocsLayout.addView(fileSourcesLayout);
        }
    }

    static public class DocFileSourceHolder {
        @BindView(R.id.referencesDocFileSourceName)
        TextView referencesDocFileSourceName;
        @BindView(R.id.referencesDocFileSourceSize)
        TextView referencesDocFileSourceSize;

        public DocFileSourceHolder(ViewGroup view) {
            ButterKnife.bind(this, view);
        }

        public void bindData(FileSource docFileSource) {
            referencesDocFileSourceName.setText(docFileSource.name);
            referencesDocFileSourceSize.setText(docFileSource.human_file_size);
        }
    }
}
