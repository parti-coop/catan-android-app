package xyz.parti.catan.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.PostFeedAdapter;
import xyz.parti.catan.sessions.SessionManager;

import static android.view.KeyCharacterMap.load;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.appToolbar)
    Toolbar appToolbar;
    @BindView(R.id.dashboardView)
    RecyclerView dashboardView;
    @BindView(R.id.logoutButton)
    Button logoutButton;

    private PostFeedAdapter feedAdapter;
    private SessionManager session;
    List<InfinitableModelHolder<Post>> posts;
    private PostsService postsService;

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

                setupToolbar();
                setupFeed();
                setupLogoutButton();
            }

            @Override
            public void onLoggedOut() {
                finish();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(appToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    private void setupFeed() {
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

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                session.logoutUser(MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
