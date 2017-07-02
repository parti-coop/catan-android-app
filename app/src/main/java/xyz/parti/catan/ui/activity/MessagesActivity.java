package xyz.parti.catan.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Message;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.adapter.DefaultProgressItem;
import xyz.parti.catan.ui.adapter.MessageItem;
import xyz.parti.catan.ui.presenter.MessagesPresenter;


public class MessagesActivity extends BaseActivity implements MessagesPresenter.View {
    MessagesPresenter presenter;
    private FastItemAdapter<MessageItem> fastAdapter;
    private FooterAdapter<ProgressItem> footerAdapter;
    private EndlessRecyclerOnScrollListener loadMoreScrollListener;

    @BindView(R.id.layout_coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.swipe_refresh_layout_message_list)
    SwipeRefreshLayout messageListSwipeRefreshLayout;
    @BindView(R.id.recyclerview_list)
    RecyclerView listRecyclerView;
    @BindView(R.id.layout_messages_list_wrapper)
    FrameLayout messageListWrapperLayout;
    @BindView(R.id.layout_message_list_demo)
    LinearLayout messageListDemoLayout;
    @BindView(R.id.layout_no_messages_sign)
    LinearLayout noMessageSignLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(MessagesActivity.this);
        SessionManager session = new SessionManager(getApplicationContext());

        presenter = new MessagesPresenter(session);
        presenter.attachView(this);

        setupMessages();
        setupSwipeRefresh();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setupMessages() {
        footerAdapter = new FooterAdapter<>();
        fastAdapter = new FastItemAdapter<>();
        fastAdapter.withSelectable(true);
        fastAdapter.withOnClickListener(new FastAdapter.OnClickListener<MessageItem>() {
            @Override
            public boolean onClick(View v, IAdapter<MessageItem> adapter, MessageItem item, int position) {
                presenter.showMessage(item.getMessage());
                return true;
            }
        });
        listRecyclerView.setHasFixedSize(true);
        LinearLayoutManager recyclerViewLayout = new LinearLayoutManager(this);
        listRecyclerView.setLayoutManager(recyclerViewLayout);
        listRecyclerView.setAdapter(footerAdapter.wrap(fastAdapter));

        presenter.loadFirstMessages();
    }

    private void setupSwipeRefresh() {
        messageListSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadFirstMessages();
            }
        });
        // Configure the refreshing colors
        messageListSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        messageListSwipeRefreshLayout.setEnabled(false);
        messageListSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        if(presenter != null) {
            presenter.detachView();
        }
        if(loadMoreScrollListener != null) {
            listRecyclerView.removeOnScrollListener(loadMoreScrollListener);
        }
        if(messageListSwipeRefreshLayout != null) {
            messageListSwipeRefreshLayout.setOnRefreshListener(null);
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void showDemo() {
        messageListDemoLayout.setVisibility(View.VISIBLE);
        noMessageSignLayout.setVisibility(View.GONE);
        messageListWrapperLayout.setVisibility(View.GONE);
    }

    @Override
    public void showFirstMessages(Page<Message> messagesPage) {
        messageListDemoLayout.setVisibility(View.GONE);
        footerAdapter.clear();

        if(messagesPage.items == null || messagesPage.items.size() <= 0) {
            noMessageSignLayout.setVisibility(View.VISIBLE);
            messageListWrapperLayout.setVisibility(View.GONE);
            return;
        }

        noMessageSignLayout.setVisibility(View.GONE);
        messageListWrapperLayout.setVisibility(View.VISIBLE);

        List<MessageItem> list = new ArrayList<>();
        fastAdapter.clear();
        for(Message message : messagesPage.items) {
            list.add(new MessageItem(message));
        }
        fastAdapter.add(list);
        if(messagesPage.has_more_item) {
            listRecyclerView.clearOnScrollListeners();
            loadMoreScrollListener = new EndlessRecyclerOnScrollListener() {
                @Override
                public void onLoadMore(int currentPage) {
                    messageListSwipeRefreshLayout.setRefreshing(false);
                    footerAdapter.clear();
                    footerAdapter.add(new DefaultProgressItem().withEnabled(false));
                    MessageItem lastItem = fastAdapter.getAdapterItem(fastAdapter.getAdapterItemCount() - 1);
                    if(lastItem != null) {
                        presenter.loadMoreMessages(lastItem.getMessage());
                    }
                }
            };
            listRecyclerView.addOnScrollListener(loadMoreScrollListener);
        }

        messageListSwipeRefreshLayout.setEnabled(true);
        messageListSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showMoreMessages(Page<Message> messagesPage) {
        messageListDemoLayout.setVisibility(View.GONE);
        noMessageSignLayout.setVisibility(View.GONE);
        messageListWrapperLayout.setVisibility(View.VISIBLE);
        footerAdapter.clear();

        List<MessageItem> list = new ArrayList<>();
        for(Message message : messagesPage.items) {
            list.add(new MessageItem(message));
        }
        fastAdapter.add(list);
        if(!messagesPage.has_more_item) {
            if (loadMoreScrollListener != null)
                listRecyclerView.removeOnScrollListener(loadMoreScrollListener);
        }
    }

    @Override
    public void showPost(Post post) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivityForResult(intent, MainActivity.REQUEST_UPDATE_POST);
    }

    @Override
    public void showUrl(Uri url) {
        startActivity(new Intent(Intent.ACTION_VIEW, url));
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showErrorList() {
        messageListDemoLayout.setVisibility(View.GONE);
        noMessageSignLayout.setVisibility(View.VISIBLE);
        messageListWrapperLayout.setVisibility(View.GONE);
    }
}
