package xyz.parti.catan.ui.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.presenter.PostPresenter;
import xyz.parti.catan.ui.task.DownloadFilesTask;

/**
 * Created by dalikim on 2017. 5. 15..
 */

public class PostActivity extends BaseActivity implements PostPresenter.View {
    PostPresenter presenter;
    private ProgressDialog downloadProgressDialog;

    @BindView(R.id.layout_post)
    LinearLayout postLayout;

    private PostBinder postBinder;

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

        setUpPost(post);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpPost(Post post) {
        this.downloadProgressDialog = new ProgressDialog(this);
        this.postBinder = new PostBinder(this, this.postLayout, this.presenter);
        this.postBinder.bindData(post);
    }

    @Override
    protected void onDestroy() {
        if(this.postBinder != null) {
            this.postBinder = null;
        }
        if(presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.REQUEST_NEW_COMMENT){
            if(data == null) {
                return;
            }
            Post post = Parcels.unwrap(data.getParcelableExtra("post"));
            if(post == null) {
                return;
            }
            if(this.presenter != null) {
                presenter.changePost(post, Post.PLAYLOAD_LATEST_COMMENT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        setUpNewCommentsResult();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setUpNewCommentsResult();
        finish();
        return super.onSupportNavigateUp();
    }

    private void setUpNewCommentsResult() {
        Intent intent = new Intent();
        intent.putExtra("post", Parcels.wrap(presenter.getPost()));
        setResult(MainActivity.REQUEST_NEW_COMMENT, intent);
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
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        intent.putExtra("focusInput", true);
        startActivityForResult(intent, MainActivity.REQUEST_NEW_COMMENT);
    }

    @Override
    public void showAllComments(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivityForResult(intent, MainActivity.REQUEST_NEW_COMMENT);
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
    public void showDownloadDocFileSourceProgress(DownloadFilesTask task) {
        downloadProgressDialog.setMessage("다운로드 중");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setOnCancelListener(dialogInterface -> task.cancel(true));
        downloadProgressDialog.setOnDismissListener(dialog -> task.cancel(true));

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
    public void showDownloadedFile(File file, String mimeType) {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setDataAndType(Uri.fromFile(file), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.getApplicationContext(), "다운로드된 파일을 열 수 있는 프로그램이 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void changePost(Post post, Object payload) {
        this.postBinder.bindPartialData(post, payload);
    }

    @Override
    public void changeSurvey(Post post) {
        this.postBinder.bindPartialData(post, post.survey);
    }
}
