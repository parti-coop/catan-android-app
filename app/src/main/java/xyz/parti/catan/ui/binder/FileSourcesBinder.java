package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.ClosableClickableList;

/**
 * Created by dalikim on 2017. 4. 4..
 */

public class FileSourcesBinder {
    private final static int HALF_IMAGE_SPACE = 5;
    private Context context;

    @BindView(R.id.layout_flies_docs)
    ViewGroup docsLayout;
    @BindView(R.id.layout_files_images)
    LinearLayout imagesLayout;

    private ClosableClickableList closableClickableList = new ClosableClickableList();

    public FileSourcesBinder(ViewGroup view) {
        this.context = view.getContext().getApplicationContext();
        LayoutInflater.from(context).inflate(R.layout.references_file_sources, view);
        ButterKnife.bind(this, view);
    }

    public void bind(PostBinder.PostBindablePresenter presenter, Post post) {
        closableClickableList.clear();

        imagesLayout.removeAllViews();
        docsLayout.removeAllViews();

        drawImageFileSources(presenter, post.getImageFileSources(), post);
        drawDocFileSources(presenter, post.getDocFileSources(), post);

        if(post.getImageFileSources().size() > 0) {
            imagesLayout.setVisibility(View.VISIBLE);
        } else {
            imagesLayout.setVisibility(View.GONE);
        }
        if(post.getDocFileSources().size() > 0) {
            docsLayout.setVisibility(View.VISIBLE);
        } else {
            docsLayout.setVisibility(View.GONE);
        }
    }

    private void drawImageFileSources(final PostBinder.PostBindablePresenter presenter, List<FileSource> imageFileSources, final Post post) {
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
                android.view.View imageView = makeImageCell(context, fileSource.attachment_md_url, imageFileSourcesRow.size(), col);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.onClickImageFileSource(post);
                    }
                });
                closableClickableList.add(imageView);
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

    private android.view.View makeImageCell(Context context, String md_url, int col_size, int current_col) {
        SimpleDraweeView draweeView = new SimpleDraweeView(context);
        if(col_size <= 1) {
            draweeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            draweeView.setAspectRatio(0.8f); // w / h
            draweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            draweeView.setImageURI(md_url);
        } else {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 300);
            layoutParams.weight = 1;
            draweeView.setLayoutParams(layoutParams);
            draweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FOCUS_CROP);
            draweeView.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0f));
            draweeView.setImageURI(md_url);
        }
        draweeView.getHierarchy().setFailureImage(R.drawable.ic_image_brand_gray, ScalingUtils.ScaleType.CENTER_INSIDE);

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
        rowBgLayout.addView(draweeView);

        return rowBgLayout;
    }

    private void drawDocFileSources(final PostBinder.PostBindablePresenter presenter, List<FileSource> docFileSources, final Post post) {
        for(final FileSource docFileSource: docFileSources) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout fileSourcesLayout = (LinearLayout) inflater.inflate(R.layout.references_doc_file_source, docsLayout, false);
            new DocFileSourceHolder(fileSourcesLayout).bindData(docFileSource);
            docsLayout.addView(fileSourcesLayout);

            fileSourcesLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickDocFileSource(post, docFileSource);
                }
            });
            closableClickableList.add(fileSourcesLayout);
        }
    }

    public void setVisibility(int visibility) {
        this.imagesLayout.setVisibility(visibility);
        this.docsLayout.setVisibility(visibility);
    }

    public void unbind() {
        closableClickableList.clear();
    }

    static class DocFileSourceHolder {
        @BindView(R.id.textview_file_name)
        TextView nameTextView;
        @BindView(R.id.textview_file_size)
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
