package xyz.parti.catan.ui.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import mehdi.sakout.fancybuttons.FancyButton;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Comment;
import xyz.parti.catan.data.model.FileSource;
import xyz.parti.catan.data.model.Group;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.data.model.PushMessage;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.helper.IntentHelper;
import xyz.parti.catan.helper.NetworkHelper;
import xyz.parti.catan.helper.StyleHelper;
import xyz.parti.catan.ui.adapter.LoadMoreRecyclerViewAdapter;
import xyz.parti.catan.ui.adapter.PostFeedRecyclerViewAdapter;
import xyz.parti.catan.ui.job.NewMessageCheckJob;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;
import xyz.parti.catan.ui.presenter.SelectedImage;
import xyz.parti.catan.ui.task.DownloadFilesTask;
import xyz.parti.catan.ui.view.GroupSectionDrawerItem;
import xyz.parti.catan.ui.view.NewOptionFormAlertBuilder;
import xyz.parti.catan.ui.view.NewPostSignAnimator;
import xyz.parti.catan.ui.view.PostFeedDrawerItem;
import xyz.parti.catan.ui.view.ShimmerLayout;

public class MainActivity extends BaseActivity implements PostFeedPresenter.View {
    public static final int REQUEST_UPDATE_POST = 1999;
    public static final int REQUEST_NEW_POST = 2001;

    @BindView(R.id.toolbar_app)
    Toolbar appToolbar;
    @BindView(R.id.recyclerview_post_list)
    RecyclerView postListRecyclerView;
    @BindView(R.id.layout_coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbarlayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.swipe_refresh_layout_post_list)
    SwipeRefreshLayout postListSwipeRefreshLayout;
    @BindView(R.id.layout_new_posts_sign)
    FrameLayout newPostsSignLayout;
    @BindView(R.id.button_new_posts_sign)
    FancyButton newPostsSignButton;
    @BindView(R.id.layout_post_list_demo)
    ShimmerLayout postListDemoLayout;
    @BindView(R.id.layout_no_posts_sign)
    RelativeLayout noPostSignLayout;
    @BindView(R.id.textview_no_posts_sign)
    TextView noPostSignTextView;
    @BindView(R.id.button_go_to_parties)
    FancyButton goToPartiesButton;
    @BindView(R.id.button_retry)
    FancyButton retryButton;
    @BindView(R.id.imageview_toolbar_dashboard_logo)
    ImageView toolbarDashboardLogoImageView;
    @BindView(R.id.layout_toolbar_parti)
    RelativeLayout toolbarPartiLayout;
    @BindView(R.id.draweeview_toolbar_parti_logo)
    SimpleDraweeView toolbarPartiLogoDraweeView;
    @BindView(R.id.textview_toolbar_parti_title)
    TextView toolbarPartiTitleTextView;
    @BindView(R.id.textview_toolbar_group_title)
    TextView toolbarGroupTitleTextView;
    @BindView(R.id.button_new_post)
    FloatingActionButton newPostButton;

    private NewPostSignAnimator newPostsSignAnimator;

    private ProgressDialog downloadProgressDialog;
    private PostFeedPresenter presenter;
    private AlertDialog optionDialog;

    private MenuItem messagesMenuItem;
    private ImageView messagesBellImageView;
    private ImageView messagesDotImageView;

