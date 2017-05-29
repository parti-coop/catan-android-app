package xyz.parti.catan.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.HeaderAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.internal.model.SelectedItemCollection;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import mehdi.sakout.fancybuttons.FancyButton;
import xyz.parti.catan.Constants;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Group;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.ui.adapter.PostFormGroupItem;
import xyz.parti.catan.ui.adapter.PostFormImageItem;
import xyz.parti.catan.ui.adapter.PostFormPartiItem;
import xyz.parti.catan.ui.presenter.PostFormPresenter;
import xyz.parti.catan.ui.presenter.SelectedImage;
import xyz.parti.catan.ui.view.ProgressToggler;
import xyz.parti.catan.ui.view.SizeFilter;

/**
 * Created by dalikim on 2017. 5. 23..
 */

public class PostFormActivity extends BaseActivity implements PostFormPresenter.View {
    private static final String TAG_PARTI_CHOICE = "xyz.parti.catan.PartiChoiceDialog";
    private static final int REQUEST_CODE_CHOOSE = 3000;
    private PostFormPresenter presenter;
    PartiChoiceFragment partiChoiceFragment;

    @BindView(R.id.textview_parti_title)
    TextView partiTitleTextView;
    @BindView(R.id.imageview_parti_logo)
    ImageView partiLogoImageView;
    @BindView(R.id.edittext_body)
    EditText editTextVew;
    @BindView(R.id.recyclerview_preview_images)
    RecyclerView previewImagesRecyclerView;
    @BindView(R.id.layout_preview_images)
    LinearLayout previewImagesLayout;
    @BindView(R.id.button_save)
    FancyButton saveButton;

