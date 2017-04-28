package xyz.parti.catan.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.mancj.slideup.SlideUp;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import xyz.parti.catan.models.Download;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.NavigationItem;
import xyz.parti.catan.ui.adapter.PostFeedAdapter;
import xyz.parti.catan.sessions.SessionManager;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_CHECK_NEW_POSTS = "xyz.parti.catan.action.CheckNewPosts";
    public static final long INTERVAL_CHECK_NEW_POSTS = 5 * 60 * 1000;

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

    private PostFeedAdapter feedAdapter;
    private SessionManager session;
    List<InfinitableModelHolder<Post>> posts;
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
        feedAdapter = new PostFeedAdapter(this, downloadProgressDialog, posts, this.session);
        feedAdapter.setLoadMoreListener(
            new PostFeedAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    dashboardView.post(new Runnable() {
                        @Override
                        public void run() {
                            int index = posts.size() - 1;
                            loadMorePosts(index);
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
        loadFirstPosts();
    }

    private void loadFirstPosts(){
        dashboardView.setVisibility(View.INVISIBLE);

        Call<Page<Post>> call = postsService.getDashBoardLastest();
        APIHelper.enqueueWithRetry(call, 5, new Callback<Page<Post>>() {
            @Override
            public void onResponse(Call<Page<Post>> call, Response<Page<Post>> response) {
                if(response.isSuccessful()){
                    feedAdapter.clear();
                    posts.clear();

                    Page<Post> page = response.body();
                    posts.addAll(InfinitableModelHolder.from(page.items));
                    feedAdapter.setMoreDataAvailable(page.has_more_item);
                    feedAdapter.notifyDataChanged();
                }else{
                    ReportHelper.wtf(getApplicationContext(), "Losd first post error : " + response.code());
                }
                swipeContainer.setRefreshing(false);
                dashboardView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void loadMorePosts(int index){
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

                    if(!feedAdapter.getMoreDataAvailable()) {
                        Toast.makeText(getApplicationContext(), R.string.no_more_data, Toast.LENGTH_LONG).show();
                    }
                    feedAdapter.notifyDataChanged();
                    //should call the custom method adapter.notifyDataChanged here to get the correct loading status
                }else{
                    feedAdapter.setMoreDataAvailable(false);
                    feedAdapter.notifyDataChanged();

                    ReportHelper.wtf(getApplicationContext(), "Load More Response Error " + String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                feedAdapter.setMoreDataAvailable(false);
                feedAdapter.notifyDataChanged();

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
}
