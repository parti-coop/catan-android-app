package xyz.parti.catan.ui.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonNull;

import java.io.File;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import retrofit2.Response;
import xyz.parti.catan.Constants;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.services.FeedbacksService;
import xyz.parti.catan.data.services.PostsService;
import xyz.parti.catan.data.services.UpvotesService;
import xyz.parti.catan.data.services.VotingsService;
import xyz.parti.catan.helper.AppVersionHelper;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.helper.RxHelper;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.task.AppVersionCheckTask;
import xyz.parti.catan.ui.task.DownloadFilesTask;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by dalikim on 2017. 5. 3..
 */

public class PostFeedPresenter extends BasePresenter<PostFeedPresenter.View> {
    private SessionManager session;
    private final PostsService postsService;
    private final VotingsService votingsService;
    private final UpvotesService upvotesService;
    private final FeedbacksService feedbacksService;
    private PostFeedRecyclerViewAdapter feedAdapter;
    private Date lastStrockedAtOfNewPostCheck = null;
    private long lastLoadFirstPostsAtMillis = -1;

    private Disposable loadFirstPostsPublisher;
    private Disposable loadMorePostsPublisher;
    private Disposable checkNewPostsPublisher;
    private Disposable reloadPostPublisher;
    private Disposable onClickSurveyOptionPublisher;
    private Disposable onClickLikePublisher;
    private Disposable onClickPollAgreePublisher;
    private Disposable onClickPollDisgreePublisher;
    private AppVersionCheckTask appVersionCheckTaks;

    public PostFeedPresenter(SessionManager session) {
        this.session = session;
        postsService = ServiceBuilder.createService(PostsService.class, session);
        feedbacksService = ServiceBuilder.createService(FeedbacksService.class, session);
        votingsService = ServiceBuilder.createService(VotingsService.class, session);
        upvotesService = ServiceBuilder.createService(UpvotesService.class, session);
    }

    @Override
    public void attachView(PostFeedPresenter.View view) {
        super.attachView(view);
        appVersionCheckTaks = new AppVersionCheckTask(new AppVersionHelper(getView().getContext()).getCurrentVerion(), getView().getContext());
    }

    @Override
    public void detachView() {
        super.detachView();
        session = null;
        feedAdapter = null;
        if(appVersionCheckTaks != null) {
            appVersionCheckTaks.cancel();
        }
    }

    private boolean isActive() {
        return getView() != null && session != null && feedAdapter != null;
    }

    public void setPostFeedRecyclerViewAdapter(PostFeedRecyclerViewAdapter feedAdapter) {
        this.feedAdapter = feedAdapter;
    }

    public void loadFirstPosts() {
        checkViewAttached();
        if(feedAdapter == null) {
            return;
        }

        loadFirstPostsPublisher = getRxGuardian().subscribe(loadFirstPostsPublisher,
                postsService.getDashBoardLastest(),
                response -> {
                    /* SUCCESS */
                    if (!isActive()) {
                        return;
                    }

                    lastLoadFirstPostsAtMillis = System.currentTimeMillis();
                    getView().ensureToPostListDemoIsGone();
                    if (response.isSuccessful()) {
                        feedAdapter.clearData();

                        Page<Post> page = response.body();
                        feedAdapter.appendModels(page.items);
                        feedAdapter.setMoreDataAvailable(page.has_more_item);
                    } else {
                        feedAdapter.setMoreDataAvailable(false);
                        getView().reportError("Load first post error : " + response.code());
                    }
                    feedAdapter.setLoadFinished();
                    getView().ensureExpendedAppBar();
                    getView().stopAndEnableSwipeRefreshing();
                    
                    if(feedAdapter.getItemCount() <= 0) {
                        getView().showEmpty(!response.isSuccessful());
                    }
                }, error -> {
                    /* ERROR **/
                    if (!isActive()) {
                        return;
                    }
                    getView().reportError(error);

                    feedAdapter.setLoadFinished();
                    getView().ensureExpendedAppBar();
                    getView().stopAndEnableSwipeRefreshing();
                    getView().showEmpty(true);
                });
    }

