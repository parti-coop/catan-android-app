package xyz.parti.catan.ui.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.dao.MessagesStatusDAO;
import xyz.parti.catan.data.dao.PartiDAO;
import xyz.parti.catan.data.dao.ReadPostFeedDAO;
import xyz.parti.catan.data.model.Group;
import xyz.parti.catan.data.model.MessagesStatus;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.PushMessage;
import xyz.parti.catan.data.model.ReadPostFeed;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.preference.JoinedPartiesPreference;
import xyz.parti.catan.data.preference.LastPostFeedPreference;
import xyz.parti.catan.data.preference.NotificationsPreference;
import xyz.parti.catan.data.services.PartiesService;
import xyz.parti.catan.data.services.PostsService;
import xyz.parti.catan.helper.AppVersionHelper;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.task.AppVersionCheckTask;
import xyz.parti.catan.ui.task.ReceivablePushMessageCheckTask;


public class PostFeedPresenter extends BasePostBindablePresenter<PostFeedPresenter.View> implements PostBinder.PostBindablePresenter {
    private final DatabaseReference partiesFirebaseRoot;
    private final List<DatabaseReference> listenPartiFireBases = new ArrayList<>();

    private SessionManager session;
    private final PostsService postsService;
    private final PartiesService partiesService;
    private PostFeedRecyclerViewAdapter feedAdapter;
    private long lastLoadFirstPostsAtMillis = -1;

    private Disposable loadFirstPostsPublisher;
    private Disposable loadMorePostsPublisher;
    private Disposable receivePushMessageForPostPublisher;
    private Disposable receivePushMessageForCommentPublisher;
    private Disposable savePost;
    private Disposable loadDrawer;
    private Disposable reloadDrawer;
    private AppVersionCheckTask appVersionCheckTask;
    private ReceivablePushMessageCheckTask receivablePushMessageCheckTask;
    private List<Parti> joindedParties = new ArrayList<>();
    private long currentPostFeedId = Constants.POST_FEED_DASHBOARD;
    private Parti currentParti;
    private ValueEventListener newPostListener;
    private LastPostFeedPreference lastPostFeedPreference;
    private final Realm realm;
    private final PartiDAO partiDAO;
    private final ReadPostFeedDAO readPostFeedDAO;
    private final MessagesStatusDAO messagesStatusDAO;
    private PartiDAO.ChangeListener partiDAOListener;

    public PostFeedPresenter(SessionManager session) {
        super(session);
        this.session = session;
        postsService = ServiceBuilder.createService(PostsService.class, session);
        partiesService = ServiceBuilder.createService(PartiesService.class, session);
        partiesFirebaseRoot = FirebaseDatabase.getInstance().getReference(BuildConfig.FIREBASE_DATABASE_PARTIES);

        realm = Realm.getDefaultInstance();
        partiDAO = new PartiDAO(realm);
        messagesStatusDAO = new MessagesStatusDAO(realm);
        readPostFeedDAO = new ReadPostFeedDAO(realm);
    }

