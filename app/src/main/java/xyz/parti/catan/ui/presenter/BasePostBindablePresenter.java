package xyz.parti.catan.ui.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import com.google.gson.JsonNull;

import java.io.File;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.services.FeedbacksService;
import xyz.parti.catan.data.services.OptionsService;
import xyz.parti.catan.data.services.PostsService;
import xyz.parti.catan.data.services.UpvotesService;
import xyz.parti.catan.data.services.VotingsService;
import xyz.parti.catan.ui.binder.CommentDiff;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.task.DownloadFilesTask;
import xyz.parti.catan.ui.view.CommentView;

/**
 * Created by dalikim on 2017. 5. 15..
 */

public abstract class BasePostBindablePresenter<T extends BasePostBindablePresenter.View> extends BasePresenter<T> implements DownloadFilesTask.PostDownloadablePresenter, PostBinder.PostBindablePresenter, CommentView.Presenter {
    private final UpvotesService upvotesService;
    private final FeedbacksService feedbacksService;
    private final OptionsService optionsService;
    private final VotingsService votingsService;
    private final PostsService postsService;
    private SessionManager session;
    private Disposable reloadPostPublisher;

    private Disposable onClickLikePublisher;
    private Disposable onClickSurveyOptionPublisher;
    private Disposable onClickPollAgreePublisher;
    private Disposable onClickPollDisgreePublisher;
    private Disposable newOptionPublisher;
    private Disposable reloadPostSurveyPublisher;

    BasePostBindablePresenter(SessionManager session) {
        this.session = session;
        upvotesService = ServiceBuilder.createService(UpvotesService.class, session);
        feedbacksService = ServiceBuilder.createService(FeedbacksService.class, session);
        votingsService = ServiceBuilder.createService(VotingsService.class, session);
        postsService = ServiceBuilder.createService(PostsService.class, session);
        optionsService = ServiceBuilder.createService(OptionsService.class, session);
    }

    protected abstract void changePost(Post post, Object payload);

    public void changePost(Post post) {
        changePost(post, null);
    }

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
    public void onClickNewComment(Post post, Comment comment) {
        getView().showNewCommentForm(post, comment);
    }

