package xyz.parti.catan.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.adapter.LoadMoreRecyclerViewAdapter;
import xyz.parti.catan.ui.presenter.CommentFeedPresenter;
import xyz.parti.catan.ui.view.NewCommentForm;


public class AllCommentsActivity extends BaseActivity implements CommentFeedPresenter.View {
    private CommentFeedRecyclerViewAdapter feedAdapter;
    private CommentFeedPresenter presenter;

    @BindView(R.id.recyclerview_list)
    RecyclerView listRecyclerView;
    @BindView(R.id.layout_list_wrapper)
    ViewGroup listWrapperView;
    @BindView(R.id.layout_no_comments_sign)
    ViewGroup noCommentsSignView;
    @BindView(R.id.new_comment_form)
    NewCommentForm newCommentForm;
    @BindView(R.id.layout_comment_list_demo)
    LinearLayout commentListDemoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comments);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(AllCommentsActivity.this);

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
        Comment comment = null;
        Parcelable commentObject = getIntent().getParcelableExtra("comment");
        if(commentObject != null) {
            comment = Parcels.unwrap(commentObject);
        }
        boolean focusInput = getIntent().getBooleanExtra("focusInput", false);

        presenter = new CommentFeedPresenter(post, session);
        presenter.attachView(this);

        if(post.comments_count > 0) {
            showCommentList();
        } else {
            noCommentsSignView.setVisibility(View.VISIBLE);
            listWrapperView.setVisibility(View.GONE);
        }
        setupComments(focusInput, post, comment);
        setupCommentForm(presenter);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupCommentForm(CommentFeedPresenter presenter) {
        newCommentForm.attachPresenter(presenter);
    }

    private void setupComments(boolean focusInput, Post post, Comment comment) {
        feedAdapter = new CommentFeedRecyclerViewAdapter(this, post);
        feedAdapter.setLoadMoreListener(new LoadMoreRecyclerViewAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                listRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        presenter.loadMoreComments();
                    }
                });
            }
        });
        presenter.setCommentFeedRecyclerViewAdapter(feedAdapter);
        feedAdapter.setPresenter(presenter);

        listRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        listRecyclerView.setLayoutManager(layoutManager);
        listRecyclerView.setAdapter(this.feedAdapter);
        listRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                final int newHeight = bottom - top;
                final int oldHeight = oldBottom - oldTop;
                if(oldHeight != 0 && newHeight != oldHeight) {
                    listRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            listRecyclerView.scrollBy(0, oldHeight - newHeight);
                        }
                    });
                }
            }
        });
        presenter.loadFirstComments();

        if(focusInput) {
            newCommentForm.focusForm(comment);
        }
    }


    @Override
    protected void onDestroy() {
        if(presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void setSendingCommentForm() {
        newCommentForm.setSending();
        if(this.feedAdapter.getItemCount() > 0) {
            listRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listRecyclerView.smoothScrollToPosition(feedAdapter.getLastPosition());
                }
            }, 100);
        }
    }

    @Override
    public void setCompletedCommentForm() {
        if(this.feedAdapter.getItemCount() > 0) {
            listRecyclerView.smoothScrollToPosition(feedAdapter.getLastPosition());
        }
        newCommentForm.setSendCompleted();
    }

    @Override
    public void showCommentList() {
        noCommentsSignView.setVisibility(View.GONE);
        listWrapperView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNewCommentForm(Comment comment) {
        newCommentForm.focusForm(comment);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showDemo() {
        commentListDemoLayout.setVisibility(View.VISIBLE);
        listWrapperView.setVisibility(View.GONE);
    }

    @Override
    public void hideDemo() {
        commentListDemoLayout.setVisibility(View.GONE);
        listWrapperView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        setupNewCommentsResult();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setupNewCommentsResult();
        finish();
        return super.onSupportNavigateUp();
    }

    private void setupNewCommentsResult() {
        Intent intent = new Intent();
        intent.putExtra("post", Parcels.wrap(presenter.getPost()));
        setResult(MainActivity.REQUEST_UPDATE_POST, intent);
    }
}
