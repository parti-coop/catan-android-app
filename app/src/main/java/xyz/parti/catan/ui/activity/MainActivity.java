package xyz.parti.catan.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.adapter.PostFeedAdapter;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.appToolbar)
    Toolbar appToolbar;
    @BindView(R.id.dashboard)
    RecyclerView dashboard;

    private PostFeedAdapter feedAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setupToolbar();
        setupFeed();

        feedAdapter.updateItems();
    }

    private void setupToolbar() {
        setSupportActionBar(appToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    private void setupFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        dashboard.setLayoutManager(linearLayoutManager);
        feedAdapter = new PostFeedAdapter(this);
        dashboard.setAdapter(feedAdapter);
    }
}