    @Override
    public void onClickLike(final Post post) {
        if(getView() == null) return;
        Flowable<Response<JsonNull>> call =  ( post.is_upvoted_by_me ?
                upvotesService.destroy("Post", post.id) : upvotesService.create("Post", post.id)
        );
        onClickLikePublisher = getRxGuardian().subscribe(onClickLikePublisher,
                call,
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            post.toggleUpvoting();
                            changePost(post, Post.IS_UPVOTED_BY_ME);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            getView().reportError("Like error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void onClickLikeComment(final Post post, final Comment comment) {
        if(getView() == null) return;
        Flowable<Response<JsonNull>> call =  ( comment.is_upvoted_by_me ?
                upvotesService.destroy("Comment", comment.id) : upvotesService.create("Comment", comment.id)
        );
        onClickLikePublisher = getRxGuardian().subscribe(onClickLikePublisher,
                call,
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            post.toggleCommentUpvoting(comment);
                            changePost(post, new CommentDiff(comment, Comment.IS_UPVOTED_BY_ME));
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_comment));
                        } else {
                            getView().reportError("Like error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void onClickSurveyOption(final Post post, Option option, boolean isChecked) {
        if(getView() == null) return;
        onClickSurveyOptionPublisher = getRxGuardian().subscribe(onClickSurveyOptionPublisher,
                feedbacksService.feedback(option.id, isChecked),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            reloadPostSurvey(post, null);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_option));
                        } else {
                            getView().reportError("Feedback error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    private void reloadPostSurvey(final Post post, final ReloadCallBack callback) {
        reloadPostSurveyPublisher = getRxGuardian().subscribe(reloadPostSurveyPublisher,
                postsService.getPost(post.id),
                new Consumer<Response<Post>>() {
                    @Override
                    public void accept(@NonNull Response<Post> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            post.survey = response.body().survey;
                            changePost(post, post.survey);
                            if(callback != null) callback.afterReload();
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void reloadPost(final Post post, final ReloadCallBack callback) {
        reloadPostPublisher = getRxGuardian().subscribe(reloadPostPublisher,
                postsService.getPost(post.id),
                new Consumer<Response<Post>>() {
                    @Override
                    public void accept(@NonNull Response<Post> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            changePost(post);
                            if(callback != null) callback.afterReload();
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void onClickPollAgree(final Post post) {
        if(getView() == null) return;
        final String newChoice = (post.poll.isAgreed() ? "unsure" : "agree");
        onClickPollAgreePublisher = getRxGuardian().subscribe(onClickPollAgreePublisher,
                votingsService.voting(post.poll.id, newChoice),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            post.poll.updateChoice(getCurrentUser(), newChoice);
                            changePost(post, post.poll);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            getView().reportError("Agree error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void onClickPollDisgree(final Post post) {
        if(getView() == null) return;
        final String newChoice = (post.poll.isDisagreed()  ? "unsure" : "disagree");
        onClickPollDisgreePublisher = getRxGuardian().subscribe(onClickPollDisgreePublisher,
                votingsService.voting(post.poll.id, newChoice),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            post.poll.updateChoice(getCurrentUser(), newChoice);
                            changePost(post, post.poll);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            getView().reportError("Disagree error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void onClickMoreComments(Post post) {
        if(getView() == null) return;
        getView().showAllComments(post);
    }

    @Override
    public void onClickImageFileSource(Post post) {
        if(getView() == null) return;
        getView().showImageFileSource(post);
    }

    @Override
    public void onClickDocFileSource(final Post post, final FileSource docFileSource) {
        if(getView() == null) return;
        getView().downloadFile(post, docFileSource);
    }

    @Override
    public void onClickNewOption(Post post) {
        if(getView() == null) return;
        getView().showNewSurveyOptionDialog(post);
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
        if(getView() == null) return;
        getView().hideDownloadDocFileSourceProgress();
    }
    

    @Override
    public void onSuccessDownloadDocFileSource(File outputFile, String fileName, String authorities) {
        if(getView() == null) return;
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(getExtension(fileName));
        Uri uri = FileProvider.getUriForFile(getView().getContext(), authorities, outputFile);
        getView().showDownloadedFile(uri, mimeType);
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    @Override
    public void onFailDownloadDocFileSource(String message) {
        if(getView() == null) return;
        getView().reportInfo(message);
    }

    @Override
    public PartiAccessToken getPartiAccessToken() {
        return session.getPartiAccessToken();
    }

    public void saveOption(final Post post, String text) {
        if(getView() == null) return;
        if(post.survey == null) return;
        newOptionPublisher = getRxGuardian().subscribe(newOptionPublisher,
                optionsService.create(post.survey.id, text),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(getView() == null) return;
                        if (response.isSuccessful()) {
                            reloadPostSurvey(post, new ReloadCallBack() {
                                @Override
                                public void afterReload() {
                                    getView().hideNewSurveyOptionDialog();
                                }
                            });
                        } else if (response.code() == 403) {
                            getView().hideNewSurveyOptionDialog();
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else {
                            getView().hideNewSurveyOptionDialog();
                            getView().reportError("New Option error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if(getView() == null) return;
                        getView().hideNewSurveyOptionDialog();
                        getView().reportError(error);
                    }
                });
    }

    interface View {
        void showUrl(Uri url);
        void showVideo(Uri parse, Uri parse1);
        void showNewCommentForm(Post post);
        void showNewCommentForm(Post post, Comment comment);
        void showAllComments(Post post);
        void showImageFileSource(Post post);
        void downloadFile(Post post, FileSource docFileSource);
        void showDownloadDocFileSourceProgress(DownloadFilesTask task);
        void updateDownloadDocFileSourceProgress(int percentage, String message);
        void hideDownloadDocFileSourceProgress();
        void showDownloadedFile(Uri uri, String mimeType);
        void showPost(Post post);
        void showNewSurveyOptionDialog(Post post);
        void hideNewSurveyOptionDialog();
        Context getContext();
        void reportError(String message);
        void reportError(Throwable error);
        void reportInfo(String string);
    }

    public interface ReloadCallBack {
        void afterReload();
    }
}
