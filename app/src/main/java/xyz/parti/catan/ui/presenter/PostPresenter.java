package xyz.parti.catan.ui.presenter;

import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.binder.PostBinder;

/**
 * Created by dalikim on 2017. 5. 15..
 */

public class PostPresenter extends BasePostBindablePresenter<PostPresenter.View> implements PostBinder.PostBindablePresenter {
    private Post post;

    public PostPresenter(Post post, SessionManager session) {
        super(session);
        this.post = post;
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
    public void changePost(Post post, Object playload) {
        if(!isActive()) return;
        this.post = post;
        getView().changePost(post, playload);
    }

    @Override
    public void onClickCreatedAt(Post post) {
        getView().showPost(post);
    }

    public Post getPost() {
        return post;
    }

    public interface View extends BasePostBindablePresenter.View {
        void changePost(Post post, Object payload);
    }
}
