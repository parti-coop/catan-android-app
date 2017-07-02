package xyz.parti.catan.ui.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.Post;


public class SurveyBinder {
    @BindView(R.id.layout_references_survey)
    LinearLayout referencesSurveyLayout;
    @BindView(R.id.layout_survey_options)
    LinearLayout optionsLayout;
    @BindView(R.id.textview_survey_footnote)
    TextView footnoteTextView;
    @BindView(R.id.layout_new_option_input)
    LinearLayout newOptionInputLayout;
    @BindView(R.id.checkbox_new_option_input)
    CheckBox newOptionInputCheckbox;
    @BindView(R.id.radiobutton_new_option_input)
    RadioButton newOptionInputRadioButton;

    private List<OptionBinder> optionBinders = new ArrayList<>();

    private LayoutInflater inflater;

    public SurveyBinder(ViewGroup view) {
        this.inflater =  LayoutInflater.from(view.getContext());
        inflater.inflate(R.layout.references_survey, view);

        ButterKnife.bind(this, view);
    }

    public void bind(final PostBinder.PostBindablePresenter presenter, final Post post) {
        unbind();

        optionsLayout.removeAllViews();
        for(int i = 0; i < post.survey.options.length; i++) {
            Option option = post.survey.options[i];
            bindOption(presenter, post, option, i);
        }

        String footnote = post.survey.remain_time_human;
        if(post.survey.multiple_select) {
            footnote += " \u2022 " + "다중선택 가능";
        }
        if(post.survey.feedbacks_count > 0) {
            footnote += " \u2022 " + "총 투표 " + post.survey.feedback_users_count+ "명";
        }
        if(post.survey.is_open && !post.survey.is_feedbacked_by_me && post.survey.feedbacks_count > 0) {
            footnote += "\n" + "투표 후 현황을 확인할 수 있습니다";
        }
        footnoteTextView.setText(footnote);

        if(post.survey.is_open) {
            if (post.survey.multiple_select) {
                newOptionInputCheckbox.setVisibility(View.VISIBLE);
                newOptionInputRadioButton.setVisibility(View.GONE);
            } else {
                newOptionInputCheckbox.setVisibility(View.GONE);
                newOptionInputRadioButton.setVisibility(View.VISIBLE);
            }

            newOptionInputLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickNewOption(post);
                }
            });
            newOptionInputLayout.setVisibility(View.VISIBLE);
        } else{
            newOptionInputLayout.setVisibility(View.GONE);
        }
    }

    private void bindOption(PostBinder.PostBindablePresenter presenter, Post post, Option option, int index) {
        LinearLayout optionLayout = (LinearLayout) inflater.inflate(R.layout.references_survey_option, optionsLayout, false);
        OptionBinder optionBinder = new OptionBinder(optionLayout);
        optionBinder.bind(presenter, post, option);
        optionBinders.add(optionBinder);
        optionLayout.setTag(R.id.tag_option, option);
        optionsLayout.addView(optionLayout, index);
    }

    public void setVisibility(int visibility) {
        this.referencesSurveyLayout.setVisibility(visibility);
    }

    public void unbind() {
        if(newOptionInputLayout != null) newOptionInputLayout.setOnClickListener(null);
        for(OptionBinder binder : optionBinders) {
            binder.unbind();
        }
    }
}
