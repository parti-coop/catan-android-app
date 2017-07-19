package xyz.parti.catan.ui.presenter;

import android.content.Context;

import com.google.gson.JsonNull;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.services.CommentsService;
import xyz.parti.catan.data.services.UpvotesService;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.ui.adapter.CommentFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.view.CommentView;
import xyz.parti.catan.ui.view.NewCommentForm;


public class CommentFeedPresenter extends BasePresenter<CommentFeedPresenter.View> implements CommentView.Presenter, NewCommentForm.Presenter {
    private final Post post;
    private final CommentsService commentsService;
    private final UpvotesService upvotesService;
    private CommentFeedRecyclerViewAdapter feedAdapter;
    private Disposable firstCommentsPublisher;
    private Disposable moreCommentsPublisher;
    private Disposable createCommentPublisher;
    private Disposable onClickLikePublisher;
    private Disposable onClickDestroyPublisher;

    public CommentFeedPresenter(Post post, SessionManager session) {
        this.post = post;
        commentsService = ServiceBuilder.createService(CommentsService.class, session);
        upvotesService = ServiceBuilder.createService(UpvotesService.class, session);
    }

    @Override
    public void attachView(CommentFeedPresenter.View view) {
        super.attachView(view);
    }

    @Override
    public void detachView() {
        feedAdapter = null;
        super.detachView();
    }

    private boolean isActive() {
        return getView() != null && feedAdapter != null;
    }

    public void setCommentFeedRecyclerViewAdapter(CommentFeedRecyclerViewAdapter feedAdapter) {
        this.feedAdapter = feedAdapter;
    }

    public void loadFirstComments() {
        if(feedAdapter == null) {
            CatanLog.d("moreCommentsPublisher : feedAdapter is null");
            return;
        }

        getView().showDemo();

        firstCommentsPublisher = getRxGuardian().subscribe(firstCommentsPublisher,
                commentsService.getComments(post.id),
                new Consumer<Response<Page<Comment>>>() {
                    @Override
                    public void accept(@NonNull Response<Page<Comment>> response) throws Exception {
                        if (!isActive()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            Page<Comment> page = response.body();
                            feedAdapter.clearAndAppendModels(page.items);
                            feedAdapter.setMoreDataAvailable(page.has_more_item);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            getView().reportError("AllComments load first error : " + response.code());
                            feedAdapter.setMoreDataAvailable(false);
                        }
                        feedAdapter.setLoadFinished();
                        getView().hideDemo();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if (!isActive()) {
                            return;
                        }

                        getView().reportError(error);
                        feedAdapter.setLoadFinished();
                        getView().hideDemo();
                    }
                });
    }

    public void loadMoreComments() {
        if(feedAdapter == null) {
            CatanLog.d("moreCommentsPublisher : feedAdapter is null");
            return;
        }

        Comment comment = feedAdapter.getFirstModel();
        if(comment == null) {
            CatanLog.d("moreCommentsPublisher : first comment is null");
            return;
        }
        feedAdapter.prependLoader();
        feedAdapter.notifyItemInserted(0);

        moreCommentsPublisher = getRxGuardian().subscribe(moreCommentsPublisher,
                commentsService.getComments(post.id, comment.id),
                new Consumer<Response<Page<Comment>>>() {
                    @Override
                    public void accept(@NonNull Response<Page<Comment>> response) throws Exception {
                        if (!isActive()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            //remove loading view
                            feedAdapter.removeFirstMoldelHolder();

                            Page<Comment> page = response.body();
                            List<Comment> result = page.items;
                            if (result.size() > 0) {
                                //add loaded data
                                feedAdapter.prependModels(page.items);
                                feedAdapter.setMoreDataAvailable(page.has_more_item);
                            } else {
                                //result size 0 means there is no more data available at server
                                feedAdapter.setMoreDataAvailable(false);
                            }
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            feedAdapter.setMoreDataAvailable(false);
                            getView().reportError("AllComments load more error : " + response.code());
                        }

                        feedAdapter.setLoadFinished();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if (!isActive()) {
                            return;
                        }

                        feedAdapter.setMoreDataAvailable(false);
                        feedAdapter.setLoadFinished();
                        getView().reportError(error);
                    }
                });
    }

    public void onClickCommentCreateButton(String body) {
        if(feedAdapter == null) {
            return;
        }

        feedAdapter.setLoadStarted();
        getView().setSendingCommentForm();

        createCommentPublisher = getRxGuardian().subscribe(createCommentPublisher,
                commentsService.createComment(post.id, body),
                new Consumer<Response<Comment>>() {
                    @Override
                    public void accept(@NonNull Response<Comment> response) throws Exception {
                        if (!isActive()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            Comment comment = response.body();
                            feedAdapter.appendModel(comment);
                            feedAdapter.notifyItemChanged(feedAdapter.getLastPosition() - 1);

                            if (!feedAdapter.isEmpty()) {
                                getView().showCommentList();
                            }
                            post.addComment(comment);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            getView().reportError("Create comment error : " + response.code());
                        }
                        getView().setCompletedCommentForm();
                        feedAdapter.setLoadFinished();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if (!isActive()) {
                            return;
                        }

                        getView().setCompletedCommentForm();
                        feedAdapter.setLoadFinished();

                        getView().reportError(error);
                    }
                });
    }

    public Post getPost() {
        return post;
    }

    @Override
    public void onClickLikeComment(final Post post, final Comment comment) {
        if(!isActive()) return;

        feedAdapter.changeModel(comment, Comment.PAYLOAD_IS_UPVOTED_BY_ME);
        Flowable<Response<JsonNull>> call =  ( comment.is_upvoted_by_me ?
                upvotesService.destroy("Comment", comment.id) : upvotesService.create("Comment", comment.id)
        );
        onClickLikePublisher = getRxGuardian().subscribe(onClickLikePublisher,
                call,
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(!isActive()) return;

                        if (response.isSuccessful()) {
                            post.toggleCommentUpvoting(comment);
                            changeComment(comment, Comment.PAYLOAD_IS_UPVOTED_BY_ME);
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
                        getView().reportError(error);
                    }
                });
    }

    private void changeComment(Comment comment, Object payload) {
        if(!isActive()) return;
        feedAdapter.changeModel(comment, payload);
    }

    @Override
    public void onClickNewComment(Post post, Comment comment) {
        getView().showNewCommentForm(comment);
    }

    @Override
    public void onClickDestroyComment(final Post post, final Comment comment) {
        onClickDestroyPublisher = getRxGuardian().subscribe(onClickDestroyPublisher,
                commentsService.destroyComment(comment.id),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        if(!isActive()) return;

                        if (response.isSuccessful()) {
                            post.removeComment(comment);
                            feedAdapter.removeModel(comment);
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
                        getView().reportError(error);
                    }
                });
    }

    public interface View {
        void setSendingCommentForm();
        void setCompletedCommentForm();
        void reportInfo(String message);
        void reportError(String message);
        void reportError(Throwable error);
        void showCommentList();
        void showNewCommentForm(Comment comment);
        Context getContext();

        void showDemo();
        void hideDemo();

    }
}
