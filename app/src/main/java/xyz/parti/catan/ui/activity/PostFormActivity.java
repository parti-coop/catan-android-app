package xyz.parti.catan.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikepenz.fastadapter.adapters.HeaderAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.parti.catan.R;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.ui.adapter.PostFormGroupItem;
import xyz.parti.catan.ui.adapter.PostFormPartiItem;
import xyz.parti.catan.ui.presenter.PostFormPresenter;
import xyz.parti.catan.ui.view.ProgressToggler;

/**
 * Created by dalikim on 2017. 5. 23..
 */

public class PostFormActivity extends BaseActivity implements PostFormPresenter.View {
    private static final String TAG_PARTI_CHOICE = "xyz.parti.catan.PartiChoiceDialog";
    private PostFormPresenter presenter;
    PartiChoiceFragment partiChoiceFragment;

    @BindView(R.id.textview_parti_title)
    TextView partiTitleTextView;
    @BindView(R.id.imageview_parti_logo)
    ImageView partiLogoImageView;
    @BindView(R.id.edittext_body)
    EditText editTextVew;

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
        super.onDestroy();
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
    public void closePartiChoiceDialog() {
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
    public String getBody() {
        return editTextVew.getText().toString();
    }

    @Override
    public Context getContext() {
        return this;
    }


    @Override
    public void finishAndReturn(Parti parti, String body) {
        Intent intent = new Intent();
        intent.putExtra("parti", Parcels.wrap(parti));
        intent.putExtra("body", body);
        setResult(MainActivity.REQUEST_NEW_POST, intent);
        finish();
    }

    @OnClick(R.id.layout_parti)
    public void onClickParti() {
        presenter.showPartiChoice();
    }

    @OnClick(R.id.button_save)
    public void onClickSave() {
        presenter.savePost();
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
            builder.setNegativeButton(R.string.cancel, (dialog, button) -> {
                dialog.cancel();
            });
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
            final LinearLayoutManager recyclerViewLayout = new LinearLayoutManager(getContext());
            partiChoiceListRecycler.setLayoutManager(recyclerViewLayout);
            partiChoiceListRecycler.setAdapter(headerAdapter.wrap(fastAdapter));
            presenter.loadJoinedParties(fastAdapter);
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
