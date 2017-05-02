package xyz.parti.catan.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.CommentsService;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.LoadMoreRecyclerViewAdapter;

/**
 * Created by dalikim on 2017. 4. 30..
 */

public class AllCommentsActivity extends BaseActivity {
    private SessionManager session;
    private List<InfinitableModelHolder<Comment>> comments;
    private CommentFeedRecyclerViewAdapter feedAdapter;
    private CommentsService commentsService;
    private Post post;

    @BindView(R.id.allComments)
    RecyclerView allCommentsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comments);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(AllCommentsActivity.this);

        session = new SessionManager(getApplicationContext());
        post = Parcels.unwrap(getIntent().getParcelableExtra("post"));
        
        commentsService = ServiceGenerator.createService(CommentsService.class, session);

        setUpComments();
    }

    private void setUpComments() {
        comments = new ArrayList<>();
        feedAdapter = new CommentFeedRecyclerViewAdapter(this, comments, session);
        feedAdapter.setLoadMoreListener(
                new LoadMoreRecyclerViewAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        allCommentsView.post(new Runnable() {
                            @Override
                            public void run() {
                                loadMoreComments();
                            }
                        });
                    }
                });

        allCommentsView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        allCommentsView.setLayoutManager(layoutManager);
        allCommentsView.setAdapter(feedAdapter);
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

        loadFirstComments();
    }

    private void loadFirstComments() {
        Call<Page<Comment>> call = commentsService.getComments(post.id);
        call.enqueue(new Callback<Page<Comment>>() {
            @Override
            public void onResponse(Call<Page<Comment>> call, Response<Page<Comment>> response) {
                if(response.isSuccessful()) {
                    Page<Comment> page = response.body();
                    comments.addAll(InfinitableModelHolder.from(page.items));
                    feedAdapter.setMoreDataAvailable(page.has_more_item);
                    feedAdapter.notifyAllDataChangedAndLoadFinished();
                } else {
                    ReportHelper.wtf(getApplicationContext(), "AllComments load first error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Page<Comment>> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    private void loadMoreComments() {
        InfinitableModelHolder<Comment> comment = comments.get(0);
        if(comment == null) {
            return;
        }
        comments.add(0, InfinitableModelHolder.<Comment>forLoader());
        feedAdapter.notifyItemInserted(0);

        Call<Page<Comment>> call = commentsService.getComments(post.id, comment.getModel().id);
        call.enqueue(new Callback<Page<Comment>>() {
            @Override
            public void onResponse(Call<Page<Comment>> call, Response<Page<Comment>> response) {
                if(response.isSuccessful()) {
                    //remove loading view
                    comments.remove(0);
                    feedAdapter.notifyItemRemoved(0);

                    Page<Comment> page = response.body();
                    List<Comment> result = page.items;
                    if(result.size() > 0){
                        //add loaded data
                        comments.addAll(0, InfinitableModelHolder.from(result));
                        feedAdapter.setMoreDataAvailable(page.has_more_item);
                        feedAdapter.notifyDataPrependedAndLoadFinished(page.items.size() + 1);
                    }else{
                        //result size 0 means there is no more data available at server
                        feedAdapter.setMoreDataAvailable(false);
                        //telling adapter to stop calling loadFirstPosts more as no more server data available
                        feedAdapter.notifyLoadFinished();
                    }
                } else {
                    feedAdapter.setMoreDataAvailable(false);
                    feedAdapter.notifyAllDataChangedAndLoadFinished();

                    ReportHelper.wtf(getApplicationContext(), "AllComments load more error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Page<Comment>> call, Throwable t) {
                feedAdapter.setMoreDataAvailable(false);
                feedAdapter.notifyAllDataChangedAndLoadFinished();

                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }
}
