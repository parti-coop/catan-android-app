package xyz.parti.catan.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import mehdi.sakout.fancybuttons.FancyButton;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.IntentHelper;
import xyz.parti.catan.helper.NetworkHelper;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.binder.DrawerNavigationHeaderBinder;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;
import xyz.parti.catan.ui.task.DownloadFilesTask;
import xyz.parti.catan.ui.view.NewPostSignAnimator;

public class MainActivity extends BaseActivity implements PostFeedPresenter.View {
    public static final String ACTION_CHECK_NEW_POSTS = "xyz.parti.catan.action.CheckNewPosts";
    public static final long INTERVAL_CHECK_NEW_POSTS = 10 * 60 * 1000;
    public static final int REQUEST_NEW_COMMENT = 1;

    @BindView(R.id.toolbar_app)
    Toolbar appToolbar;
    @BindView(R.id.recyclerview_post_list)
    RecyclerView postListRecyclerView;
    @BindView(R.id.drawer_layout_root)
    DrawerLayout rootDrawerLayout;
    @BindView(R.id.appbarlayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.layout_drawer_panel)
    RelativeLayout drawerPanelLayout;
    @BindView(R.id.navigation_view_drawer)
    NavigationView drawerNavigationView;
    @BindView(R.id.swipe_refresh_layout_post_list)
    SwipeRefreshLayout postListSwipeRefreshLayout;
    @BindView(R.id.layout_new_posts_sign)
    FrameLayout newPostsSignLayout;
    @BindView(R.id.button_new_posts_sign)
    FancyButton newPostsSignButton;
    @BindView(R.id.layout_post_list_demo)
    ShimmerFrameLayout postListDemoLayout;
    @BindView(R.id.layout_no_posts_sign)
    RelativeLayout noPostSignLayout;
    @BindView(R.id.button_go_to_parties)
    FancyButton goToPartiesButton;
    @BindView(R.id.button_retry)
    FancyButton retryButton;

    private AlarmManager newPostsAlarmMgr;
    private PendingIntent newPostsAlarmIntent;

    private NewPostSignAnimator newPostsSignAnimator;

