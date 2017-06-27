package xyz.parti.catan.ui.presenter;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Response;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.services.CommentsService;
import xyz.parti.catan.ui.binder.CommentDiff;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.view.NewCommentForm;

/**
 * Created by dalikim on 2017. 5. 15..
 */

public class PostPresenter extends BasePostBindablePresenter<PostPresenter.View> implements PostBinder.PostBindablePresenter, NewCommentForm.Presenter {
    private Post post;
    private Disposable createCommentPublisher;
    private final CommentsService commentsService;

    public PostPresenter(Post post, SessionManager session) {
        super(session);
        this.post = post;
        commentsService = ServiceBuilder.createService(CommentsService.class, session);
    }

    @Override
    public void attachView(PostPresenter.View view) {
        super.attachView(view);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    private boolean isActive() {
        return getView() != null;
    }

    @Override
    public void changePost(Post post, Object payload) {
        if(!isActive()) return;

        this.post = post;
        if(this.post.sticky_comment != null && payload != null && payload instanceof CommentDiff) {
            CommentDiff commentDiff = (CommentDiff) payload;
           if(this.post.sticky_comment.id.equals(commentDiff.getComment().id)) {
               getView().changeStickyComment(post, commentDiff.getComment(), commentDiff.getPayload());
           }
        }

        getView().changePost(post, payload);
    }

    @Override
    public void onClickCreatedAt(Post post) {
        getView().showPost(post);
    }

    public Post getPost() {
        return post;
    }

    @Override
    public void onClickCommentCreateButton(String body) {
        if(!isActive()) return;
        getView().setSendingCommentForm();

        createCommentPublisher = getRxGuardian().subscribe(createCommentPublisher,
                commentsService.createComment(post.id, body),
                new Consumer<Response<Comment>>() {
                    @Override
                    public void accept(@NonNull Response<Comment> response) throws Exception {
                        if (!isActive()) return;
                        if (response.isSuccessful()) {
                            Comment comment = response.body();
                            post.addComment(comment);
                            getView().showNewComment(post);
                        } else if (response.code() == 403) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                        } else {
                            getView().reportError("Create comment error in post: " + response.code());
                        }
                        getView().setCompletedCommentForm();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        if (!isActive()) return;
                        getView().setCompletedCommentForm();
                        getView().reportError(error);
                    }
                });
    }

    public interface View extends BasePostBindablePresenter.View {
        void changePost(Post post, Object payload);

        void setSendingCommentForm();
        void setCompletedCommentForm();
        void showNewComment(Post post);
        void changeStickyComment(Post post, Comment comment, Object payload);
    }
}
