package xyz.parti.catan.ui.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.PushMessage;
import xyz.parti.catan.data.repository.NotificationsRepository;
import xyz.parti.catan.data.services.PostsService;
import xyz.parti.catan.helper.AppVersionHelper;
import xyz.parti.catan.ui.adapter.InfinitableModelHolder;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.task.AppVersionCheckTask;
import xyz.parti.catan.ui.task.ReceivablePushMessageCheckTask;

/**
 * Created by dalikim on 2017. 5. 3..
 */

public class PostFeedPresenter extends BasePostBindablePresenter<PostFeedPresenter.View> implements PostBinder.PostBindablePresenter {
    private SessionManager session;
    private final PostsService postsService;
    private PostFeedRecyclerViewAdapter feedAdapter;
    private Date lastStrockedAtOfNewPostCheck = null;
    private long lastLoadFirstPostsAtMillis = -1;

    private Disposable loadFirstPostsPublisher;
    private Disposable loadMorePostsPublisher;
    private Disposable checkNewPostsPublisher;
    private Disposable receivePushMessageForPostPublisher;
    private Disposable savePost;
    private AppVersionCheckTask appVersionCheckTask;
    private ReceivablePushMessageCheckTask receivablePushMessageCheckTask;

    public PostFeedPresenter(SessionManager session) {
        super(session);
        this.session = session;
        postsService = ServiceBuilder.createService(PostsService.class, session);
    }

    @Override
    public void attachView(PostFeedPresenter.View view) {
        super.attachView(view);
        appVersionCheckTask = new AppVersionCheckTask(new AppVersionHelper(getView().getContext()).getCurrentVerion(), getView().getContext());
        receivablePushMessageCheckTask = new ReceivablePushMessageCheckTask(getView().getContext(), session);
    }

    @Override
    public void detachView() {
        super.detachView();
        session = null;
        feedAdapter = null;
        if(appVersionCheckTask != null) {
            appVersionCheckTask.cancel();
        }
        if(receivablePushMessageCheckTask != null) {
            receivablePushMessageCheckTask.cancel();
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
                        Page<Post> page = response.body();
                        feedAdapter.clearAndAppendModels(page.items, 1);
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
            InfinitableModelHolder<Post> firstPostHolder = feedAdapter.getHolder(1);
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

    @Override
    public void changePost(Post post, Object playload) {
        if(!isActive()) return;

        feedAdapter.changeModel(post, playload);
    }

    @Override
    public void onClickCreatedAt(Post post) {
        getView().showPost(post);
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
        if(this.appVersionCheckTask == null) {
            return;
        }

        this.appVersionCheckTask.check(newVersion -> {
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

    public void receivePushMessage(int notificatiionId, PushMessage pushMessage) {
        if(!isActive()) {
            Log.d(Constants.TAG_TEST, "NOT active");
            return;
        }

        if(notificatiionId != -1) {
            Log.d(Constants.TAG_TEST, "MAIN notificatiionId" + notificatiionId);
            NotificationsRepository repository = new NotificationsRepository(getView().getContext().getSharedPreferences(Constants.PREF_NAME_NOTIFICATIONS, Context.MODE_PRIVATE));
            repository.destroy(notificatiionId);
        }

        if(pushMessage == null) {
            Log.d(Constants.TAG_TEST, "NULL pushMessage");
            return;
        }
        if(pushMessage.user_id != session.getCurrentUser().id) {
            Log.d(Constants.TAG_TEST, "ANOTHER USER");
            return;
        }

        if("post".equals(pushMessage.type) && pushMessage.param != null) {
            long postId = Long.parseLong(pushMessage.param);
            if(postId <= 0) {
                getView().showMessage(getView().getContext().getResources().getString(R.string.not_found_post));
                return;
            }
            receivePushMessageForPostPublisher = getRxGuardian().subscribe(receivePushMessageForPostPublisher, postsService.getPost(postId),
                    response -> {
                        if(response.isSuccessful()) {
                            getView().showPost(response.body());
                        } else {
                            getView().showMessage(getView().getContext().getResources().getString(R.string.not_found_post));
                        }
                    }, error -> getView().reportError(error)
            );
        } else if (pushMessage.url != null && !TextUtils.isEmpty(pushMessage.url)) {
            getView().showUrl(Uri.parse(pushMessage.url));
        }
    }

    public void checkReceivablePushMessage() {
        if(!isActive()) return;
        if(receivablePushMessageCheckTask == null) return;
        receivablePushMessageCheckTask.check();
    }

    public void showPostForm() {
        getView().showPostForm();
    }

    public void savePost(Parti parti, String body, List<SelectedImage> fileSourcesAttachmentImages) {
        if(!isActive()) return;

        getView().scrollToTop();
        feedAdapter.prependLoader();

        RequestBody partiIdReq = RequestBody.create(okhttp3.MultipartBody.FORM, parti.id.toString());
        RequestBody bodyReq = RequestBody.create(okhttp3.MultipartBody.FORM, body);

        List<MultipartBody.Part> filesReq = new ArrayList<>();
        for(SelectedImage image : fileSourcesAttachmentImages) {
            MultipartBody.Part request = buildFileSourceAttachmentRequest(image);
            if(request == null) continue;
            filesReq.add(request);
        }

        savePost = getRxGuardian().subscribe(savePost,
                postsService.createPost(partiIdReq, bodyReq, filesReq),
                response -> {
                    if(!isActive()) return;
                    feedAdapter.removeFirstMoldelHolder();
                    if(response.isSuccessful())  {
                        feedAdapter.prependModel(response.body());
                        getView().scrollToTop();
                    } else {
                        getView().reportError("savePost error : " + response.code());
                        getView().showPostForm(parti, body);
                    }
                }, error -> {
                    if(!isActive()) return;
                    feedAdapter.removeFirstMoldelHolder();
                    getView().reportError(error);
                    getView().showPostForm(parti, body);
                });
    }

    private MultipartBody.Part buildFileSourceAttachmentRequest(SelectedImage image) {
        if(image == null) return null;

        File file = new File(image.path);
        if(!file.exists()) {
            Log.d(Constants.TAG, "Not found file : " + image.path);
            return null;
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse(getView().getContext().getContentResolver().getType(image.uri)), file);
        return MultipartBody.Part.createFormData("post[file_sources_attributes][][attachment]", file.getName(), requestFile);
    }

    public interface View extends BasePostBindablePresenter.View {
        void stopAndEnableSwipeRefreshing();
        boolean isVisibleNewPostsSign();
        void showNewPostsSign();
        void ensureToPostListDemoIsGone();
        void showPostListDemo();
        void ensureExpendedAppBar();
        void showSettings();

        Context getContext();
        void showNewVersionMessage(String newVersion);
        void showMessage(String message);
        void showEmpty(boolean isError);
        void readyToRetry();
        void showPostForm();
        void showPostForm(Parti parti, String body);
        void scrollToTop();
    }
}