    private CheckNewPostBroadcastReceiver newPostsBroadcastReceiver = new CheckNewPostBroadcastReceiver();
    private ActionBarDrawerToggle drawerToggle;
    private ProgressDialog downloadProgressDialog;
    private PostFeedPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        if(new NetworkHelper(this).isValidNetwork()) {
            SessionManager session = new SessionManager(this.getApplicationContext());
            session.checkLogin(new SessionManager.OnCheckListener() {
                @Override
                public void onLoggedIn() {
                    if (BuildConfig.DEBUG) {
                        Log.d(Constants.TAG, "이미 로그인되어 있음");
                    }
                    ButterKnife.bind(MainActivity.this);

                    presenter = new PostFeedPresenter(session);
                    presenter.attachView(MainActivity.this);
                    checkAppVersion();
                    setUpToolbar();
                    setUpFeed();
                    setUpCheckNewPost();
                    setUpDrawerBar();
                    setUpSwipeRefresh();
                }

                @Override
                public void onLoggedOut() {
                    finish();
                }
            });
        }
    }

    private void checkAppVersion() {
        presenter.checkAppVersion();
    }

    private void setUpSwipeRefresh() {
        postListSwipeRefreshLayout.setOnRefreshListener(() -> presenter.loadFirstPosts());
        // Configure the refreshing colors
        postListSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpDrawerBar() {
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                rootDrawerLayout,      /* DrawerLayout object */
                R.string.drawer_open,        /* "open drawer" description */
                R.string.drawer_close       /* "close drawer" description */
        );
        drawerNavigationView.setNavigationItemSelectedListener(item -> {
            rootDrawerLayout.closeDrawers();

            switch (item.getItemId()){
                case R.id.button_settings:
                    if(presenter != null) {
                        presenter.showSettings();
                    }
                    return true;
                default:
                    return true;
            }
        });
        drawerNavigationView.setCheckedItem(R.id.item_post_feed);

        new DrawerNavigationHeaderBinder(drawerNavigationView.getHeaderView(0)).bindData(presenter.getCurrentUser());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setUpCheckNewPost() {
        newPostsSignAnimator = new NewPostSignAnimator(this.newPostsSignLayout);

        postListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newPostsSignAnimator != null) {
                    newPostsSignAnimator.hideDelayed(5000);
                }
            }
        });
        newPostsSignButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                if(newPostsSignAnimator != null) {
                    newPostsSignAnimator.hideImmediately();
                }
                postListSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        postListSwipeRefreshLayout.setRefreshing(true);
                        if(postListRecyclerView.computeVerticalScrollOffset() != 0) {
                            postListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                public void onScrollStateChanged(RecyclerView view, int state) {
                                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                                        view.removeOnScrollListener(this);
                                        appBarLayout.setExpanded(true, true);
                                    }
                                }
                            });
                            postListRecyclerView.smoothScrollToPosition(0);
                        } else {
                            appBarLayout.setExpanded(true, true);
                        }
                        presenter.loadFirstPosts();
                    }
                });
            }
        });

        newPostsAlarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        newPostsAlarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_CHECK_NEW_POSTS), 0);
    }

    private void startCheckNewPostJob() {
        Log.d(Constants.TAG, "startCheckNewPostJob");
        if(newPostsBroadcastReceiver != null) {
            registerReceiver(newPostsBroadcastReceiver, new IntentFilter(ACTION_CHECK_NEW_POSTS));
        }
        if(newPostsAlarmMgr != null && newPostsAlarmIntent != null) {
            newPostsAlarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + INTERVAL_CHECK_NEW_POSTS, INTERVAL_CHECK_NEW_POSTS, newPostsAlarmIntent);
        }
    }

    private void cancelCheckNewPostJob() {
        Log.d(Constants.TAG, "cancelCheckNewPostJob");
        if(newPostsBroadcastReceiver != null) {
            try {
                unregisterReceiver(newPostsBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if(newPostsAlarmMgr != null && newPostsAlarmIntent != null) {
            newPostsAlarmMgr.cancel(newPostsAlarmIntent);
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(appToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        appToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    private void setUpFeed() {
        downloadProgressDialog = new ProgressDialog(this);

        PostFeedRecyclerViewAdapter feedAdapter = new PostFeedRecyclerViewAdapter(this);
        feedAdapter.setPresenter(presenter);
        feedAdapter.setLoadMoreListener(() -> {
            postListRecyclerView.post(() -> presenter.loadMorePosts());
            //Calling loadMorePosts function in Runnable to fix the
            // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
        });

        postListRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager recyclerViewLayout = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 1000;
            }
        };
        postListRecyclerView.setLayoutManager(recyclerViewLayout);
        postListRecyclerView.setAdapter(feedAdapter);
        postListRecyclerView.setItemViewCacheSize(50);
        postListRecyclerView.setDrawingCacheEnabled(true);
        postListRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        presenter.setPostFeedRecyclerViewAdapter(feedAdapter);
        presenter.loadFirstPosts();

        postListSwipeRefreshLayout.setEnabled(false);
        postListDemoLayout.startShimmerAnimation();
    }

    @Override
    protected void onDestroy() {
        cancelCheckNewPostJob();
        if(downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
        }
        if(presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause()  {
        super.onPause();
        cancelCheckNewPostJob();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCheckNewPostJob();
        presenter.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_NEW_COMMENT){
            Post post = Parcels.unwrap(data.getParcelableExtra("post"));
            if(this.presenter != null) {
                presenter.changePost(post, Post.PLAYLOAD_LATEST_COMMENT);
            }
        }
    }

    @Override
    public void showAllComments(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivityForResult(intent, REQUEST_NEW_COMMENT);
    }

    @Override
    public void showNewCommentForm(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        intent.putExtra("focusInput", true);
        startActivityForResult(intent, REQUEST_NEW_COMMENT);
    }

    @Override
    public void showEmpty(boolean isError) {
        postListDemoLayout.setVisibility(View.GONE);
        postListRecyclerView.setVisibility(View.GONE);
        noPostSignLayout.setVisibility(View.VISIBLE);

        if(isError) {
            retryButton.setVisibility(View.VISIBLE);
            goToPartiesButton.setVisibility(View.GONE);
        } else {
            retryButton.setVisibility(View.GONE);
            goToPartiesButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.button_retry)
    public void onClickRetry() {
        presenter.retryLoadingPost();
    }

    @OnClick(R.id.button_go_to_parties)
    public void onClickGoToParties() {
        presenter.goToParties();
    }

    @Override
    public void readyToRetry() {
        postListDemoLayout.setVisibility(View.VISIBLE);
        postListRecyclerView.setVisibility(View.VISIBLE);
        noPostSignLayout.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        goToPartiesButton.setVisibility(View.GONE);
    }

    @Override
    public void showDownloadDocFileSourceProgress(final DownloadFilesTask task) {
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
    public void ensureToPostListDemoIsGone() {
        postListDemoLayout.stopShimmerAnimation();
        postListDemoLayout.setVisibility(View.GONE);
    }

    @Override
    public void showPostListDemo() {
        appBarLayout.setExpanded(true);
        postListDemoLayout.startShimmerAnimation();
        postListDemoLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void ensureExpendedAppBar() {
        appBarLayout.setExpanded(true);
    }

    @Override
    public void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showNewVersionMessage(String newVersion) {
        Snackbar.make(rootDrawerLayout, String.format(getResources().getString(R.string.new_version), newVersion), 30 * 1000)
                .setAction(R.string.ok,
                    view -> new IntentHelper(this).startPlayStore(getPackageName()))
                .show();
    }

    @Override
    public void reportError(Throwable error) {
        ReportHelper.wtf(this, error);
    }

    @Override
    public void reportError(String message) {
        ReportHelper.wtf(this, message);
    }

    @Override
    public void showImageFileSource(Post post) {
        Intent intent = new Intent(this, PostImagesViewActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivity(intent);
    }

    @Override
    public void stopAndEnableSwipeRefreshing() {
        postListSwipeRefreshLayout.setEnabled(true);
        postListSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean isVisibleNewPostsSign() {
        return newPostsSignLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showNewPostsSign() {
        newPostsSignAnimator.show();
    }

    @Override
    public void showUrl(Uri url) {
        startActivity(new Intent(Intent.ACTION_VIEW, url));
    }

    @Override
    public void showVideo(Uri webUrl, @NonNull Uri appUrl) {
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, appUrl);
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            showUrl(webUrl);
        }
    }

    public class CheckNewPostBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(presenter == null) return;
            presenter.checkNewPosts();
        }
    }
}
