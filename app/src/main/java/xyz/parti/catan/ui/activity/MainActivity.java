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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mancj.slideup.SlideUp;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.alarms.LocalBroadcastableAlarmReceiver;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.APIHelper;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.FileSource;
import xyz.parti.catan.models.Option;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.models.User;
import xyz.parti.catan.services.FeedbacksService;
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.services.VotingsService;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;
import xyz.parti.catan.ui.task.DownloadFilesTask;
import xyz.parti.catan.ui.view.NavigationItem;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements PostFeedPresenter {
    public static final String ACTION_CHECK_NEW_POSTS = "xyz.parti.catan.action.CheckNewPosts";
    public static final long INTERVAL_CHECK_NEW_POSTS = 10 * 60 * 1000;

    @BindView(R.id.appToolbar)
    Toolbar appToolbar;
    @BindView(R.id.dashboardView)
    RecyclerView dashboardView;
    @BindView(R.id.rootLayout)
    DrawerLayout rootLayout;
    @BindView(R.id.appToolbarLayout)
    AppBarLayout appToolbarLayout;
    @BindView(R.id.drawerPane)
    RelativeLayout drawerPane;
    @BindView(R.id.drawerNavigationView)
    NavigationView drawerNavigationView;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;
    @BindView(R.id.newPostsSignLayout)
    FrameLayout newPostsSignLayout;
    @BindView(R.id.newPostsSignButton)
    FancyButton newPostsSignButton;

    private PostFeedRecyclerViewAdapter feedAdapter;
    private SessionManager session;
    List<InfinitableModelHolder<Post>> posts;

    private FeedbacksService feedbacksService;
    private PostsService postsService;

    private AlarmManager newPostsAlarmMgr;
    private PendingIntent newPostsAlarmIntent;

    private BroadcastReceiver newPostsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNewPosts();
        }
    };
    List<NavigationItem> navigationItems = new ArrayList<>();
    private ActionBarDrawerToggle drawerToggle;

    private SlideUp newPostsSignSlideUp;
    private ProgressDialog downloadProgressDialog;
    private VotingsService votingsService;

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
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFirstPosts();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setUpDrawerBar() {
        navigationItems.add(new NavigationItem(this, R.string.navigation_logout));
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                rootLayout,            /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );
        drawerNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                rootLayout.closeDrawers();

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
        dashboardView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        newPostsSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        if(newPostsSignSlideUp.isVisible() && !newPostsSignSlideUp.isAnimationRunning()) {
                            newPostsSignSlideUp.hide();
                        }
                        swipeContainer.setRefreshing(true);
                        dashboardView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            public void onScrollStateChanged(RecyclerView view, int state) {
                                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                                    view.removeOnScrollListener(this);
                                    appToolbarLayout.setExpanded(true, true);
                                }
                            }
                        });
                        dashboardView.smoothScrollToPosition(0);
                        loadFirstPosts();
                    }
                });
            }
        });
    }

    private void startCheckNewPostAlarm() {
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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    private void setUpFeed() {
        posts = new ArrayList<>();
        downloadProgressDialog = new ProgressDialog(this);

        feedAdapter = new PostFeedRecyclerViewAdapter(this, posts);
        feedAdapter.setPresenter(this);
        feedAdapter.setLoadMoreListener(
            new PostFeedRecyclerViewAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    dashboardView.post(new Runnable() {
                        @Override
                        public void run() {
                            loadMorePosts();
                        }
                    });
                    //Calling loadMorePosts function in Runnable to fix the
                    // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
                }
            });

        dashboardView.setHasFixedSize(true);
        dashboardView.setLayoutManager(new LinearLayoutManager(this));
        dashboardView.setAdapter(feedAdapter);

        postsService = ServiceGenerator.createService(PostsService.class, session);
        feedbacksService = ServiceGenerator.createService(FeedbacksService.class, session);
        votingsService = ServiceGenerator.createService(VotingsService.class, session);

        loadFirstPosts();
    }

    private void loadFirstPosts(){
        Call<Page<Post>> call = postsService.getDashBoardLastest();
        APIHelper.enqueueWithRetry(call, 5, new Callback<Page<Post>>() {
            @Override
            public void onResponse(Call<Page<Post>> call, Response<Page<Post>> response) {
                if(response.isSuccessful()){
                    feedAdapter.clearData();
                    posts.clear();

                    Page<Post> page = response.body();
                    posts.addAll(InfinitableModelHolder.from(page.items));
                    feedAdapter.setMoreDataAvailable(page.has_more_item);
                    feedAdapter.notifyAllDataChangedAndLoadFinished();
                }else{
                    ReportHelper.wtf(getApplicationContext(), "Losd first post error : " + response.code());
                }
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void loadMorePosts(){
        //add loading progress view
        InfinitableModelHolder<Post> post = posts.get(posts.size() - 1);
        posts.add(InfinitableModelHolder.<Post>forLoader());
        feedAdapter.notifyItemInserted(posts.size() - 1);

        Call<Page<Post>> call = postsService.getDashboardAfter(post.getModel().id);
        call.enqueue(new Callback<Page<Post>>() {
            @Override
            public void onResponse(Call<Page<Post>> call, Response<Page<Post>> response) {
                if(response.isSuccessful()){
                    //remove loading view
                    posts.remove(posts.size() - 1);

                    Page<Post> page = response.body();
                    List<Post> result = page.items;
                    if(result.size() > 0){
                        //add loaded data
                        posts.addAll(InfinitableModelHolder.from(result));
                        feedAdapter.setMoreDataAvailable(page.has_more_item);
                    }else{
                        //result size 0 means there is no more data available at server
                        feedAdapter.setMoreDataAvailable(false);
                        //telling adapter to stop calling loadFirstPosts more as no more server data available
                    }

                    feedAdapter.notifyAllDataChangedAndLoadFinished();
                    //should call the custom method adapter.notifyAllDataChangedAndLoadFinished here to get the correct loading status
                }else{
                    feedAdapter.setMoreDataAvailable(false);
                    feedAdapter.notifyAllDataChangedAndLoadFinished();

                    ReportHelper.wtf(getApplicationContext(), "Load More Response Error " + String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                feedAdapter.setMoreDataAvailable(false);
                feedAdapter.notifyAllDataChangedAndLoadFinished();

                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newPostsBroadcastReceiver);
        if(downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
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

    private void checkNewPosts() {
        Date lastStrockedAt = null;
        if(!posts.isEmpty()) {
            InfinitableModelHolder<Post> firstPost = posts.get(0);
            if(!firstPost.isLoader()) {
                lastStrockedAt = firstPost.getModel().last_stroked_at;
            }
        }
        if(lastStrockedAt == null) {
            return;
        }

        Call<JsonObject> call = postsService.hasUpdated(lastStrockedAt);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()) {
                    if (newPostsSignSlideUp.isVisible()) {
                        return;
                    }

                    if (response.body().get("has_updated").getAsBoolean()) {
                        newPostsSignSlideUp.show();
                    }
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Check new post error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    @Override
    public void onClickLinkSource(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onClickDocFileSource(final Post post, final FileSource docFileSource) {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        final DownloadFilesTask downloadTask = new DownloadFilesTask(MainActivity.this, MainActivity.this, post.id, docFileSource.id, docFileSource.name);
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
    public void onClickImageFileSource(Post post) {
        Intent intent = new Intent(this, PostImagesViewActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivity(intent);
    }

    @Override
    public void onClickMoreComments(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivity(intent);
    }

    @Override
    public void onClickNewComment(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        intent.putExtra("focusInput", true);
        startActivity(intent);
    }

    @Override
    public void onClickSurveyOption(final Post post, Option option, boolean isChecked) {
        Call<JsonNull> call = feedbacksService.feedback(option.id, isChecked);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    reloadPost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Feedback error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    @Override
    public void onClickPollAgree(final Post post) {
        final String newChoice = (post.poll.isAgreed() ? "unsure" : "agree");
        Call<JsonNull> call = votingsService.voting(post.poll.id, newChoice);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    post.poll.updateChoice(session.getCurrentUser(), newChoice);
                    reloadPost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Agree error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    @Override
    public void onClickPollDisgree(final Post post) {
        final String newChoice = (post.poll.isDisagreed()  ? "unsure" : "disagree");
        Call<JsonNull> call = votingsService.voting(post.poll.id, newChoice);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    post.poll.updateChoice(session.getCurrentUser(), newChoice);
                    reloadPost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Disagree error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    @Override
    public void onPreDownloadDocFileSource(final DownloadFilesTask task) {
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
    public void onProgressUpdateDownloadDocFileSource(int percentage, String message) {
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
        downloadProgressDialog.setProgress(percentage);
        downloadProgressDialog.setMessage(message);
    }

    @Override
    public void onPostDownloadDocFileSource() {
        downloadProgressDialog.dismiss();
    }

    @Override
    public void onSuccessDownloadDocFileSource(File outputFile, String fileName) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(getExtension(fileName));
        newIntent.setDataAndType(Uri.fromFile(outputFile), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.getApplicationContext(), "다운로드된 파일을 열 수 있는 프로그램이 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    @Override
    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    @Override
    public PartiAccessToken getPartiAccessToken() {
        return session.getPartiAccessToken();
    }

    public void reloadPost(final Post post) {
        Call<Post> call = postsService.getPost(post.id);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()) {
                    post.survey = response.body().survey;
                    feedAdapter.rebindData(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Rebind survey error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }
}
