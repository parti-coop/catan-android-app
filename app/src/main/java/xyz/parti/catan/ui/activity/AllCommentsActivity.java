package xyz.parti.catan.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.joanzapata.iconify.widget.IconButton;

import org.parceler.Parcel;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.adapter.LoadMoreRecyclerViewAdapter;
import xyz.parti.catan.ui.presenter.CommentFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class AllCommentsActivity extends BaseActivity implements CommentFeedPresenter.View {
    private CommentFeedRecyclerViewAdapter feedAdapter;
    private CommentFeedPresenter presenter;

    @BindView(R.id.allComments)
    RecyclerView allCommentsView;
    @BindView(R.id.allCommentsWrapper)
    ViewGroup allCommentsWrapperView;
    @BindView(R.id.noCommentsSign)
    ViewGroup noCommentsSignView;
    @BindView(R.id.commentFormEditText)
    EditText commentFormEditText;
    @BindView(R.id.commentFormSend)
    IconButton commentCreateButton;

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
            allCommentsWrapperView.setVisibility(View.GONE);
        }
        setUpComments(focusInput);
        setUpCommentForm();
    }

    private void setUpComments(boolean focusInput) {
        feedAdapter = new CommentFeedRecyclerViewAdapter(this);
        feedAdapter.setLoadMoreListener(
                new LoadMoreRecyclerViewAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        allCommentsView.post(new Runnable() {
                            @Override
                            public void run() {
                                presenter.loadMoreComments();
                            }
                        });
                    }
                });
        presenter.setCommentFeedRecyclerViewAdapter(feedAdapter);

        allCommentsView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        allCommentsView.setLayoutManager(layoutManager);
        allCommentsView.setAdapter(this.feedAdapter);
        allCommentsView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       final int oldBottom) {
                final int newHeight = bottom - top;
                final int oldHeight = oldBottom - oldTop;
                if(oldHeight != 0 && newHeight != oldHeight) {
                    allCommentsView.post(new Runnable() {
                        @Override
                        public void run() {
                            allCommentsView.scrollBy(0, oldHeight - newHeight);
                        }
                    });
                }
            }
        });
        presenter.loadFirstComments();

        if(focusInput) {
            commentFormEditText.post(new Runnable() {
                public void run() {
                    commentFormEditText.setFocusableInTouchMode(true);
                    commentFormEditText.requestFocus();
                }
            });
        }
    }

    private void setUpCommentForm() {
        disableCommentCreateButton();
        commentCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.onClickCommentCreateButton(commentFormEditText.getText().toString());
            }
        });

        commentFormEditText.addTextChangedListener(new TextWatcher() {
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
        commentCreateButton.setEnabled(true);
        commentCreateButton.setTextColor(ContextCompat.getColor(this, R.color.style_color_primary));
    }

    void disableCommentCreateButton() {
        commentCreateButton.setEnabled(false);
        commentCreateButton.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void setSendingCommentForm() {
        disableCommentCreateButton();
        commentFormEditText.setEnabled(false);
        commentCreateButton.setText("{fa-circle-o-notch spin}");
    }

    @Override
    public void setCompletedCommentForm() {
        allCommentsView.smoothScrollToPosition(feedAdapter.getLastPosition());

        commentFormEditText.setText(null);
        commentFormEditText.setEnabled(true);
        commentCreateButton.setText("{fa-send}");
        enableCommentCreateButton();
    }

    @Override
    public void showCommentList() {
        noCommentsSignView.setVisibility(View.GONE);
        allCommentsWrapperView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("post", Parcels.wrap(presenter.getPost()));
        setResult(MainActivity.REQUEST_NEW_COMMENT, intent);

        super.onBackPressed();
    }
}
