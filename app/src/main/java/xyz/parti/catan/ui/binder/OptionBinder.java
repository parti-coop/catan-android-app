package xyz.parti.catan.ui.binder;

import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 28..
 */

class OptionBinder {
    @BindView(R.id.textview_body)
    TextView bodyTextView;
    @BindView(R.id.checkbox)
    CheckBox checkBox;
    @BindView(R.id.textview_feedbacks_count)
    TextView feedbacksCountTextView;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;
    @BindView(R.id.textview_selected_sign)
    IconTextView selectedSignTextView;

    private final PostBinder.PostBindablePresenter presenter;

    OptionBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        ButterKnife.bind(this, view);
    }

    public void bindData(final Post post, final Option option) {
        bodyTextView.setText(option.body);
        if(post.survey.is_open) {
            bodyTextView.setOnClickListener(view -> checkBox.setChecked(!checkBox.isChecked()));
        }

        if(post.survey.is_open) {
            checkBox.setVisibility(android.view.View.VISIBLE);
            checkBox.setChecked(option.is_my_select);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    presenter.onClickSurveyOption(post, option, b);
                }
            });
            selectedSignTextView.setVisibility(android.view.View.GONE);
        } else {
            checkBox.setVisibility(android.view.View.GONE);
            if(option.is_my_select) {
                selectedSignTextView.setVisibility(android.view.View.VISIBLE);
            } else {
                selectedSignTextView.setVisibility(android.view.View.GONE);
            }
        }

        if(post.survey.is_feedbacked_by_me || !post.survey.is_open) {
            if(option.is_mvp) {
                feedbacksCountTextView.setText("" + option.feedbacks_count + "표 \u2022 최다득표");
            } else {
                feedbacksCountTextView.setText("" + option.feedbacks_count + "표");
            }
            feedbacksCountTextView.setVisibility(android.view.View.VISIBLE);

            progressBar.setMax(post.survey.feedback_users_count);
            progressBar.setProgress(option.feedbacks_count);
            progressBar.setVisibility(android.view.View.VISIBLE);
        } else {
            feedbacksCountTextView.setVisibility(android.view.View.GONE);
            progressBar.setVisibility(android.view.View.GONE);
        }
    }
}
