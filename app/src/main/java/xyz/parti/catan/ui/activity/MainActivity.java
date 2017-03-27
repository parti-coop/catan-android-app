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

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.adapter.PostFeedAdapter;
import xyz.parti.catan.sessions.SessionManager;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.appToolbar)
    Toolbar appToolbar;
    @BindView(R.id.dashboardView)
    RecyclerView dashboardView;
    @BindView(R.id.logoutButton)
    Button logoutButton;

    private PostFeedAdapter feedAdapter;
    private SessionManager session;

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

                feedAdapter.updateItems();
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        dashboardView.setLayoutManager(linearLayoutManager);
        feedAdapter = new PostFeedAdapter(this);
        dashboardView.setAdapter(feedAdapter);
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
