package xyz.parti.catan.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;
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
        setUpComments(focusInput, post, comment);
        setUpCommentForm();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpComments(boolean focusInput, Post post, Comment comment) {
        feedAdapter = new CommentFeedRecyclerViewAdapter(this, post);
        feedAdapter.setLoadMoreListener(() -> listRecyclerView.post(() -> presenter.loadMoreComments()));
        presenter.setCommentFeedRecyclerViewAdapter(feedAdapter);
        feedAdapter.setPresenter(presenter);

        listRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        listRecyclerView.setLayoutManager(layoutManager);
        listRecyclerView.setAdapter(this.feedAdapter);
        listRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final int newHeight = bottom - top;
            final int oldHeight = oldBottom - oldTop;
            if(oldHeight != 0 && newHeight != oldHeight) {
                listRecyclerView.post(() -> listRecyclerView.scrollBy(0, oldHeight - newHeight));
            }
        });
        presenter.loadFirstComments();

        if(focusInput) {
            newCommentInputEditText.post(() -> {
                newCommentInputEditText.setFocusableInTouchMode(true);
                newCommentInputEditText.requestFocus();
            });
            if(comment != null) {
                String defaultComment = String.format(Locale.getDefault(), "@%s ", comment.user.nickname);
                newCommentInputEditText.setText(defaultComment);
                newCommentInputEditText.setSelection(defaultComment.length());
            }
        }
    }

    private void setUpCommentForm() {
        disableCommentCreateButton();
        newCommentCreateButton.setOnClickListener(view -> presenter.onClickCommentCreateButton(newCommentInputEditText.getText().toString()));

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
        if(presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void setSendingCommentForm() {
        disableCommentCreateButton();
        newCommentInputEditText.setEnabled(false);
        newCommentCreateButton.setText("{fa-circle-o-notch spin}");
        if(this.feedAdapter.getItemCount() > 0) {
            listRecyclerView.postDelayed(() -> {
                listRecyclerView.smoothScrollToPosition(feedAdapter.getLastPosition());
            }, 100);
        }
    }

    @Override
    public void setCompletedCommentForm() {
        if(this.feedAdapter.getItemCount() > 0) {
            listRecyclerView.smoothScrollToPosition(feedAdapter.getLastPosition());
        }
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
    public Context getContext() {
        return this;
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