    @Override
    public void attachView(PostFeedPresenter.View view) {
        super.attachView(view);
        appVersionCheckTask = new AppVersionCheckTask(AppVersionHelper.getCurrentVerion(getView().getContext()), getView().getContext());
        receivablePushMessageCheckTask = new ReceivablePushMessageCheckTask(getView().getContext(), session);
        lastPostFeedPreference = new LastPostFeedPreference(getView().getContext());
        currentPostFeedId = lastPostFeedPreference.fetch();
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
        if(partiDAO != null) {
            partiDAO.unwatchAll();
        }
        if(messagesStatusDAO != null) {
            messagesStatusDAO.unwatchAll();
        }
        if(realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    private boolean isActive() {
        return getView() != null && session != null && feedAdapter != null;
    }

    public void setPostFeedRecyclerViewAdapter(PostFeedRecyclerViewAdapter feedAdapter) {
        this.feedAdapter = feedAdapter;
        disableNewPost();
        playWithPostFeed(new OnLoadPostFeed() {
            @Override
            public void onDashbard() {
                enableNewPost();
            }

            @Override
            public void onParti(Parti parti) {
                if(parti.is_postable) {
                    enableNewPost();
                } else {
                    disableNewPost();
                }
            }
        });
    }

    public void loadFirstPosts() {
        if(!isActive()) return;

        Flowable<Response<Page<Post>>> boardLastest =
            this.currentPostFeedId == Constants.POST_FEED_DASHBOARD ? postsService.getDashBoardLastest() : partiesService.getPostsLastest(currentPostFeedId);
        loadFirstPostsPublisher = getRxGuardian().subscribe(loadFirstPostsPublisher,
                boardLastest,
                new Consumer<Response<Page<Post>>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Response<Page<Post>> response) throws Exception {
                        /* SUCCESS */
                        if (!isActive()) {
                            return;
                        }

                        lastLoadFirstPostsAtMillis = System.currentTimeMillis();
                        getView().readyToShowPostFeed();
                        if (response.isSuccessful()) {
                            Page<Post> page = response.body();
                            feedAdapter.clearAndAppendModels(page.items, 1);
                            feedAdapter.setMoreDataAvailable(page.has_more_item);

                            ReadPostFeed readPostFeed = readPostFeedDAO.forPartiOrDashboard(currentPostFeedId);
                            if (page.last_stroked_at != null) {
                                readPostFeed.lastReadAt = page.last_stroked_at;
                                readPostFeedDAO.save(readPostFeed);
                            }
                            markUnreadOrNotPostFeed(readPostFeed);
                            if(readPostFeed.isUnread() && currentPostFeedId == readPostFeed.postFeedId && !getView().isVisibleNewPostsSign()) {
                                getView().showNewPostsSign();
                            }
                        } else if (response.code() == 410 || response.code() == 403) {
                            feedAdapter.setMoreDataAvailable(false);
                            feedAdapter.clear();
                        } else {
                            feedAdapter.setMoreDataAvailable(false);
                            getView().reportError("Load first post error : " + response.code());
                        }
                        feedAdapter.setLoadFinished();
                        getView().ensureExpendedAppBar();
                        getView().stopAndEnableSwipeRefreshing();

                        if (feedAdapter.getModelItemCount() <= 0) {
                            if(response.code() == 403 || response.code() == 410) {
                                getView().showBlocked();
                            } else {
                                getView().showEmpty(!response.isSuccessful());
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                        /* ERROR **/
                        if (!isActive()) {
                            return;
                        }
                        getView().reportError(error);

                        feedAdapter.setLoadFinished();
                        getView().ensureExpendedAppBar();
                        getView().stopAndEnableSwipeRefreshing();
                        getView().showEmpty(true);
                    }
                });
    }

    public void loadMorePosts() {
        if(!isActive()) return;

        Post post = feedAdapter.getLastModel();
        if(post == null) {
            return;
        }
        feedAdapter.appendLoader();

        Flowable<Response<Page<Post>>> boardLastest =
                this.currentPostFeedId == Constants.POST_FEED_DASHBOARD ? postsService.getDashboardAfter(post.id) : partiesService.getPostsAfter(currentPostFeedId, post.id);

        loadMorePostsPublisher = getRxGuardian().subscribe(loadMorePostsPublisher,
                boardLastest,
                new Consumer<Response<Page<Post>>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Response<Page<Post>> response) throws Exception {
                        /* SUCCESS **/
                        if (!isActive()) {
                            return;
                        }

                        if (response.isSuccessful()) {
                            //remove loading view
                            feedAdapter.removeLastMoldelHolder();

                            Page<Post> page = response.body();
                            List<Post> result = page.items;
                            if (result.size() > 0) {
                                //add loaded data
                                feedAdapter.appendModels(page.items);
                                feedAdapter.setMoreDataAvailable(page.has_more_item);
                            } else {
                                //result size 0 means there is no more data available at server
                                feedAdapter.setMoreDataAvailable(false);
                                //telling adapter to stop calling loadFirstPosts more as no more server data available
                            }
                        } else if (response.code() == 410 || response.code() == 403) {
                            feedAdapter.setMoreDataAvailable(false);
                            feedAdapter.clear();
                        } else {
                            feedAdapter.setMoreDataAvailable(false);
                            getView().reportError("Load more post error : " + response.code());
                        }

                        feedAdapter.setLoadFinished();
                        if (feedAdapter.getModelItemCount() <= 0) {
                            if(response.code() == 403 || response.code() == 410) {
                                getView().showBlocked();
                            } else {
                                getView().showEmpty(!response.isSuccessful());
                            }
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                        /* ERROR **/
                        if (!isActive()) {
                            return;
                        }
                        feedAdapter.removeLastMoldelHolder();
                        feedAdapter.setLoadFinished();
                        feedAdapter.setMoreDataAvailable(false);
                        getView().reportError(error);
                    }
                });
    }

    @Override
    public void changePost(Post post, Object payload) {
        if(!isActive()) return;

        feedAdapter.changeModel(post, payload);
    }

    @Override
    public void onClickCreatedAt(Post post) {
        getView().showPost(post);
    }

    public void refreshFeedIfNeed() {
        if(lastLoadFirstPostsAtMillis <= 0 || feedAdapter == null) {
            return;
        }

        if(feedAdapter.getModelItemCount() <= 0) {
            this.retryLoadingPost();
            return;
        }

        Post model = feedAdapter.getFirstModel();
        if(model == null) {
            return;
        }

        long reload_gap_mills = 30 * 60 * 1000;
        if(System.currentTimeMillis() - lastLoadFirstPostsAtMillis > reload_gap_mills) {
            refreshPosts();
        }
    }

    private void refreshPosts() {
        clearPostsAndShowDemo();
        loadFirstPosts();
    }

    private void clearPostsAndShowDemo() {
        feedAdapter.clearAndAppendPostLineForm();
        feedAdapter.notifyDataSetChanged();
        getView().showPostListDemo();
    }

    public void showSettings() {
        getView().showSettings();
    }

    public void checkAppVersion() {
        if(this.appVersionCheckTask == null) {
            return;
        }

        this.appVersionCheckTask.check(new AppVersionCheckTask.NewVersionAction() {
            @Override
            public void run(String newVersion) {
                if (isActive()) getView().showNewVersionMessage(newVersion);
            }
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

    public void receivePushMessage(int notificatiionId, PushMessage pushMessage) {
        if(!isActive()) {
            CatanLog.d("NOT active");
            return;
        }

        if(notificatiionId == Constants.NO_NOTIFICATION_ID) {
            return;
        }

        CatanLog.d("MAIN notificatiionId " + notificatiionId);
        NotificationsPreference repository = new NotificationsPreference(getView().getContext());
        repository.destroy(notificatiionId);

        if(pushMessage == null) {
            CatanLog.d("NULL pushMessage and reload");
            showMessages();
            return;
        }

        if(session.getCurrentUser() == null || pushMessage.user_id != session.getCurrentUser().id) {
            CatanLog.d("ANOTHER USER");
            switchDashboardPostFeed(true);
            return;
        }

        messagesStatusDAO.saveLocalReadMessageIdIfNew(pushMessage.user_id, pushMessage.id);

        if("post".equals(pushMessage.type) && pushMessage.param != null) {
            long postId = Long.parseLong(pushMessage.param);
            if(postId <= 0) {
                getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                return;
            }
            receivePushMessageForPostPublisher = getRxGuardian().subscribe(receivePushMessageForPostPublisher, postsService.getPost(postId),
                    new Consumer<Response<Post>>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Response<Post> response) throws Exception {
                            if (response.isSuccessful()) {
                                getView().showPost(response.body());
                            } else if (response.code() == 403) {
                                getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                            } else {
                                getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                            getView().reportError(error);
                        }
                    });
        } else if("comment".equals(pushMessage.type) && pushMessage.param != null) {
            long commentId = Long.parseLong(pushMessage.param);
            if(commentId <= 0) {
                getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                return;
            }
            receivePushMessageForCommentPublisher = getRxGuardian().subscribe(receivePushMessageForCommentPublisher, postsService.getPostByStickyCommentId(commentId),
                    new Consumer<Response<Post>>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Response<Post> response) throws Exception {
                            if (response.isSuccessful()) {
                                getView().showPost(response.body());
                            } else if (response.code() == 403) {
                                getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_post));
                            } else {
                                getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_post));
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                            getView().reportError(error);
                        }
                    });
        } else if (pushMessage.url != null && !TextUtils.isEmpty(pushMessage.url)) {
            getView().showUrl(Uri.parse(pushMessage.url));
        }
    }

    public void checkReceivablePushMessage() {
        if(!isActive()) return;
        if(receivablePushMessageCheckTask == null) return;
        receivablePushMessageCheckTask.check();
    }

    public void showProfile() {
        if(!isActive()) return;

        getView().showProfile(getCurrentUser());
    }

    public void syncToolbarWithPostFeed() {
        if(!isActive()) return;

        playWithPostFeed(new PostFeedPresenter.OnLoadPostFeed() {
            @Override
            public void onDashbard() {
                getView().changeDashboardToolbar();
            }

            @Override
            public void onParti(Parti parti) {
                getView().changePartiPostFeedToolbar(parti);
            }
        });
    }

    public void watchMessages() {
        if(!isActive()) return;

        MessagesStatusDAO.ChangeListener messagesStatusChangeListener = new MessagesStatusDAO.ChangeListener() {
            @Override
            public void onInit(MessagesStatus messagesStatus) {
                updateMessagesStatus(messagesStatus);
            }

            @Override
            public void onChange(MessagesStatus messagesStatus) {
                updateMessagesStatus(messagesStatus);
            }

            private void updateMessagesStatus(MessagesStatus messagesStatus) {
                if (!isActive()) return;
                if (messagesStatus.hasUnread()) {
                    getView().setUnreadMessagesStatus();
                } else {
                    getView().setReadMessagesStatus();
                }
            }
        };
        messagesStatusDAO.watch(session.getCurrentUser(), messagesStatusChangeListener);
    }

    interface OnLoadPostFeed {
        void onDashbard();
        void onParti(Parti parti);
    }

    private void playWithPostFeed(final OnLoadPostFeed callback) {
        if(currentPostFeedId == Constants.POST_FEED_DASHBOARD) {
            callback.onDashbard();
            return;
        }
        if(currentParti != null && currentParti.id == currentPostFeedId) {
            callback.onParti(currentParti);
            return;
        }
        getRxGuardian().subscribeSimultaneously(partiesService.getParti(currentPostFeedId),
                new Consumer<Response<Parti>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Response<Parti> response) throws Exception {
                        if(!isActive()) return;
                        if(response.isSuccessful()) {
                            Parti parti = response.body();
                            callback.onParti(parti);
                            currentParti = parti;
                        } else if (response.code() == 410) {
                            getView().reportInfo(getView().getContext().getResources().getString(R.string.not_found_parti));
                        } else {
                            getView().reportError("fetch parti error : " + response.code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                        getView().reportError(error);
                    }
                });
    }

    public void showMessages() {
        if(!isActive()) return;
        getView().showMessages();
    }

    public void showPostForm() {
        if(!isActive()) return;

        playWithPostFeed(new OnLoadPostFeed() {
            @Override
            public void onDashbard() {
                getView().showPostForm();
            }

            @Override
            public void onParti(Parti parti) {
                getView().showPostForm(parti);
            }
        });
    }

    public void savePost(final Parti parti, final String body, List<SelectedImage> fileSourcesAttachmentImages) {
        if(!isActive()) return;

        final boolean isCurrentParti = (currentPostFeedId == Constants.POST_FEED_DASHBOARD || parti.id.equals(currentPostFeedId));
        if(isCurrentParti) {
            getView().scrollToTop();
            feedAdapter.addLoader(1);
        } else {
            switchPartiPostFeed(parti, false, true);
        }

        RequestBody partiIdReq = RequestBody.create(okhttp3.MultipartBody.FORM, parti.id.toString());
        RequestBody bodyReq = RequestBody.create(okhttp3.MultipartBody.FORM, body);
        RequestBody isHtmlBodyReq = RequestBody.create(okhttp3.MultipartBody.FORM, "false");

        List<MultipartBody.Part> filesReq = new ArrayList<>();
        for(SelectedImage image : fileSourcesAttachmentImages) {
            MultipartBody.Part request = buildFileSourceAttachmentRequest(image);
            if(request == null) continue;
            filesReq.add(request);
        }

        savePost = getRxGuardian().subscribe(savePost,
                postsService.create(partiIdReq, bodyReq, isHtmlBodyReq, filesReq),
                new Consumer<Response<Post>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Response<Post> response) throws Exception {
                        if (!isActive()) return;

                        if(isCurrentParti) {
                            feedAdapter.removeMoldelHolderAt(1);
                            if (response.isSuccessful()) {
                                feedAdapter.addModel(1, response.body());
                                getView().scrollToTop();
                            } else if (response.code() == 403) {
                                getView().reportInfo(getView().getContext().getResources().getString(R.string.blocked_or_not_found_parti));
                            } else {
                                getView().reportError("savePost error : " + response.code());
                                getView().showPostForm(parti, body);
                            }
                        } else {
                            loadFirstPosts();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                        if (!isActive()) return;
                        feedAdapter.removeFirstMoldelHolder();
                        getView().reportError(error);
                        getView().showPostForm(parti, body);
                    }
                });
    }

    private MultipartBody.Part buildFileSourceAttachmentRequest(SelectedImage image) {
        if(image == null) return null;

        File file = new File(image.path);
        if(!file.exists()) {
            CatanLog.d("Not found file : " + image.path);
            return null;
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse(getView().getContext().getContentResolver().getType(image.uri)), file);
        return MultipartBody.Part.createFormData("post[file_sources_attributes][][attachment]", file.getName(), requestFile);
    }

    public void loadDrawer() {
        if(!isActive()) return;

        if(lastPostFeedPreference.isNewbie()) {
            getView().openDrawer();
            lastPostFeedPreference.save(currentPostFeedId);
        }  else if(!needToUpdateDrawer()) {
            getView().ensureToHideDrawerDemo();
            ensuerInitDrawer();
            return;
        }

        loadDrawer = getRxGuardian().subscribe(loadDrawer,
            partiesService.getMyJoined(),
            new Consumer<Response<Parti[]>>() {
                @Override
                public void accept(@io.reactivex.annotations.NonNull final Response<Parti[]> response) throws Exception {
                    if (!isActive()) return;

                    if (response.isSuccessful()) {
                        final List<Parti> list = new ArrayList<>(Arrays.asList(response.body()));
                        partiDAO.save(list, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                setDrawerIsUpToDate();
                                ensuerInitDrawer();
                            }
                        });
                        readPostFeedDAO.init(list);
                    } else {
                        getView().ensureToHideDrawerDemo();
                        watchNewPosts();
                    }
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                    getView().reportError(error);
                    getView().ensureToHideDrawerDemo();
                }
            });
    }

    public void reloadDrawer() {
        if(!isActive()) return;

        reloadDrawer = getRxGuardian().subscribe(reloadDrawer,
                partiesService.getMyJoinedPartiesChangedAt(),
                new Consumer<Response<Date>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull final Response<Date> response) throws Exception {
                        if (!isActive()) return;
                        if (response.isSuccessful()) {
                            if(response.body() != null) {
                                setMyJoinedPartiesChangedAt(response.body().getTime());
                            }
                            if(needToUpdateDrawer()) {
                                loadDrawer();
                                return;
                            }
                        }
                        getView().ensureToHideDrawerDemo();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable error) throws Exception {
                        getView().reportError(error);
                        getView().ensureToHideDrawerDemo();
                    }
                });
    }

    private boolean needToUpdateDrawer() {
        return new JoinedPartiesPreference(getView().getContext()).needToUpgrade();
    }

    private void setDrawerIsUpToDate() {
        new JoinedPartiesPreference(getView().getContext()).sync();
    }

    private void setMyJoinedPartiesChangedAt(long timestamp) {
        new JoinedPartiesPreference(getView().getContext()).saveChangedAt(timestamp);
    }

    private void refreshDrawerPending() {
        new JoinedPartiesPreference(getView().getContext()).reset();
    }

    private void ensuerInitDrawer() {
        if(partiDAOListener != null) return;

        partiDAOListener = new PartiDAO.ChangeListener() {
            @Override
            public void onChange(List<Parti> list) {
                if (!isActive()) return;

                if (!getView().canRefreshDrawer()) {
                    refreshDrawerPending();
                    return;
                }
                joindedParties.clear();
                joindedParties.addAll(list);

                if(session.getCurrentUser() != null) {
                    getView().setupDrawerItems(session.getCurrentUser(), getGroupList(joindedParties), PostFeedPresenter.this.currentPostFeedId);
                }
                getView().ensureToHideDrawerDemo();
                watchNewPosts();
            }
        };
        partiDAO.watchAll(partiDAOListener);
    }

    public void selectCurrentDrawerItem() {
        if(!isActive()) return;

        getView().selectDrawerItem(currentPostFeedId);
    }

    public void watchNewPosts() {
        if(newPostListener != null) {
            for(DatabaseReference partiFirebase : listenPartiFireBases) {
                partiFirebase.removeEventListener(newPostListener);
            }
        }

        newPostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getKey() == null) return;
                long postFeedId = Long.parseLong(dataSnapshot.getKey());
                if(!isJoinedParti(postFeedId)) {
                    readPostFeedDAO.destroyIfExist(postFeedId);
                    return;
                }

                ReadPostFeed readPostFeed = readPostFeedDAO.forPartiOrDashboard(postFeedId);

                Long lastStrokedBy = dataSnapshot.child("last_stroked_by").getValue(Long.class);
                if(getCurrentUser() == null || getCurrentUser().id.equals(lastStrokedBy)) return;

                Long lastStrokedSecondTime = dataSnapshot.child("last_stroked_at").getValue(Long.class);
                if(lastStrokedSecondTime == null) {
                    lastStrokedSecondTime = (long) -1;
                }
                readPostFeedDAO.updateLastStrokedAtSeconds(readPostFeed, lastStrokedSecondTime);
                markUnreadOrNotPostFeed(readPostFeed);

                if(readPostFeed.isUnread()) {
                    if(currentPostFeedId == postFeedId && !getView().isVisibleNewPostsSign()) {
                        getView().showNewPostsSign();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                CatanLog.e(databaseError.getMessage(), databaseError.toException());
            }
        };

        for(Parti parti : this.joindedParties) {
            DatabaseReference partiFirebase = partiesFirebaseRoot.child(String.valueOf(parti.id));
            partiFirebase.addValueEventListener(newPostListener);
            listenPartiFireBases.add(partiFirebase);
        }
    }

    private boolean isJoinedParti(long currentId) {
        for(Parti parti : this.joindedParties) {
            if(parti.id == currentId) {
                return true;
            }
        }
        return false;
    }

    private void markUnreadOrNotPostFeed(ReadPostFeed readPostFeed) {
        if(!isActive()) return;

        getView().markUnreadOrNotPostFeed(readPostFeed.postFeedId, readPostFeed.isUnread(), readPostFeed.postFeedId == currentPostFeedId);
        if(!readPostFeed.isDashboard()) {
            getView().markUnreadOrNotPostFeed(Constants.POST_FEED_DASHBOARD, readPostFeedDAO.forDashboard().isUnread(), Constants.POST_FEED_DASHBOARD == currentPostFeedId);
        }
    }

    public void unwatchNewPosts() {
        if(newPostListener != null) {
            for(DatabaseReference partiFirebase : listenPartiFireBases) {
                partiFirebase.removeEventListener(newPostListener);
            }
        }
        listenPartiFireBases.clear();
    }

    @NonNull
    private TreeMap<Group, List<Parti>> getGroupList(List<Parti> parties) {
        TreeMap<Group, List<Parti>> result = new TreeMap<>();
        for(Parti parti : parties) {
            List<Parti> items = result.get(parti.group);
            if(items == null) {
                items = new ArrayList<>();
            }
            items.add(parti);
            result.put(parti.group, items);
        }
        return result;
    }

    public void switchDashboardPostFeed(boolean force) {
        if(!isActive()) return;

        currentPostFeedId = Constants.POST_FEED_DASHBOARD;
        currentParti = null;
        lastPostFeedPreference.save(currentPostFeedId);
        getView().changeDashboardToolbar();
        enableNewPost();

        if(force || currentPostFeedId != Constants.POST_FEED_DASHBOARD) {
            refreshPosts();
        }
    }

    public void switchPartiPostFeed(Parti parti) {
        switchPartiPostFeed(parti, true, true);
    }

    private void enableNewPost() {
        if(!isActive()) return;

        showNewPostLineForm();
        getView().showNewPostButton();
    }

    private void showNewPostLineForm() {
        feedAdapter.showNewPostLineFormView();
    }

    private void disableNewPost() {
        if(!isActive()) return;

        hideNewPostLineForm();
        getView().hideNewPostButton();
    }

    private void hideNewPostLineForm() {
        feedAdapter.hideNewPostLineFormView();
    }

    private void switchPartiPostFeed(Parti parti, boolean needToLoadPost, boolean force) {
        if(!isActive()) return;

        currentPostFeedId = parti.id;
        currentParti = parti;
        lastPostFeedPreference.save(currentPostFeedId);
        getView().changePartiPostFeedToolbar(parti);
        if(parti.is_postable) {
            enableNewPost();
        } else {
            disableNewPost();
        }
        if(force || !parti.id.equals(currentPostFeedId)) {
            if (needToLoadPost) {
                refreshPosts();
            } else {
                clearPostsAndShowDemo();
            }
        }
    }

    public interface View extends BasePostBindablePresenter.View {
        void stopAndEnableSwipeRefreshing();
        boolean isVisibleNewPostsSign();
        void showNewPostsSign();
        void readyToShowPostFeed();
        void showPostListDemo();
        void ensureExpendedAppBar();
        void showSettings();
        void showMessages();

        Context getContext();
        void showNewVersionMessage(String newVersion);
        void reportInfo(String message);
        void showEmpty(boolean isError);
        void showBlocked();
        void readyToRetry();
        void showPostForm();
        void showPostForm(Parti parti);
        void showPostForm(Parti parti, String body);
        void scrollToTop();

        void setupDrawerItems(User currentUser, TreeMap<Group, List<Parti>> joindedParties, long currentPartiId);
        void ensureToHideDrawerDemo();
        boolean canRefreshDrawer();
        void openDrawer();
        void selectDrawerItem(long currentPostFeedId);

        void markUnreadOrNotPostFeed(long partiId, boolean unread, boolean isSelected);

        void changeDashboardToolbar();
        void changePartiPostFeedToolbar(Parti parti);

        void showProfile(User user);

        void setUnreadMessagesStatus();
        void setReadMessagesStatus();

        void showNewPostButton();
        void hideNewPostButton();
    }
}
