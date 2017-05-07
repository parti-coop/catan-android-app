package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.joanzapata.iconify.widget.IconButton;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.adapter.LoadMoreRecyclerViewAdapter;
import xyz.parti.catan.ui.presenter.CommentFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class AllCommentsActivity extends BaseActivity implements CommentFeedPresenter.View {
    private CommentFeedRecyclerViewAdapter feedAdapter;
    private CommentFeedPresenter presenter;

    @BindView(R.id.recyclerview_list)
    RecyclerView listRecyclerView;
    @BindView(R.id.layout_list_wrapper)
    ViewGroup listWrapperView;
    @BindView(R.id.layout_no_comments_sign)
    ViewGroup noCommentsSignView;
    @BindView(R.id.edittext_new_comment_input)
    EditText newCommentInputEditText;
    @BindView(R.id.button_new_comment_create)
    IconButton newCommentCreateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comments);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(AllCommentsActivity.this);

        SessionManager session = new SessionManager(getApplicationContext());
        Post post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        boolean focusInput = getIntent().getBooleanExtra("focusInput", false);

        presenter = new CommentFeedPresenter(this, post, session);

        if(post.comments_count > 0) {
            showCommentList();
        } else {
            noCommentsSignView.setVisibility(View.VISIBLE);
            listWrapperView.setVisibility(View.GONE);
        }
        setUpComments(focusInput);
        setUpCommentForm();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUpComments(boolean focusInput) {
        feedAdapter = new CommentFeedRecyclerViewAdapter(this);
        feedAdapter.setLoadMoreListener(
                new LoadMoreRecyclerViewAdapter.OnLoadMoreListener() {
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

        listRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        listRecyclerView.setLayoutManager(layoutManager);
        listRecyclerView.setAdapter(this.feedAdapter);
        listRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       final int oldBottom) {
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
            newCommentInputEditText.post(new Runnable() {
                public void run() {
                    newCommentInputEditText.setFocusableInTouchMode(true);
                    newCommentInputEditText.requestFocus();
                }
            });
        }
    }

    private void setUpCommentForm() {
        disableCommentCreateButton();
        newCommentCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickCommentCreateButton(newCommentInputEditText.getText().toString());
            }
        });

        newCommentInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(TextUtils.isEmpty(charSequence)) {
                    disableCommentCreateButton();
                } else {
                    enableCommentCreateButton();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    void enableCommentCreateButton() {
        newCommentCreateButton.setEnabled(true);
        newCommentCreateButton.setTextColor(ContextCompat.getColor(this, R.color.style_color_primary));
    }

    void disableCommentCreateButton() {
        newCommentCreateButton.setEnabled(false);
        newCommentCreateButton.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void setSendingCommentForm() {
        disableCommentCreateButton();
        newCommentInputEditText.setEnabled(false);
        newCommentCreateButton.setText("{fa-circle-o-notch spin}");
    }

    @Override
    public void setCompletedCommentForm() {
        listRecyclerView.smoothScrollToPosition(feedAdapter.getLastPosition());

        newCommentInputEditText.setText(null);
        newCommentInputEditText.setEnabled(true);
        newCommentCreateButton.setText("{fa-send}");
        enableCommentCreateButton();
    }

    @Override
    public void showCommentList() {
        noCommentsSignView.setVisibility(View.GONE);
        listWrapperView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        setUpNewCommentsResult();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setUpNewCommentsResult();
        finish();
        return super.onSupportNavigateUp();
    }

    private void setUpNewCommentsResult() {
        Intent intent = new Intent();
        intent.putExtra("post", Parcels.wrap(presenter.getPost()));
        setResult(MainActivity.REQUEST_NEW_COMMENT, intent);
    }
}