    private FastItemAdapter<PostFormImageItem> previewImagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_form);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SessionManager session = new SessionManager(this);
        presenter = new PostFormPresenter(session);
        presenter.attachView(PostFormActivity.this);

        setUpForm();
        setUpPreviewImages();
    }

    private void setUpPreviewImages() {
        LinearLayoutManager recyclerViewLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        previewImagesRecyclerView.setLayoutManager(recyclerViewLayout);

        previewImagesAdapter = new FastItemAdapter<>();
        //just add an `EventHook` to your `FastAdapter` by implementing either a `ClickEventHook`, `LongClickEventHook`, `TouchEventHook`, `CustomEventHook`
        previewImagesAdapter.withItemEvent(new ClickEventHook<PostFormImageItem>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof PostFormImageItem.ViewHolder) {
                    return ((PostFormImageItem.ViewHolder) viewHolder).removeTextView;
                }
                return null;
            }

            @Override
            public void onClick(View v, int position, FastAdapter<PostFormImageItem> fastAdapter, PostFormImageItem item) {
                if(presenter == null) return;
                presenter.removeImage(item.getUrl());
            }
        });

        previewImagesRecyclerView.setAdapter(previewImagesAdapter);
    }

    private void setUpForm() {
        if(getIntent() != null) {
            String body = getIntent().getStringExtra("body");
            if(body != null) {
                editTextVew.setText(body);
            }
            Parti parti = Parcels.unwrap(getIntent().getParcelableExtra("parti"));
            if(parti != null) {
                presenter.setDefaultParti(parti);
            } else {
                showPartiChoiceDialog();
            }
        } else {
            showPartiChoiceDialog();
        }
    }

    @Override
    protected void onDestroy() {
        if(presenter != null) {
            presenter.detachView();
        }
        if(previewImagesAdapter != null) {
            previewImagesAdapter = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(presenter != null) {
            presenter.showPartiChoiceIfNeed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE:
                if(data == null) return;
                if(presenter == null) return;

                presenter.resetImages(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
                break;
            default:
                break;
        }
    }

    private void showPartiChoiceDialog() {
        if(partiChoiceFragment == null || partiChoiceFragment.getDialog() == null) {
            partiChoiceFragment = new PartiChoiceFragment();
            partiChoiceFragment.attachPresenter(presenter);
            partiChoiceFragment.show(getSupportFragmentManager(), TAG_PARTI_CHOICE);
        } else {
            partiChoiceFragment.getDialog().show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void hidePartiChoice() {
        if(partiChoiceFragment == null) return;
        partiChoiceFragment.getDialog().hide();
    }

    @Override
    public void cancelNewPost() {
        finish();
    }

    @Override
    public void setParti(Parti parti) {
        partiTitleTextView.setText(parti.title);
        new ImageHelper(partiLogoImageView).loadInto(parti.logo_url);
    }

    @Override
    public void showPartiChoice() {
        showPartiChoiceDialog();
    }

    @Override
    public void hidePartiChoiceProgressBar() {
        if(partiChoiceFragment == null) return;
        partiChoiceFragment.hideProgressBar();
    }

    @Override
    public void showPartiChoiceProgressBar() {
        if(partiChoiceFragment == null) return;
        partiChoiceFragment.showProgressBar();
    }

    @Override
    public void resetPartiChoiceList(List<Parti> joindedParties) {
        if(partiChoiceFragment == null) return;
        partiChoiceFragment.resetPartiList(joindedParties);
    }

    @Override
    public void resetPreviewImages(List<Uri> imagesUrls) {
        previewImagesAdapter.clear();
        List<PostFormImageItem> items = new ArrayList<>();
        for(Uri imageUri : imagesUrls) {
            items.add(new PostFormImageItem(imageUri));
            Log.d(Constants.TAG_TEST, imageUri.toString());
        }
        previewImagesAdapter.add(items);
        toggleVisibilityOfPreviewImagesAdapter();
    }

    private void toggleVisibilityOfPreviewImagesAdapter() {
        if(previewImagesAdapter.getItemCount() > 0) {
            previewImagesLayout.setVisibility(View.VISIBLE);
        } else {
            previewImagesLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void removePreviewImage(Uri url) {
        if(url == null) return;

        int count = previewImagesAdapter.getItemCount();
        for(int i = 0; i < count; i++) {
            PostFormImageItem item = previewImagesAdapter.getAdapterItem(i);
            if(url.equals(item.getUrl())) {
                previewImagesAdapter.remove(i);
                break;
            }
        }
        toggleVisibilityOfPreviewImagesAdapter();
    }

    @Override
    public String getBody() {
        return editTextVew.getText().toString();
    }

    @Override
    public Context getContext() {
        return this;
    }


    @Override
    public void finishAndReturn(Parti parti, String body, ArrayList<SelectedImage> fileSourceAttachmentImages) {
        Intent intent = new Intent();
        intent.putExtra("parti", Parcels.wrap(parti));
        intent.putExtra("body", body);
        intent.putExtra("fileSourceAttachmentImages", Parcels.wrap(fileSourceAttachmentImages));
        setResult(MainActivity.REQUEST_NEW_POST, intent);
        finish();
    }

    @Override
    public void showImagePicker(ArrayList<Uri> uris) {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Matisse.from(PostFormActivity.this)
                                .choose(MimeType.of(MimeType.GIF, MimeType.PNG, MimeType.JPEG))
                                .capture(true)
                                .captureInList(false)
                                .captureStrategy(new CaptureStrategy(true, "xyz.parti.catan.fileprovider", "Parti"))
                                .countable(true)
                                .maxSelectable(9)
                                .addFilter(new SizeFilter(10 * Filter.K * Filter.K))
                                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                .thumbnailScale(0.85f)
                                .theme(R.style.AppMetiess)
                                .imageEngine(new GlideEngine())
                                .forResult(REQUEST_CODE_CHOOSE, uris);
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    }
                })
                .setRationaleMessage(R.string.image_pick_permission_rationale)
                .setDeniedMessage(R.string.image_pick_permission_denied)
                .setPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"})
                .check();

    }

    @OnClick(R.id.layout_parti)
    public void onClickParti() {
        presenter.showPartiChoice();
    }

    @OnClick(R.id.button_save)
    public void onClickSave() {
        presenter.savePost();
    }

    @OnClick(R.id.button_pick_image)
    public void onClickPickImage() {
        presenter.showImagePicker();
    }

    @OnTextChanged(R.id.edittext_body)
    public void addBodyEditTextChanged(CharSequence text) {
        if(TextUtils.isEmpty(text)){
            saveButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
        }
    }

    public static class PartiChoiceFragment extends DialogFragment {
        private FastItemAdapter<AbstractItem> fastAdapter;

        @BindView(R.id.recyclerview_parti_choice_list)
        RecyclerView partiChoiceListRecycler;
        @BindView(R.id.progressbar_status)
        ProgressBar statusProgressBar;

        private PostFormPresenter presenter;
        private ProgressToggler progressToggler;
        private HeaderAdapter<PostFormGroupItem> headerAdapter;

        public void attachPresenter(PostFormPresenter presenter) {
            this.presenter = presenter;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppAlertDialog);

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.fragment_parti_choice, null);
            ButterKnife.bind(this, view);

            setUpParties();
            builder.setView(view);

            builder.setTitle(R.string.dialog_title_choice_parti_on_post_form);
            builder.setNegativeButton(R.string.cancel, (iDialog, button) -> iDialog.cancel());
            AlertDialog dialog = builder.create();

            if(savedInstanceState != null && savedInstanceState.getBoolean("showDialog", false)) {
                dialog.hide();
            }
            return dialog;
        }

        private void setUpParties() {
            progressToggler = new ProgressToggler(partiChoiceListRecycler, statusProgressBar);

            fastAdapter = new FastItemAdapter<>();
            fastAdapter.withSelectable(true);
            fastAdapter.withOnClickListener((v, adapter, item, position) -> {
                presenter.selectParti(((PostFormPartiItem)item).getParti());
                return true;
            });
            headerAdapter = new HeaderAdapter<>();
            
            partiChoiceListRecycler.setHasFixedSize(true);
            LinearLayoutManager recyclerViewLayout = new LinearLayoutManager(getContext());
            partiChoiceListRecycler.setLayoutManager(recyclerViewLayout);
            partiChoiceListRecycler.setAdapter(headerAdapter.wrap(fastAdapter));
            presenter.loadJoinedParties();
        }

        public void resetPartiList(List<Parti> parties) {
            if(fastAdapter == null) return;

            List<AbstractItem> items = new ArrayList<>();
            TreeMap<Group, List<PostFormPartiItem>> result = getGroupList(parties);
            for(Group group: result.keySet()) {
                if(group.isIndie()) {
                    items.add(0, new PostFormGroupItem(group));
                    items.addAll(1, result.get(group));
                } else {
                    items.add(new PostFormGroupItem(group));
                    items.addAll(result.get(group));
                }
            }
            fastAdapter.clear();
            fastAdapter.add(items);
        }

        @NonNull
        private TreeMap<Group, List<PostFormPartiItem>> getGroupList(List<Parti> parties) {
            TreeMap<Group, List<PostFormPartiItem>> result = new TreeMap<>();
            for(Parti parti : parties) {
                List<PostFormPartiItem> items = result.get(parti.group);
                if(items == null) {
                    items = new ArrayList<>();
                }
                items.add(new PostFormPartiItem(parti));
                result.put(parti.group, items);
            }
            return result;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            presenter.cancelPartiChoice();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("showDialog", getDialog() != null && getDialog().isShowing());
        }

        public void hideProgressBar() {
            progressToggler.toggle(false);
        }

        public void showProgressBar() {
            progressToggler.toggle(true);
        }
    }
}
