package xyz.parti.catan.ui.presenter;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.APIHelper;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.FileSource;
import xyz.parti.catan.models.Option;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.models.User;
import xyz.parti.catan.services.FeedbacksService;
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.services.UpvotesService;
import xyz.parti.catan.services.VotingsService;
import xyz.parti.catan.sessions.SessionManager;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.task.DownloadFilesTask;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by dalikim on 2017. 5. 3..
 */

public class PostFeedPresenter {
    private final SessionManager session;
    private View view;
    private final PostsService postsService;
    private final VotingsService votingsService;
    private final UpvotesService upvotesService;
    private final FeedbacksService feedbacksService;
    private PostFeedRecyclerViewAdapter feedAdapter;

    public PostFeedPresenter(View view, SessionManager session) {
        this.view = view;
        this.session = session;
        postsService = ServiceGenerator.createService(PostsService.class, session);
        feedbacksService = ServiceGenerator.createService(FeedbacksService.class, session);
        votingsService = ServiceGenerator.createService(VotingsService.class, session);
        upvotesService = ServiceGenerator.createService(UpvotesService.class, session);
    }

    public void detachView() {
        view = null;
    }

    public void setPostFeedRecyclerViewAdapter(PostFeedRecyclerViewAdapter feedAdapter) {
        this.feedAdapter = feedAdapter;
    }

    public void loadFirstPosts() {
        if(feedAdapter == null) {
            return;
        }

        Call<Page<Post>> call = postsService.getDashBoardLastest();
        APIHelper.enqueueWithRetry(call, 5, new Callback<Page<Post>>() {
            @Override
            public void onResponse(Call<Page<Post>> call, Response<Page<Post>> response) {
                if(response.isSuccessful()){
                    feedAdapter.clearData();

                    Page<Post> page = response.body();
                    feedAdapter.appendModels(page.items);
                    feedAdapter.setMoreDataAvailable(page.has_more_item);
                }else{
                    ReportHelper.wtf(getApplicationContext(), "Losd first post error : " + response.code());
                }
                feedAdapter.setLoadFinished();
                view.setSwipeRefreshing(false);
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
                feedAdapter.setLoadFinished();
                view.setSwipeRefreshing(false);
            }
        });

    }

