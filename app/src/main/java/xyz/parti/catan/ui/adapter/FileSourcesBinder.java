package xyz.parti.catan.ui.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.models.FileSource;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.activity.PostImagesViewActivity;

/**
 * Created by dalikim on 2017. 4. 4..
 */

public class FileSourcesBinder {
    final static String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    final static int HALF_IMAGE_SPACE = 5;
    private final ProgressDialog downloadProgressDialog;
    private final Context context;
    private final Activity activity;
    private final SessionManager session;

    @BindView(R.id.referencesDocsLayout)
    ViewGroup referencesDocsLayout;
    @BindView(R.id.referencesImagesLayout)
    ViewGroup referencesImagesLayout;

    public FileSourcesBinder(Activity activity, ProgressDialog downloadProgressDialog, ViewGroup view, SessionManager session) {
        this.downloadProgressDialog = downloadProgressDialog;
        this.context = view.getContext();
        this.activity = activity;
        this.session = session;
        ButterKnife.bind(this, view);
    }

    public void bindData(Post post) {
        referencesImagesLayout.removeAllViews();
        referencesDocsLayout.removeAllViews();
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
                rowLayout.setBackgroundResource(R.color.dashboard_image_border_color);
                rowLayout.setPadding(0, HALF_IMAGE_SPACE * 2, 0, 0);
            }
            rowLayout.setLayoutParams(layoutParams);

            int col = 0;
            for(FileSource fileSource: imageFileSourcesRow) {
                View imageView = makeImageCell(context, fileSource.attachment_url, imageFileSourcesRow.size(), col);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PostImagesViewActivity.class);
                        intent.putExtra("post", Parcels.wrap(post));
                        context.startActivity(intent);
                    }
                });
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
                        if(currentRow.size() >= 1) {
                            nextRow = true;
                        }
                    } else {
                        if(currentRow.size() >= 2) {
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

    private View makeImageCell(Context context, String url, int col_size, int current_col) {
        final ImageView imageView = new ImageView(context);
        
        int height = 300;
        if(col_size == 1) {
            height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);
        ImageHelper.loadInto(imageView, url, (col_size <= 1 ? ImageView.ScaleType.CENTER_INSIDE : ImageView.ScaleType.CENTER_CROP));

        LinearLayout rowBgLayout = new LinearLayout(context);
        rowBgLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.dashboard_image_border_color));
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
            CardView fileSourcesLayout = (CardView) inflater.inflate(R.layout.references_doc_file_source, referencesDocsLayout, false);
            new DocFileSourceHolder(fileSourcesLayout).bindData(docFileSource);
            referencesDocsLayout.addView(fileSourcesLayout);

            final ProgressDialog progressBar = new ProgressDialog(FileSourcesBinder.this.activity);
            progressBar.setMessage("다운로드 중");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setIndeterminate(true);
            progressBar.setCancelable(true);

            fileSourcesLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final DownloadFilesTask downloadTask = new DownloadFilesTask(FileSourcesBinder.this.context, downloadProgressDialog, session.getPartiAccessToken(), post.id, docFileSource.id, docFileSource.name);
                    downloadTask.execute();
                }
            });
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
