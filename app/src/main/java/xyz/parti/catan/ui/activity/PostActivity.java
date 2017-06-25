package xyz.parti.catan.ui.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.presenter.PostPresenter;
import xyz.parti.catan.ui.task.DownloadFilesTask;
import xyz.parti.catan.ui.view.CommentView;
import xyz.parti.catan.ui.view.MaxHeightScrollView;
import xyz.parti.catan.ui.view.NewCommentForm;

/**
 * Created by dalikim on 2017. 5. 15..
 */

public class PostActivity extends BaseActivity implements PostPresenter.View {
    PostPresenter presenter;
    private ProgressDialog downloadProgressDialog;

    @BindView(R.id.scrollview_post)
    ScrollView postScrollView;
    @BindView(R.id.layout_post)
    ConstraintLayout postLayout;
    @BindView(R.id.layout_sticky_comment)
    FrameLayout stickyCommentLayout;
    @BindView(R.id.new_comment_form)
    NewCommentForm newCommentForm;

    private PostBinder postBinder;
    private CommentView stickeyCommentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(PostActivity.this);

        SessionManager session = new SessionManager(getApplicationContext());
        if(getIntent() == null) {
            finish();
            return;
        }
        Post post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        if(post == null) {
            finish();
            return;
        }

        presenter = new PostPresenter(post, session);
        presenter.attachView(this);

        setupPost(post);
        if(post.needToStickyComment(Constants.LIMIT_LAST_COMMENTS_COUNT_IN_POST_ACTIVITY)) {
            setupStickyComment(post);
        } else {
            if(post.sticky_comment != null) {
                setupHighlightComment(post.sticky_comment);
            }
        }

        newCommentForm.attachPresenter(presenter);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupHighlightComment(final Comment comment) {
        postLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                postLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                postBinder.highlightComment(postScrollView, comment);
            }
        });
    }

    private void setupPost(Post post) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.downloadProgressDialog = new ProgressDialog(this, R.style.AppProgressDialog);
        } else {
            this.downloadProgressDialog = new ProgressDialog(this);
        }
        this.postBinder = new PostBinder(this, this.postLayout, false, Constants.LIMIT_LAST_COMMENTS_COUNT_IN_POST_ACTIVITY);
        this.postBinder.bind(this.presenter, post);
    }

    @Override
    protected void onDestroy() {
        if(this.postBinder != null) {
            this.postBinder.unbind();
            this.postBinder = null;
        }
        if(presenter != null) {
            presenter.detachView();
        }
        if(stickeyCommentView != null) {
            stickeyCommentView.unbind();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.REQUEST_UPDATE_POST){
            if(data == null) {
                return;
            }
            Post post = Parcels.unwrap(data.getParcelableExtra("post"));
            if(post == null) {
                return;
            }
            if(this.presenter != null) {
                if(post.sticky_comment != null) {
                    post.sticky_comment = null;
                }
                stickyCommentLayout.setVisibility(View.GONE);
                presenter.changePost(post);
            }
        }
    }

    @Override
    public void onBackPressed() {
        setupNewCommentsResult();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setupNewCommentsResult();
        finish();
        return super.onSupportNavigateUp();
    }

    private void setupNewCommentsResult() {
        Intent intent = new Intent();
        intent.putExtra("post", Parcels.wrap(presenter.getPost()));
        setResult(MainActivity.REQUEST_UPDATE_POST, intent);
    }

    @Override
    public void showUrl(Uri url) {
        startActivity(new Intent(Intent.ACTION_VIEW, url));
    }

    @Override
    public void showVideo(Uri webUrl, Uri appUrl) {
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, appUrl);
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            showUrl(webUrl);
        }
    }

    @Override
    public void showNewCommentForm(Post post) {
        newCommentForm.focusForm(null);
        stickyCommentLayout.setVisibility(View.GONE);
    }

    @Override
    public void showNewCommentForm(Post post, Comment comment) {
        newCommentForm.focusForm(comment);
    }

    @Override
    public void showAllComments(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivityForResult(intent, MainActivity.REQUEST_UPDATE_POST);
    }

    @Override
    public void showImageFileSource(Post post) {
        Intent intent = new Intent(this, PostImagesViewActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivity(intent);
    }

    @Override
    public void downloadFile(final Post post, final FileSource docFileSource) {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        final DownloadFilesTask downloadTask = new DownloadFilesTask(presenter, post.id, docFileSource.id, docFileSource.name);
                        downloadTask.execute();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    }
                })
                .setRationaleMessage(R.string.doc_file_source_download_permission_rationale)
                .setDeniedMessage(R.string.doc_file_source_download_permission_denied)
                .setPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"})
                .check();
    }

    @Override
    public void showDownloadDocFileSourceProgress(final DownloadFilesTask task) {
        downloadProgressDialog.setMessage("다운로드 중");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel(true);
            }
        });
        downloadProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                task.cancel(true);
            }
        });

        downloadProgressDialog.show();
    }

    @Override
    public void updateDownloadDocFileSourceProgress(int percentage, String message) {
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
        downloadProgressDialog.setProgress(percentage);
        downloadProgressDialog.setMessage(message);
    }

    @Override
    public void hideDownloadDocFileSourceProgress() {
        downloadProgressDialog.dismiss();
    }

    @Override
    public void showDownloadedFile(Uri uri, String mimeType) {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setDataAndType(uri, mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        List<ResolveInfo> resolvedInfoActivities = getPackageManager().queryIntentActivities(newIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolvedInfoActivities) {
            grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        try {
            startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            reportInfo(R.string.not_found_app_for_downloaded_file);
        }
    }

    @Override
    public void showPost(Post post) {
        // do nothing
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void changePost(Post post, Object payload) {
        if(payload == null) {
            this.postBinder.bind(presenter, post);
        } else {
            this.postBinder.rebindData(presenter, post, payload);
        }
    }

    @Override
    public void setSendingCommentForm() {
        newCommentForm.setSending();
    }

    @Override
    public void setCompletedCommentForm() {
        newCommentForm.setSendCompleted();
    }

    @Override
    public void showNewComment(Post post) {
        postBinder.rebindData(presenter, post, Post.PLAYLOAD_LATEST_COMMENT);
        postLayout.post(new Runnable() {
            @Override
            public void run() {
                postScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        stickyCommentLayout.setVisibility(View.GONE);
    }

    @Override
    public void changeStickyComment(Post post, Comment comment, Object payload) {
        if(stickeyCommentView == null) return;
        stickeyCommentView.rebindData(post, comment, payload);
    }

    public void setupStickyComment(Post post) {
        if(post.sticky_comment == null) return;

        stickeyCommentView = new CommentView(this);
        stickeyCommentView.attachPresenter(presenter);
        stickeyCommentView.setPadding(getResources().getDimensionPixelSize(R.dimen.post_card_padding), 0, getResources().getDimensionPixelSize(R.dimen.post_card_padding), 0);
        stickeyCommentView.bindData(post, post.sticky_comment, false);

        final MaxHeightScrollView scrollView = new MaxHeightScrollView(PostActivity.this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        scrollView.addView(stickeyCommentView);

        View line = new View(this);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2));
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_gray));

        stickyCommentLayout.addView(line);
        stickyCommentLayout.addView(scrollView);

        stickyCommentLayout.post(new Runnable() {
            @Override
            public void run() {
                int maxHeight = getResources().getDimensionPixelSize(R.dimen.sticky_comment_max_height);
                int currentHeight = stickeyCommentView.getHeight();
                scrollView.setMaxHeight(Math.min(maxHeight, currentHeight));
            }
        });
    }
}
