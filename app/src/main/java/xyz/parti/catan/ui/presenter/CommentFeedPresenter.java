package xyz.parti.catan.ui.presenter;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.Constants;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.CommentsService;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.activity.AllCommentsActivity;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by dalikim on 2017. 5. 3..
 */

public class CommentFeedPresenter {
    private View view;
    private Post post;
    private final CommentsService commentsService;
    private CommentFeedRecyclerViewAdapter feedAdapter;

    public CommentFeedPresenter(View view, Post post, SessionManager session) {
        this.view = view;
        this.post = post;
        commentsService = ServiceGenerator.createService(CommentsService.class, session);
    }

    public void detachView() {
        view = null;
    }

    private boolean isActive() {
        return view != null;
    }

    public void setCommentFeedRecyclerViewAdapter(CommentFeedRecyclerViewAdapter feedAdapter) {
        this.feedAdapter = feedAdapter;
    }

    public void loadFirstComments() {
        if(feedAdapter == null) {
            Log.d(Constants.TAG, "loadMoreComments : feedAdapter is null");
            return;
        }

        Call<Page<Comment>> call = commentsService.getComments(post.id);
        call.enqueue(new Callback<Page<Comment>>() {
            @Override
            public void onResponse(Call<Page<Comment>> call, Response<Page<Comment>> response) {
                if(!isActive()) {
                    return;
                }

                if(response.isSuccessful()) {
                    Page<Comment> page = response.body();
                    feedAdapter.appendModels(page.items);
                    feedAdapter.setMoreDataAvailable(page.has_more_item);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "AllComments load first error : " + response.code());
                }
                feedAdapter.setLoadFinished();
            }

            @Override
            public void onFailure(Call<Page<Comment>> call, Throwable t) {
                if(!isActive()) {
                    return;
                }

                ReportHelper.wtf(getApplicationContext(), t);
                feedAdapter.setLoadFinished();
            }
        });


    }

    public void loadMoreComments() {
        if(feedAdapter == null) {
            Log.d(Constants.TAG, "loadMoreComments : feedAdapter is null");
            return;
        }

        Comment comment = feedAdapter.getFirstModel();
        if(comment == null) {
            Log.d(Constants.TAG, "loadMoreComments : first comment is null");
            return;
        }
        feedAdapter.prependLoader();
        feedAdapter.notifyItemInserted(0);

        Call<Page<Comment>> call = commentsService.getComments(post.id, comment.id);
        call.enqueue(new Callback<Page<Comment>>() {
            @Override
            public void onResponse(Call<Page<Comment>> call, Response<Page<Comment>> response) {
                if(!isActive()) {
                    return;
                }

                if(response.isSuccessful()) {
                    //remove loading view
                    feedAdapter.removeFirstMoldelHolder();

                    Page<Comment> page = response.body();
                    List<Comment> result = page.items;
                    if(result.size() > 0){
                        //add loaded data
                        feedAdapter.prependModels(page.items);
                        feedAdapter.setMoreDataAvailable(page.has_more_item);
                    }else{
                        //result size 0 means there is no more data available at server
                        feedAdapter.setMoreDataAvailable(false);
                    }
                    feedAdapter.setLoadFinished();
                } else {
                    feedAdapter.setMoreDataAvailable(false);
                    feedAdapter.notifyDataSetChanged();
                    feedAdapter.setLoadFinished();
                    ReportHelper.wtf(getApplicationContext(), "AllComments load more error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Page<Comment>> call, Throwable t) {
                if(!isActive()) {
                    return;
                }

                feedAdapter.setMoreDataAvailable(false);
                feedAdapter.notifyDataSetChanged();
                feedAdapter.setLoadFinished();

                ReportHelper.wtf(getApplicationContext(), t);
            }
        });

    }

    public void onClickCommentCreateButton(String body) {
        if(feedAdapter == null) {
            return;
        }

        feedAdapter.setLoadStarted();
        view.setSendingCommentForm();

        Call<Comment> call = commentsService.createComment(post.id, body);
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if(response.isSuccessful()) {
                    Comment comment = response.body();
                    feedAdapter.appendModel(comment);
                    feedAdapter.notifyItemChanged(feedAdapter.getLastPosition() - 1);

                    if(!feedAdapter.isEmpty()) {
                        view.showCommentList();
                    }
                    post.addComment(comment);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Create comment error : " + response.code());
                }
                view.setCompletedCommentForm();
                feedAdapter.setLoadFinished();
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                view.setCompletedCommentForm();
                feedAdapter.setLoadFinished();

                ReportHelper.wtf(getApplicationContext(), t);
            }
        });

    }

    public Post getPost() {
        return post;
    }

    public interface View {
        void setSendingCommentForm();
        void setCompletedCommentForm();
        void showCommentList();
    }
}