    public void loadMorePosts() {
        checkViewAttached();
        if(feedAdapter == null) {
            return;
        }

        Post post = feedAdapter.getLastModel();
        if(post == null) {
            return;
        }
        feedAdapter.appendLoader();

        loadMorePostsPublisher = getRxGuardian().subscribe(loadMorePostsPublisher,
                postsService.getDashboardAfter(post.id),
                response -> {
                    /* SUCCESS **/
                    if(!isActive()) {
                        return;
                    }

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
                            //telling adapter to stop calling loadFirstPosts more as no more server data available
                        }
                    }else{
                        feedAdapter.setMoreDataAvailable(false);
                        getView().reportError("Load more post error : " + response.code());
                    }
                    feedAdapter.setLoadFinished();
                }, error -> {
                    /* ERROR **/
                    if(!isActive()) {
                        return;
                    }
                    feedAdapter.removeLastMoldelHolder();
                    feedAdapter.setLoadFinished();
                    feedAdapter.setMoreDataAvailable(false);
                    getView().reportError(error);
                });
    }

    public void checkNewPosts() {
        checkViewAttached();
        if(feedAdapter == null) {
            return;
        }

        if(getView().isVisibleNewPostsSign()) {
            return;
        }

        final Date lastStrockedAt = getLastStrockedAtForNewPostCheck();
        if (lastStrockedAt == null) return;

        RxHelper.unsubscribe(checkNewPostsPublisher);
        checkNewPostsPublisher = getRxGuardian().subscribe(checkNewPostsPublisher,
                postsService.hasUpdated(lastStrockedAt),
                response -> {
                    if(!isActive()) {
                        return;
                    }

                    if(response.isSuccessful()) {
                        if (getView().isVisibleNewPostsSign()) return;
                        if (!response.body().has_updated) return;
                        if (!lastStrockedAt.equals(getLastStrockedAtForNewPostCheck())) return;

                        PostFeedPresenter.this.lastStrockedAtOfNewPostCheck = response.body().last_stroked_at;
                        getView().showNewPostsSign();
                    } else {
                        getView().reportError("Check new post error : " + response.code());
                    }
                }, error -> {
                    if(!isActive()) {
                        return;
                    }

                    getView().reportError(error);
                });
    }

    @Nullable
    private Date getLastStrockedAtForNewPostCheck() {
        if(this.lastStrockedAtOfNewPostCheck != null) {
            return lastStrockedAtOfNewPostCheck;
        }

        Date lastStrockedAtOfFirstInPostList = null;
        if(!feedAdapter.isEmpty()) {
            InfinitableModelHolder<Post> firstPostHolder = feedAdapter.getFirstHolder();
            if(!firstPostHolder.isLoader()) {
                lastStrockedAtOfFirstInPostList = firstPostHolder.getModel().last_stroked_at;
            }
        }

        if(lastStrockedAtOfNewPostCheck == null) {
            return lastStrockedAtOfFirstInPostList;
        } else if(lastStrockedAtOfFirstInPostList == null) {
            return lastStrockedAtOfNewPostCheck;
        } else {
            return (lastStrockedAtOfNewPostCheck.getTime() > lastStrockedAtOfFirstInPostList.getTime() ? lastStrockedAtOfNewPostCheck : lastStrockedAtOfFirstInPostList);
        }
    }

    public void changePost(final Post post, Object playload) {
        feedAdapter.changeModel(post, playload);
    }

    private void reloadPostSurvey(final Post post) {
        RxHelper.unsubscribe(reloadPostPublisher);
        reloadPostPublisher = getRxGuardian().subscribe(reloadPostPublisher,
                postsService.getPost(post.id),
                response -> {
                    if(!isActive()) return;
                    if(response.isSuccessful()) {
                        post.survey = response.body().survey;
                        feedAdapter.changeModel(post, post.survey);
                    } else {
                        getView().reportError("Rebind survey error : " + response.code());
                    }
                }, error -> getView().reportError(error));
    }

    public void onClickLinkSource(Post post) {
        if(post.link_source == null) {
            return;
        }

        if(post.link_source.is_video && post.link_source.video_app_url != null) {
            getView().showVideo(Uri.parse(post.link_source.url), Uri.parse(post.link_source.video_app_url));
        } else {
            getView().showUrl(Uri.parse(post.link_source.url));
        }
    }

    public void onClickDocFileSource(final Post post, final FileSource docFileSource) {
        getView().downloadFile(post, docFileSource);
    }

    public void onClickImageFileSource(Post post) {
        getView().showImageFileSource(post);
    }

    public void onClickLike(final Post post) {
        Flowable<Response<JsonNull>> call =  ( post.is_upvoted_by_me ?
                upvotesService.destroy("Post", post.id) : upvotesService.create("Post", post.id)
        );
        onClickLikePublisher = getRxGuardian().subscribe(onClickLikePublisher,
                call,
                response -> {
                    if(response.isSuccessful()) {
                        post.toggleUpvoting();
                        changePost(post, Post.IS_UPVOTED_BY_ME);
                    } else {
                        ReportHelper.wtf(getApplicationContext(), "Like error : " + response.code());
                    }
                }, error -> ReportHelper.wtf(getApplicationContext(), error)
        );
    }

    public void onClickMoreComments(Post post) {
        getView().showAllComments(post);
    }

    public void onClickNewComment(Post post) {
        getView().showNewCommentForm(post);
    }

    public void onClickSurveyOption(final Post post, Option option, boolean isChecked) {
        onClickSurveyOptionPublisher = getRxGuardian().subscribe(onClickSurveyOptionPublisher,
                feedbacksService.feedback(option.id, isChecked),
                response -> {
                    if(response.isSuccessful()) {
                        reloadPostSurvey(post);
                    } else {
                        ReportHelper.wtf(getApplicationContext(), "Feedback error : " + response.code());
                    }
                }, error -> ReportHelper.wtf(getApplicationContext(), error)
        );
    }

    public void onClickPollAgree(final Post post) {
        final String newChoice = (post.poll.isAgreed() ? "unsure" : "agree");
        onClickPollAgreePublisher = getRxGuardian().subscribe(onClickPollAgreePublisher,
                votingsService.voting(post.poll.id, newChoice),
                response -> {
                    if(response.isSuccessful()) {
                        post.poll.updateChoice(getCurrentUser(), newChoice);
                        changePost(post, post.poll);
                    } else {
                        ReportHelper.wtf(getApplicationContext(), "Agree error : " + response.code());
                    }
                }, error -> ReportHelper.wtf(getApplicationContext(), error)
        );
    }

    public void onClickPollDisgree(final Post post) {
        final String newChoice = (post.poll.isDisagreed()  ? "unsure" : "disagree");
        onClickPollDisgreePublisher = getRxGuardian().subscribe(onClickPollDisgreePublisher,
                votingsService.voting(post.poll.id, newChoice),
                response -> {
                    if(response.isSuccessful()) {
                        post.poll.updateChoice(getCurrentUser(), newChoice);
                        changePost(post, post.poll);
                    } else {
                        ReportHelper.wtf(getApplicationContext(), "Disagree error : " + response.code());
                    }
                }, error -> ReportHelper.wtf(getApplicationContext(), error)
        );
    }

    public void onPreDownloadDocFileSource(final DownloadFilesTask task) {
        if(!isActive()) return;
        getView().showDownloadDocFileSourceProgress(task);
    }

    public void onProgressUpdateDownloadDocFileSource(int percentage, String message) {
        if(!isActive()) return;
        getView().updateDownloadDocFileSourceProgress(percentage, message);
    }

    public void onPostDownloadDocFileSource() {
        if(!isActive()) return;
        getView().hideDownloadDocFileSourceProgress();
    }

    public void onSuccessDownloadDocFileSource(File outputFile, String fileName) {
        if(!isActive()) return;
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(getExtension(fileName));
        getView().showDownloadedFile(outputFile, mimeType);
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public void onFailDownloadDocFileSource(String message) {
        if(!isActive()) return;
        getView().reportError(message);
    }

    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    public PartiAccessToken getPartiAccessToken() {
        return session.getPartiAccessToken();
    }

    public void onResume() {
        if(lastLoadFirstPostsAtMillis <= 0 || feedAdapter == null) {
            return;
        }

        if(feedAdapter.getItemCount() <= 0) {
            this.retryLoadingPost();
            return;
        }

        Post model = feedAdapter.getFirstModel();
        if(model == null) {
            return;
        }

        long reload_gap_mills = 10 * 60 * 1000;
        if(System.currentTimeMillis() - lastLoadFirstPostsAtMillis > reload_gap_mills) {
            feedAdapter.clearData();
            feedAdapter.notifyDataSetChanged();
            getView().showPostListDemo();
            loadFirstPosts();
        }
    }

    public void showSettings() {
        getView().showSettings();
    }

    public void checkAppVersion() {
        if(this.appVersionCheckTaks == null) {
            return;
        }

        this.appVersionCheckTaks.check(newVersion -> {
            if(isActive()) getView().showNewVersionMessage(newVersion);
        });
    }

    public void retryLoadingPost() {
        if(!isActive()) return;

        getView().readyToRetry();
        loadFirstPosts();
    }

    public void goToParties() {
        if(!isActive()) return;

        getView().showUrl(Uri.parse("https://parti.xyz/parties"));
    }

    public void preloadImage(String url) {
        if(url == null) {
            return;
        }
        Glide.with(getView().getContext())
                .load(url)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        //OK
                    }
                });
    }

    public interface View {
        void stopAndEnableSwipeRefreshing();
        boolean isVisibleNewPostsSign();
        void showNewPostsSign();
        void showUrl(Uri parse);
        void showVideo(Uri webUrl, Uri appUrl);
        void downloadFile(Post post, FileSource docFileSource);
        void showImageFileSource(Post post);
        void showAllComments(Post post);
        void showNewCommentForm(Post post);
        void showDownloadDocFileSourceProgress(DownloadFilesTask task);
        void updateDownloadDocFileSourceProgress(int percentage, String message);
        void hideDownloadDocFileSourceProgress();
        void showDownloadedFile(File file, String mimeType);
        void ensureToPostListDemoIsGone();
        void showPostListDemo();
        void ensureExpendedAppBar();
        void showSettings();
        Context getContext();
        void showNewVersionMessage(String newVersion);
        void reportError(Throwable error);
        void reportError(String message);
        void showEmpty(boolean isError);
        void readyToRetry();
    }
}
