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
        this.post = post;
        if(!isActive()) return;
        getView().changePost(post, playload);
    }

    @Override
    protected void changeSurvey(Post post) {
        this.post.survey = post.survey;
        if(!isActive()) return;
        getView().changeSurvey(post);
    }

    public Post getPost() {
        return post;
    }

    public interface View extends BasePostBindablePresenter.View {
        void changePost(Post post, Object payload);
        void changeSurvey(Post post);
    }
}
