package xyz.parti.catan.ui.binder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.Post;


class OptionBinder {
    @BindView(R.id.textview_option_body)
    TextView bodyTextView;
    @BindView(R.id.checkbox_survey)
    CheckBox checkBox;
    @BindView(R.id.radiobutton_survey)
    RadioButton radioButton;
    @BindView(R.id.textview_feedbacks_count)
    TextView feedbacksCountTextView;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;
    @BindView(R.id.textview_survey_selected_sign)
    IconTextView selectedSignTextView;

    OptionBinder(ViewGroup view) {
        ButterKnife.bind(this, view);
    }

    public void bind(final PostBinder.PostBindablePresenter presenter, final Post post, final Option option) {
        unbind();

        bodyTextView.setText(option.body);

        final CompoundButton holder;
        if(post.survey.multiple_select) {
            checkBox.setVisibility(View.VISIBLE);
            radioButton.setVisibility(View.GONE);
            holder = checkBox;
        } else {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.VISIBLE);
            holder = radioButton;
        }


        if(post.survey.is_open) {
            bodyTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.setChecked(!holder.isChecked());
                }
            });
        }

        if(post.survey.is_open) {
            holder.setVisibility(android.view.View.VISIBLE);
            holder.setChecked(option.is_my_select);
            holder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    presenter.onClickSurveyOption(post, option, b);
                }
            });
            selectedSignTextView.setVisibility(android.view.View.GONE);
        } else {
            holder.setVisibility(android.view.View.GONE);
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

            progressBar.setMax(100);
            progressBar.setProgress(Math.round(option.percentage));
            progressBar.setVisibility(android.view.View.VISIBLE);
        } else {
            feedbacksCountTextView.setVisibility(android.view.View.GONE);
            progressBar.setVisibility(android.view.View.GONE);
        }
    }

    void unbind() {
        if(bodyTextView != null) bodyTextView.setOnClickListener(null);
        if(checkBox != null) checkBox.setOnCheckedChangeListener(null);
        if(radioButton != null) radioButton.setOnCheckedChangeListener(null);
    }


}
