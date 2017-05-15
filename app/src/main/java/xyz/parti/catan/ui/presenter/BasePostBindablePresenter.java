package xyz.parti.catan.ui.presenter;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.gson.JsonNull;

import java.io.File;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import retrofit2.Response;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.services.FeedbacksService;
import xyz.parti.catan.data.services.PostsService;
import xyz.parti.catan.data.services.UpvotesService;
import xyz.parti.catan.data.services.VotingsService;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.helper.RxHelper;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.task.DownloadFilesTask;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by dalikim on 2017. 5. 15..
 */

abstract class BasePostBindablePresenter<T extends BasePostBindablePresenter.View> extends BasePresenter<T> implements DownloadFilesTask.PostDownloadablePresenter, PostBinder.PostBindablePresenter {
    private final UpvotesService upvotesService;
    private final FeedbacksService feedbacksService;
    private final VotingsService votingsService;
    private final PostsService postsService;
    private SessionManager session;
    private Disposable reloadPostPublisher;

    private Disposable onClickLikePublisher;
    private Disposable onClickSurveyOptionPublisher;
    private Disposable onClickPollAgreePublisher;
    private Disposable onClickPollDisgreePublisher;

    BasePostBindablePresenter(SessionManager session) {
        this.session = session;
        upvotesService = ServiceBuilder.createService(UpvotesService.class, session);
        feedbacksService = ServiceBuilder.createService(FeedbacksService.class, session);
        votingsService = ServiceBuilder.createService(VotingsService.class, session);
        postsService = ServiceBuilder.createService(PostsService.class, session);
    }

    protected abstract void changePost(Post post, Object playload);
    protected abstract void changeSurvey(Post post);

    @Override
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

    @Override
    public void onClickNewComment(Post post) {
        getView().showNewCommentForm(post);
    }

    @Override

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

    @Override
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

    private void reloadPostSurvey(final Post post) {
        RxHelper.unsubscribe(reloadPostPublisher);
        reloadPostPublisher = getRxGuardian().subscribe(reloadPostPublisher,
                postsService.getPost(post.id),
                response -> {
                    if(getView() == null) return;
                    if(response.isSuccessful()) {
                        post.survey = response.body().survey;
                        changeSurvey(post);
                    } else {
                        getView().reportError("Rebind survey error : " + response.code());
                    }
                }, error -> getView().reportError(error));
    }

    @Override
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

    @Override
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

    @Override
    public void onClickMoreComments(Post post) {
        getView().showAllComments(post);
    }

    @Override
    public void onClickImageFileSource(Post post) {
        getView().showImageFileSource(post);
    }

    @Override
    public void onClickDocFileSource(final Post post, final FileSource docFileSource) {
        getView().downloadFile(post, docFileSource);
    }
    @Override
    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    @Override
    public void onPreDownloadDocFileSource(final DownloadFilesTask task) {
        if(getView() == null) return;
        getView().showDownloadDocFileSourceProgress(task);
    }

    @Override
    public void onProgressUpdateDownloadDocFileSource(int percentage, String message) {
        if(getView() == null) return;
        getView().updateDownloadDocFileSourceProgress(percentage, message);
    }

    @Override
    public void onPostDownloadDocFileSource() {
        if(getView() == null) return;
        getView().hideDownloadDocFileSourceProgress();
    }
    

    @Override
    public void onSuccessDownloadDocFileSource(File outputFile, String fileName) {
        if(getView() == null) return;
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(getExtension(fileName));
        getView().showDownloadedFile(outputFile, mimeType);
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    @Override
    public void onFailDownloadDocFileSource(String message) {
        if(getView() == null) return;
        getView().reportError(message);
    }

    @Override
    public PartiAccessToken getPartiAccessToken() {
        return session.getPartiAccessToken();
    }

    interface View {
        void showUrl(Uri url);
        void showVideo(Uri parse, Uri parse1);
        void showNewCommentForm(Post post);
        void reportError(String message);
        void reportError(Throwable error);
        void showAllComments(Post post);
        void showImageFileSource(Post post);
        void downloadFile(Post post, FileSource docFileSource);
        void showDownloadDocFileSourceProgress(DownloadFilesTask task);
        void updateDownloadDocFileSourceProgress(int percentage, String message);
        void hideDownloadDocFileSourceProgress();
        void showDownloadedFile(File outputFile, String mimeType);
    }
}
