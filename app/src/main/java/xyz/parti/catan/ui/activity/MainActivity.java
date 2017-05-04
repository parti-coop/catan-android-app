package xyz.parti.catan.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mancj.slideup.SlideUp;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.alarms.LocalBroadcastableAlarmReceiver;
import xyz.parti.catan.models.FileSource;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;
import xyz.parti.catan.ui.task.DownloadFilesTask;
import xyz.parti.catan.ui.view.NavigationItem;

public class MainActivity extends AppCompatActivity implements PostFeedPresenter.View {
    public static final String ACTION_CHECK_NEW_POSTS = "xyz.parti.catan.action.CheckNewPosts";
    public static final long INTERVAL_CHECK_NEW_POSTS = 10 * 60 * 1000;
    public static final int REQUEST_NEW_COMMENT = 1;

    @BindView(R.id.toolbar_app)
    Toolbar appToolbar;
    @BindView(R.id.recyclerview_list)
    RecyclerView listRecyclerView;
    @BindView(R.id.drawer_layout_root)
    DrawerLayout rootDrawerLayout;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.layout_drawer_panel)
    RelativeLayout drawerPanelLayout;
    @BindView(R.id.navigation_view_drawer)
    NavigationView drawerNavigationView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.layout_new_posts_sign)
    FrameLayout newPostsSignLayout;
    @BindView(R.id.button_new_posts_sign)
    FancyButton newPostsSignButton;

    private PostFeedRecyclerViewAdapter feedAdapter;
    private SessionManager session;

    private AlarmManager newPostsAlarmMgr;
    private PendingIntent newPostsAlarmIntent;

    private BroadcastReceiver newPostsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(presenter == null) {
                return;
            }
            presenter.checkNewPosts();
        }
    };
    List<NavigationItem> navigationItems = new ArrayList<>();
    private ActionBarDrawerToggle drawerToggle;
    private SlideUp newPostsSignSlideUp;
    private ProgressDialog downloadProgressDialog;
    private PostFeedPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());
        session.checkLogin(new SessionManager.OnCheckListener() {
            @Override
            public void onLoggedIn() {
                if(BuildConfig.DEBUG) {
                    Log.d(Constants.TAG, "이미 로그인되어 있음");
                }
                ButterKnife.bind(MainActivity.this);

                presenter = new PostFeedPresenter(MainActivity.this, session);
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

    private void setUpSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadFirstPosts();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpDrawerBar() {
        navigationItems.add(new NavigationItem(this, R.string.navigation_logout));
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                rootDrawerLayout,            /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );
        drawerNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                rootDrawerLayout.closeDrawers();

                switch (item.getItemId()){
                    case R.id.logoutButton:
                        session.logoutUser();
                        return true;
                    default:
                        return true;
                }
            }
        });
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
        startCheckNewPostAlarm();

        newPostsSignSlideUp = new SlideUp.Builder(newPostsSignLayout)
                .withStartState(SlideUp.State.HIDDEN)
                .withStartGravity(Gravity.TOP)
                .build();
        newPostsSignSlideUp.hide();
        listRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(!newPostsSignSlideUp.isVisible() || newPostsSignSlideUp.isAnimationRunning()) {
                    return;
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(newPostsSignSlideUp.isVisible() && !newPostsSignSlideUp.isAnimationRunning()) {
                            newPostsSignSlideUp.hide();
                        }
                    }
                }, 5000);
            }
        });

        newPostsSignButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if(newPostsSignSlideUp.isVisible() && !newPostsSignSlideUp.isAnimationRunning()) {
                            newPostsSignSlideUp.hide();
                        }
                        swipeRefreshLayout.setRefreshing(true);
                        listRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            public void onScrollStateChanged(RecyclerView view, int state) {
                                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                                    view.removeOnScrollListener(this);
                                    appBarLayout.setExpanded(true, true);
                                }
                            }
                        });
                        listRecyclerView.smoothScrollToPosition(0);
                        presenter.loadFirstPosts();
                    }
                });
            }
        });
    }

    private void startCheckNewPostAlarm() {
        Log.d(Constants.TAG, "startCheckNewPostAlarm");
        newPostsAlarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, LocalBroadcastableAlarmReceiver.class);
        intent.putExtra(LocalBroadcastableAlarmReceiver.INTENT_EXTRA_ACTION, ACTION_CHECK_NEW_POSTS);
        newPostsAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        newPostsAlarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INTERVAL_CHECK_NEW_POSTS, INTERVAL_CHECK_NEW_POSTS, newPostsAlarmIntent);
    }

    private void cancelCheckNewPostAlarm() {
        Log.d(Constants.TAG, "cancelCheckNewPostAlarm");
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

        feedAdapter = new PostFeedRecyclerViewAdapter(this);
        feedAdapter.setPresenter(presenter);
        feedAdapter.setLoadMoreListener(
            new PostFeedRecyclerViewAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    listRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            presenter.loadMorePosts();
                        }
                    });
                    //Calling loadMorePosts function in Runnable to fix the
                    // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
                }
            });

        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listRecyclerView.setAdapter(feedAdapter);

//        loadFirstPosts();
        this.presenter.setPostFeedRecyclerViewAdapter(feedAdapter);
        this.presenter.loadFirstPosts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newPostsBroadcastReceiver);
        if(downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
        }
        if(presenter != null) {
            presenter.detachView();
        }
    }

    @Override
    protected void onPause()  {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newPostsBroadcastReceiver);
        cancelCheckNewPostAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(newPostsBroadcastReceiver, new IntentFilter(ACTION_CHECK_NEW_POSTS));
        startCheckNewPostAlarm();
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
            public void onDismiss(DialogInterface dialog) {
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
    public void downloadFile(final Post post, final FileSource docFileSource) {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        final DownloadFilesTask downloadTask = new DownloadFilesTask(presenter, MainActivity.this, post.id, docFileSource.id, docFileSource.name);
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
    public void showSimpleMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showImageFileSource(Post post) {
        Intent intent = new Intent(this, PostImagesViewActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivity(intent);
    }

    @Override
    public void setSwipeRefreshing(boolean b) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean isVisibleNewPostsSignSlideUp() {
        return newPostsSignSlideUp.isVisible();
    }

    @Override
    public void showNewPostsSignSlideUp() {
        newPostsSignSlideUp.show();
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
}