    public void loadMorePosts() {
        if(feedAdapter == null) {
            return;
        }
        //add loading progress view
        Post post = feedAdapter.getLastModel();
        if(post == null) {
            return;
        }

        feedAdapter.appendLoader();

        Call<Page<Post>> call = postsService.getDashboardAfter(post.id);
        call.enqueue(new Callback<Page<Post>>() {
            @Override
            public void onResponse(Call<Page<Post>> call, Response<Page<Post>> response) {
                if(response.isSuccessful()){
                    //remove loading view
                    feedAdapter.removeLastMoldelHolder();

                    Page<Post> page = response.body();
                    List<Post> result = page.items;
                    if(result.size() > 0){
                        //add loaded data
                        feedAdapter.appendModels(page.items);
                        feedAdapter.setMoreDataAvailable(page.has_more_item);
                    }else{
                        //result size 0 means there is no more data available at server
                        feedAdapter.setMoreDataAvailable(false);
                        feedAdapter.notifyDataSetChanged();
                        //telling adapter to stop calling loadFirstPosts more as no more server data available
                    }
                    feedAdapter.setLoadFinished();
                }else{
                    feedAdapter.setMoreDataAvailable(false);
                    feedAdapter.notifyDataSetChanged();
                    feedAdapter.setLoadFinished();

                    ReportHelper.wtf(getApplicationContext(), "Load More Response Error " + String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<Page<Post>> call, Throwable t) {
                feedAdapter.setMoreDataAvailable(false);
                feedAdapter.notifyDataSetChanged();
                feedAdapter.setLoadFinished();

                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    public void checkNewPosts() {
        if(feedAdapter == null) {
            return;
        }

        Date lastStrockedAt = null;
        if(!feedAdapter.isEmpty()) {
            InfinitableModelHolder<Post> firstPostHolder = feedAdapter.getFirstHolder();
            if(!firstPostHolder.isLoader()) {
                lastStrockedAt = firstPostHolder.getModel().last_stroked_at;
            }
        }
        if(lastStrockedAt == null) {
            return;
        }

        Call<JsonObject> call = postsService.hasUpdated(lastStrockedAt);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()) {
                    if (view.isVisibleNewPostsSignSlideUp()) {
                        return;
                    }

                    if (response.body().get("has_updated").getAsBoolean()) {
                        view.showNewPostsSignSlideUp();
                    }
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Check new post error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    public void changePost(final Post post) {
        feedAdapter.changeModel(post);
    }

    public void reloadPost(final Post post) {
        Call<Post> call = postsService.getPost(post.id);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()) {
                    post.survey = response.body().survey;
                    feedAdapter.changeModel(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Rebind survey error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    public void onClickLinkSource(String url) {
        view.showUrl(Uri.parse(url));
    }

    public void onClickDocFileSource(final Post post, final FileSource docFileSource) {
        view.downloadFile(post, docFileSource);
    }

    public void onClickImageFileSource(Post post) {
        view.showImageFileSource(post);
    }

    public void onClickLike(final Post post) {
        Call<JsonNull> call =  ( post.is_upvoted_by_me ?
                upvotesService.destroy("Post", post.id) : upvotesService.create("Post", post.id)
        );
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    post.toggleUpvoting();
                    changePost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Like error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    public void onClickMoreComments(Post post) {
        view.showAllComments(post);
    }

    public void onClickNewComment(Post post) {
        view.showNewCommentForm(post);
    }

    public void onClickSurveyOption(final Post post, Option option, boolean isChecked) {
        Call<JsonNull> call = feedbacksService.feedback(option.id, isChecked);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    reloadPost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Feedback error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }


    public void onClickPollAgree(final Post post) {
        final String newChoice = (post.poll.isAgreed() ? "unsure" : "agree");
        Call<JsonNull> call = votingsService.voting(post.poll.id, newChoice);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    post.poll.updateChoice(getCurrentUser(), newChoice);
                    changePost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Agree error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    public void onClickPollDisgree(final Post post) {
        final String newChoice = (post.poll.isDisagreed()  ? "unsure" : "disagree");
        Call<JsonNull> call = votingsService.voting(post.poll.id, newChoice);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    post.poll.updateChoice(getCurrentUser(), newChoice);
                    changePost(post);
                } else {
                    ReportHelper.wtf(getApplicationContext(), "Disagree error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(getApplicationContext(), t);
            }
        });
    }

    public void onPreDownloadDocFileSource(final DownloadFilesTask task) {
        view.showDownloadDocFileSourceProgress(task);
    }

    public void onProgressUpdateDownloadDocFileSource(int percentage, String message) {
        view.updateDownloadDocFileSourceProgress(percentage, message);
    }

    public void onPostDownloadDocFileSource() {
        view.hideDownloadDocFileSourceProgress();
    }

    public void onSuccessDownloadDocFileSource(File outputFile, String fileName) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(getExtension(fileName));
        view.showDownloadedFile(outputFile, mimeType);
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public void onFailDownloadDocFileSource(String message) {
        view.showSimpleMessage(message);
    }

    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    public PartiAccessToken getPartiAccessToken() {
        return session.getPartiAccessToken();
    }

    public interface View {
        void setSwipeRefreshing(boolean b);
        boolean isVisibleNewPostsSignSlideUp();
        void showNewPostsSignSlideUp();
        void showUrl(Uri parse);
        void downloadFile(Post post, FileSource docFileSource);
        void showImageFileSource(Post post);
        void showAllComments(Post post);
        void showNewCommentForm(Post post);
        void showDownloadDocFileSourceProgress(DownloadFilesTask task);
        void updateDownloadDocFileSourceProgress(int percentage, String message);
        void hideDownloadDocFileSourceProgress();
        void showDownloadedFile(File file, String mimeType);
        void showSimpleMessage(String message);
    }
}