    private Drawer drawer;
    private ShimmerLayout drawerDemoLayout;
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(new NetworkHelper(this).isValidNetwork()) {
            final SessionManager session = new SessionManager(this.getApplicationContext());
            session.checkLogin(new SessionManager.OnCheckListener() {
                @Override
                public void onLoggedIn() {
                    CatanLog.d("이미 로그인되어 있음");

                    unbinder = ButterKnife.bind(MainActivity.this);

                    presenter = new PostFeedPresenter(session);
                    presenter.attachView(MainActivity.this);
                    checkAppVersion();
                    setupToolbar();
                    setupFeed(session.getCurrentUser());

                    setupCheckNewPost();
                    setupDrawerBar();
                    setupSwipeRefresh();
                    setupFloatingNewPostButton();

                    receivePushMessageIntent(getIntent());
                    checkReceivablePushMessage();
                }

                @Override
                public void onLoggedOut() {
                    finish();
                }
            });
        }
    }

    private void setupFloatingNewPostButton() {
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.showPostForm();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(downloadProgressDialog != null) {
            downloadProgressDialog.dismiss();
        }
        if(optionDialog != null) {
            optionDialog.dismiss();
        }
        if(presenter != null) {
            presenter.detachView();
        }
        if(presenter != null) {
            presenter.unwatchNewPosts();
        }
        if(postListSwipeRefreshLayout != null) {
            postListSwipeRefreshLayout.setOnRefreshListener(null);
        }

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearMemoryCaches();

        if(unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(presenter != null) {
            presenter.refreshFeedIfNeed();
            presenter.watchNewPosts();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(presenter != null) {
            presenter.unwatchNewPosts();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NewMessageCheckJob.scheduleJob();
        ensureExpendedAppBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NewMessageCheckJob.cancelJob();
        ensureExpendedAppBar();
    }

    private void checkAppVersion() {
        presenter.checkAppVersion();
    }
    private void checkReceivablePushMessage() {
        presenter.checkReceivablePushMessage();
    }

    private void setupSwipeRefresh() {
        postListSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadFirstPosts();
            }
        });
        // Configure the refreshing colors
        postListSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void setupDrawerBar() {
        drawer = new DrawerBuilder()
                .withTranslucentStatusBar(false)
                .withActivity(this)
                .withToolbar(appToolbar)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        if(drawer.getDrawerItems().size() <= 0) {
                            presenter.reloadDrawer();
                        } else {
                            presenter.selectCurrentDrawerItem();
                        }
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        presenter.reloadDrawer();
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem item) {
                        if(item.getType() == R.id.drawer_item_dashbord) {
                            presenter.switchDashboardPostFeed(true);
                        } else if(item.getType() == R.id.drawer_item_parti) {
                            Object tagData = item.getTag();
                            if (tagData == null) return false;
                            if (!(tagData instanceof Parti)) return false;
                            presenter.switchPartiPostFeed((Parti) tagData);
                        }
                        drawer.closeDrawer();
                        return true;
                    }
                }).build();

        drawerDemoLayout = (ShimmerLayout) LayoutInflater.from(this).inflate(R.layout.drawer_demo, drawer.getSlider(), false);
        drawer.getSlider().addView(drawerDemoLayout);
        drawerDemoLayout.startLoading();
        setDrawerVerticalBottomPadding(drawer.getSlider(), R.dimen.default_space);
        presenter.loadDrawer();
    }

    private void setDrawerVerticalBottomPadding(View v, @DimenRes int id) {
        int verticalPadding = getResources().getDimensionPixelSize(id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            v.setPaddingRelative(0, 0, 0, verticalPadding);
        } else {
            v.setPadding(0, 0, 0, verticalPadding);
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        SessionManager session = new SessionManager(this.getApplicationContext());
        session.checkLogin(new SessionManager.OnCheckListener() {
            @Override
            public void onLoggedIn() {
                receivePushMessageIntent(intent);
            }

            @Override
            public void onLoggedOut() {
                finish();
            }
        });
    }

    private void receivePushMessageIntent(Intent intent) {
        if(intent == null) {
            return;
        }
        PushMessage pushMessage = Parcels.unwrap(intent.getParcelableExtra("pushMessage"));
        int notificatiionId = intent.getIntExtra("notificationId", Constants.NO_NOTIFICATION_ID);

        presenter.receivePushMessage(notificatiionId, pushMessage);
    }

    private void setupCheckNewPost() {
        newPostsSignAnimator = new NewPostSignAnimator(this.newPostsSignLayout);

        postListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newPostsSignAnimator != null) {
                    newPostsSignAnimator.hideDelayed(5000);
                }
            }
        });
        newPostsSignButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                if(newPostsSignAnimator != null) {
                    newPostsSignAnimator.hideImmediately();
                }
                postListSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        postListSwipeRefreshLayout.setRefreshing(true);
                        if(postListRecyclerView.computeVerticalScrollOffset() != 0) {
                            postListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                public void onScrollStateChanged(RecyclerView view, int state) {
                                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                                        view.removeOnScrollListener(this);
                                        appBarLayout.setExpanded(true, true);
                                    }
                                }
                            });
                            postListRecyclerView.smoothScrollToPosition(0);
                        } else {
                            appBarLayout.setExpanded(true, true);
                        }
                        presenter.loadFirstPosts();
                    }
                });
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(appToolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        appToolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    private void setupFeed(User currentUser) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.downloadProgressDialog = new ProgressDialog(this, R.style.AppProgressDialog);
        } else {
            this.downloadProgressDialog = new ProgressDialog(this);
        }

        PostFeedRecyclerViewAdapter feedAdapter = new PostFeedRecyclerViewAdapter(this, currentUser);
        feedAdapter.setPresenter(presenter);
        feedAdapter.setHasStableIds(true);
        feedAdapter.setLoadMoreListener(new LoadMoreRecyclerViewAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                postListRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        presenter.loadMorePosts();
                        //Calling loadMorePosts function in Runnable to fix the
                        // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling error
                    }
                });
            }
        });

        postListRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager recyclerViewLayout = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 2000;
            }
        };
        recyclerViewLayout.setInitialPrefetchItemCount(20);
        postListRecyclerView.setLayoutManager(recyclerViewLayout);
        postListRecyclerView.setAdapter(feedAdapter);
        postListRecyclerView.setItemViewCacheSize(50);
        postListRecyclerView.setDrawingCacheEnabled(true);
        postListRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        presenter.setPostFeedRecyclerViewAdapter(feedAdapter);
        presenter.loadFirstPosts();

        postListSwipeRefreshLayout.setEnabled(false);
        postListDemoLayout.startLoading();
        presenter.syncToolbarWithPostFeed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_UPDATE_POST:
                if(data == null) return;
                if(this.presenter == null) return;

                Post post = Parcels.unwrap(data.getParcelableExtra("post"));
                if(post == null) return;
                presenter.changePost(post);
                return;
            case REQUEST_NEW_POST:
                if(data == null) return;
                if(this.presenter == null) return;

                Parti parti = Parcels.unwrap(data.getParcelableExtra("parti"));
                String body = data.getStringExtra("body");
                ArrayList<SelectedImage> fileSourceAttachmentUris = Parcels.unwrap(data.getParcelableExtra("fileSourceAttachmentImages"));
                presenter.savePost(parti, body, fileSourceAttachmentUris);
                return;
            default:
        }
    }

    @Override
    public void showAllComments(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivityForResult(intent, REQUEST_UPDATE_POST);
    }

    @Override
    public void showNewCommentForm(Post post) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        intent.putExtra("focusInput", true);
        startActivityForResult(intent, REQUEST_UPDATE_POST);
    }

    @Override
    public void showNewCommentForm(Post post, Comment comment) {
        Intent intent = new Intent(this, AllCommentsActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        intent.putExtra("comment", Parcels.wrap(comment));
        intent.putExtra("focusInput", true);
        startActivityForResult(intent, REQUEST_UPDATE_POST);
    }

    @Override
    public void showEmpty(boolean isError) {
        postListDemoLayout.setVisibility(View.GONE);
        postListRecyclerView.setVisibility(View.GONE);
        noPostSignLayout.setVisibility(View.VISIBLE);
        if(newPostsSignAnimator != null) {
            newPostsSignAnimator.hideImmediately();
        }
        postListSwipeRefreshLayout.setRefreshing(false);

        if(isError) {
            retryButton.setVisibility(View.VISIBLE);
            noPostSignTextView.setText(getResources().getText(R.string.error_any));
            goToPartiesButton.setVisibility(View.GONE);
        } else {
            retryButton.setVisibility(View.GONE);
            noPostSignTextView.setText(getResources().getText(R.string.no_posts));
            goToPartiesButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showBlocked() {
        postListDemoLayout.setVisibility(View.GONE);
        postListRecyclerView.setVisibility(View.GONE);
        noPostSignLayout.setVisibility(View.VISIBLE);
        if(newPostsSignAnimator != null) {
            newPostsSignAnimator.hideImmediately();
        }
        postListSwipeRefreshLayout.setRefreshing(false);

        retryButton.setVisibility(View.GONE);
        noPostSignTextView.setText(getResources().getText(R.string.blocked_or_not_found_parti));
        goToPartiesButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_retry)
    public void onClickRetry() {
        presenter.retryLoadingPost();
    }

    @OnClick(R.id.button_go_to_parties)
    public void onClickGoToParties() {
        presenter.goToParties();
    }

    @Override
    public void readyToRetry() {
        postListDemoLayout.setVisibility(View.VISIBLE);
        postListRecyclerView.setVisibility(View.VISIBLE);
        noPostSignLayout.setVisibility(View.GONE);

        retryButton.setVisibility(View.GONE);
        goToPartiesButton.setVisibility(View.GONE);
    }

    @Override
    public void showPostForm() {
        Intent intent = new Intent(this, PostFormActivity.class);
        startActivityForResult(intent, REQUEST_NEW_POST);
    }

    @Override
    public void showPostForm(Parti parti) {
        Intent intent = new Intent(this, PostFormActivity.class);
        if(parti != null) {
            intent.putExtra("parti", Parcels.wrap(parti));
        }
        startActivityForResult(intent, REQUEST_NEW_POST);
    }

    @Override
    public void showPostForm(Parti parti, String body) {
        Intent intent = new Intent(this, PostFormActivity.class);
        intent.putExtra("parti", Parcels.wrap(parti));
        intent.putExtra("body", body);
        startActivityForResult(intent, REQUEST_NEW_POST);
    }

    @Override
    public void scrollToTop() {
        if(postListRecyclerView != null && postListRecyclerView.getVisibility() == View.VISIBLE) {
            postListRecyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void setupDrawerItems(User currentUser, TreeMap<Group, List<Parti>> joindedParties, final long currentPostFeedId) {
        final List<IDrawerItem> drawerItems = new ArrayList<>();
        PostFeedDrawerItem dashboardItem = PostFeedDrawerItem.forDashboard().withName(R.string.navigation_menu_dashboard).withLogo(currentUser.image_url).withIdentifier(Constants.POST_FEED_DASHBOARD);
        drawerItems.add(dashboardItem);

        List<Group> groups = Group.sortByTitle(joindedParties.keySet());
        for(Group group: groups) {
            SectionDrawerItem groupItem = new GroupSectionDrawerItem().withName(group.title).withDivider(false).withTextColorRes(R.color.material_drawer_header_selection_text);

            int indieCount = 1;
            if(group.isIndie()) {
                drawerItems.add(indieCount++, groupItem);
            } else {
                drawerItems.add(groupItem);
            }

            List<Parti> parties = Parti.sortByTitle(joindedParties.get(group));
            for(Parti parti :parties) {
                PostFeedDrawerItem item = PostFeedDrawerItem.forParti().withName(parti.title).withLogo(parti.logo_url);
                item.withTag(parti).withIdentifier(parti.id);
                if(group.isIndie()) {
                    drawerItems.add(indieCount++, item);
                } else {
                    drawerItems.add(item);
                }
            }
        }

        drawer.setItems(drawerItems);
        drawer.getSlider().post(new Runnable() {
            @Override
            public void run() {
                if(drawer.getCurrentSelection() != currentPostFeedId) {
                    drawer.setSelection(currentPostFeedId, false);
                }
            }
        });
    }

    @Override
    public void ensureToHideDrawerDemo() {
        drawerDemoLayout.setVisibility(View.GONE);
    }

    @Override
    public void markUnreadOrNotPostFeed(long postFeedId, boolean unread, boolean selected) {
        if(drawer == null) {
            return;
        }

        PostFeedDrawerItem item = (PostFeedDrawerItem) drawer.getDrawerItem(postFeedId);
        if(item == null) {
            return;
        }

        if(unread != item.isUnreadMarked()) {
            item.withUnreadMark(unread);
            drawer.updateItem(item);
        }

        if(selected != (drawer.getCurrentSelection() == postFeedId)) {
            presenter.selectCurrentDrawerItem();
        }
    }

    @Override
    public void changeDashboardToolbar() {
        toolbarPartiLayout.setVisibility(View.GONE);
        toolbarDashboardLogoImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void changePartiPostFeedToolbar(Parti parti) {
        toolbarPartiLayout.setVisibility(View.VISIBLE);
        toolbarPartiTitleTextView.setText(parti.title);
        if(parti.group != null && !parti.group.isIndie()) {
            toolbarGroupTitleTextView.setVisibility(View.VISIBLE);
            toolbarGroupTitleTextView.setText(parti.group.title);
        } else {
            toolbarGroupTitleTextView.setVisibility(View.GONE);
        }
        toolbarPartiLogoDraweeView.setImageURI(parti.logo_url);
        toolbarDashboardLogoImageView.setVisibility(View.GONE);
    }

    @Override
    public boolean canRefreshDrawer() {
        return drawer != null && (drawer.getDrawerItems().size() <= 0 || !drawer.isDrawerOpen());
    }

    @Override
    public void openDrawer() {
        if(drawer == null) return;
        drawer.openDrawer();
    }

    @Override
    public void selectDrawerItem(long currentPostFeedId) {
        if(drawer == null) return;
        if(drawer.getCurrentSelection() == currentPostFeedId) return;

        drawer.setSelection(currentPostFeedId, false);
    }

    @Override
    public void showProfile(User user) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.profile_url)));
    }

    @Override
    public void showDownloadDocFileSourceProgress(final DownloadFilesTask task) {
        downloadProgressDialog.setMessage("다운로드 중");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel(true);
            }
        });
        downloadProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                task.cancel(true);
            }
        });

        downloadProgressDialog.show();
    }

    @Override
    public void updateDownloadDocFileSourceProgress(int percentage, String message) {
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
        downloadProgressDialog.setProgress(percentage);
        downloadProgressDialog.setMessage(message);
    }

    @Override
    public void hideDownloadDocFileSourceProgress() {
        downloadProgressDialog.dismiss();
    }

    @Override
    public void downloadFile(final Post post, final FileSource docFileSource) {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        final DownloadFilesTask downloadTask = new DownloadFilesTask(presenter, post.id, docFileSource.id, docFileSource.name);
                        downloadTask.execute();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    }
                })
                .setRationaleMessage(R.string.doc_file_source_download_permission_rationale)
                .setDeniedMessage(R.string.doc_file_source_download_permission_denied)
                .setPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"})
                .check();
    }

    @Override
    public void showDownloadedFile(Uri uri, String mimeType) {
        CatanLog.d("Show File MimeType : " + mimeType);
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setDataAndType(uri, mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        List<ResolveInfo> resolvedInfoActivities = getPackageManager().queryIntentActivities(newIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolvedInfoActivities) {
            grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        try {
            startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            reportInfo(R.string.not_found_app_for_downloaded_file);
        }
    }

    @Override
    public void readyToShowPostFeed() {
        postListDemoLayout.setVisibility(View.GONE);
        postListRecyclerView.setVisibility(View.VISIBLE);
        postListRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                postListRecyclerView.scrollToPosition(0);
            }
        }, 100);
        noPostSignLayout.setVisibility(View.GONE);

        if(newPostsSignAnimator != null) {
            newPostsSignAnimator.hideImmediately();
            newPostsSignAnimator.setDisable(false);
        }
        postListSwipeRefreshLayout.setEnabled(true);
        postListSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showPostListDemo() {
        postListDemoLayout.setVisibility(View.VISIBLE);
        noPostSignLayout.setVisibility(View.GONE);

        if(newPostsSignAnimator != null) {
            newPostsSignAnimator.setDisable(true);
        }
        postListSwipeRefreshLayout.setEnabled(false);
        postListSwipeRefreshLayout.setRefreshing(false);
        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                appBarLayout.setExpanded(true);
            }
        });
    }

    @Override
    public void ensureExpendedAppBar() {
        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                appBarLayout.setExpanded(true, true);
            }
        });
    }

    @Override
    public void showPost(Post post) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivityForResult(intent, REQUEST_UPDATE_POST);
    }

    @Override
    public void showNewSurveyOptionDialog(final Post post) {
        optionDialog = NewOptionFormAlertBuilder.build(this, presenter, post);
        optionDialog.show();
    }

    @Override
    public void hideNewSurveyOptionDialog() {
        optionDialog.dismiss();
    }

    @Override
    public void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void showMessages() {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showNewVersionMessage(String newVersion) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, String.format(getResources().getString(R.string.new_version), newVersion), 30 * 1000)
                .setAction(R.string.ok,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new IntentHelper(MainActivity.this).startPlayStore(getPackageName());
                            }
                        });
        StyleHelper.forSnackbar(this, snackbar);
        snackbar.show();
    }

    @Override
    public void showImageFileSource(Post post) {
        Intent intent = new Intent(this, PostImagesViewActivity.class);
        intent.putExtra("post", Parcels.wrap(post));
        startActivity(intent);
    }

    @Override
    public void stopAndEnableSwipeRefreshing() {
        postListSwipeRefreshLayout.setEnabled(true);
        postListSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean isVisibleNewPostsSign() {
        return newPostsSignLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showNewPostsSign() {
        if(isVisibleNewPostsSign()) {
            newPostsSignAnimator.show();
        }
    }

    @Override
    public void showUrl(Uri url) {
        startActivity(new Intent(Intent.ACTION_VIEW, url));
    }

    @Override
    public void showVideo(Uri webUrl, @NonNull Uri appUrl) {
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, appUrl);
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            showUrl(webUrl);
        }
    }

    /*
        OptionsMenu
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        messagesMenuItem = menu.findItem(R.id.action_messages);
        messagesMenuItem.setActionView(R.layout.action_bar_notifitcation_icon);
        View messagesView = messagesMenuItem.getActionView();
        if(messagesView != null) {
            messagesBellImageView = (ImageView) messagesView.findViewById(R.id.imageview_messages_bell);
            messagesDotImageView = (ImageView) messagesView.findViewById(R.id.imageview_messages_dot);
            messagesBellImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.showMessages();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem profileItem = menu.findItem(R.id.action_profile);
        User user = presenter.getCurrentUser();
        if (user != null) {
            int size = getResources().getDimensionPixelSize(R.dimen.action_user_image);

            ImageHelper.getBitmap(this, Uri.parse(user.image_url), size, size, new ImageHelper.OnCompletedListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    profileItem.setIcon(new BitmapDrawable(getResources(), ImageHelper.getCircularBitmap(bitmap)));
                }

                @Override
                public void onFailure() {
                    Drawable defaultDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_account_circle_white_24dp);
                    Drawable wrappedDrawable = DrawableCompat.wrap(defaultDrawable);
                    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(MainActivity.this, R.color.brand_gray_dark));
                    profileItem.setIcon(defaultDrawable);
                }
            });
        }
        presenter.watchMessages();

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_messages:
                presenter.showMessages();
                return true;
            case R.id.action_profile:
                presenter.showProfile();
                return true;
            case R.id.action_settings:
                presenter.showSettings();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setReadMessagesStatus() {
        if(messagesBellImageView == null || messagesDotImageView == null) return;
        messagesBellImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notifications_none_white_24dp));
        messagesDotImageView.setVisibility(View.GONE);
    }

    @Override
    public void showNewPostButton() {
        newPostButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNewPostButton() {
        newPostButton.setVisibility(View.GONE);
    }

    @Override
    public void setUnreadMessagesStatus() {
        if(messagesBellImageView == null || messagesDotImageView == null) return;
        messagesBellImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notifications_white_24dp));
        messagesDotImageView.setVisibility(View.VISIBLE);
    }
}
