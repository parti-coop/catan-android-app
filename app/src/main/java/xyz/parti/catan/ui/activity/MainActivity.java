package xyz.parti.catan.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.alarms.LocalBroadcastableAlarmReceiver;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.NavigationItem;
import xyz.parti.catan.ui.adapter.PostFeedAdapter;
import xyz.parti.catan.sessions.SessionManager;

public class MainActivity extends AppCompatActivity {
    public static final String ACTION_CHECK_NEW_POSTS = "xyz.parti.catan.action.CheckNewPosts";
    public static final long INTERVAL_CHECK_NEW_POSTS = 1 * 5 * 1000;

    @BindView(R.id.appToolbar)
    Toolbar appToolbar;
    @BindView(R.id.dashboardView)
    RecyclerView dashboardView;
    @BindView(R.id.rootLayout)
    DrawerLayout rootLayout;
    @BindView(R.id.newPostsSignLayout)
    View newPostsSignLayout;
    @BindView(R.id.drawerPane)
    RelativeLayout drawerPane;
    @BindView(R.id.drawerNavigationView)
    NavigationView drawerNavigationView;

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
            }

            @Override
            public void onLoggedOut() {
                finish();
            }
        });
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
                        session.logoutUser(MainActivity.this);
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
        newPostsAlarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, LocalBroadcastableAlarmReceiver.class);
        intent.putExtra(LocalBroadcastableAlarmReceiver.INTENT_EXTRA_ACTION, ACTION_CHECK_NEW_POSTS);
        newPostsAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        newPostsAlarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INTERVAL_CHECK_NEW_POSTS, INTERVAL_CHECK_NEW_POSTS, newPostsAlarmIntent);
    }

    private void setUpToolbar() {
        setSupportActionBar(appToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    private void setUpFeed() {
        posts = new ArrayList<>();
        feedAdapter = new PostFeedAdapter(this, session, posts);
        feedAdapter.setLoadMoreListener(
            new PostFeedAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    dashboardView.post(new Runnable() {
                        @Override
                        public void run() {
                            int index = posts.size() - 1;
                            loadMore(index);
                        }
                    });
                    //Calling loadMore function in Runnable to fix the
                    // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
                }
            });

        dashboardView.setHasFixedSize(true);
        dashboardView.setLayoutManager(new LinearLayoutManager(this));
        dashboardView.setAdapter(feedAdapter);

        postsService = ServiceGenerator.createService(PostsService.class, session);
        load();
    }

    private void load(){
        Call<Page<Post>> call = postsService.getDashBoardLastest();
        call.enqueue(new Callback<Page<Post>>() {
            @Override
            public void onResponse(Call<Page<Post>> call, Response<Page<Post>> response) {
                if(response.isSuccessful()){
                    Page<Post> page = response.body();
                    posts.addAll(InfinitableModelHolder.from(page.items));
                    feedAdapter.setMoreDataAvailable(page.has_more_item);
                    feedAdapter.notifyDataChanged();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
                    Log.e(Constants.TAG," Response Error "+String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
                Log.e(Constants.TAG," Response Error "+t.getMessage());
            }
        });
    }

    private void loadMore(int index){
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
                        //telling adapter to stop calling load more as no more server data available
                    }

                    if(!feedAdapter.getMoreDataAvailable()) {
                        Toast.makeText(getApplicationContext(), R.string.no_more_data, Toast.LENGTH_LONG).show();
                    }
                    feedAdapter.notifyDataChanged();
                    //should call the custom method adapter.notifyDataChanged here to get the correct loading status
                }else{
                    Toast.makeText(getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
                    Log.e(Constants.TAG," Load More Response Error "+String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.error_any, Toast.LENGTH_LONG).show();
                Log.e(Constants.TAG," Load More Response Error "+t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newPostsBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(newPostsBroadcastReceiver, new IntentFilter(ACTION_CHECK_NEW_POSTS));
    }

    private void checkNewPosts() {
        TSnackbar snack = TSnackbar.make(newPostsSignLayout, "Hello from TSnackBar.", TSnackbar.LENGTH_LONG);
        snack.show();
    }
}
