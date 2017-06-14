package xyz.parti.catan.ui.binder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Option;
import xyz.parti.catan.data.model.Post;

/**
 * Created by dalikim on 2017. 4. 28..
 */

public class SurveyBinder {
    @BindView(R.id.layout_references_survey)
    LinearLayout referencesSurveyLayout;
    @BindView(R.id.textview_footnote)
    TextView footnoteTextView;

    private PostBinder.PostBindablePresenter presenter;
    private LayoutInflater inflater;

    public SurveyBinder(PostBinder.PostBindablePresenter presenter, ViewGroup view) {
        this.presenter = presenter;
        this.inflater =  LayoutInflater.from(view.getContext());
        inflater.inflate(R.layout.references_survey, view);

        ButterKnife.bind(this, view);
    }

    public void bindData(Post post) {
        for(int i = 0; i < referencesSurveyLayout.getChildCount(); i++) {
            View childView = referencesSurveyLayout.getChildAt(i);
            if(childView.getTag(R.id.option) != null) {
                referencesSurveyLayout.removeViewAt(i);
            }
        }
        for(int i = 0; i < post.survey.options.length; i++) {
            Option option = post.survey.options[i];
            bindOption(post, option, i);
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
    }

    private void bindOption(Post post, Option option, int index) {
        LinearLayout optionLayout = (LinearLayout) inflater.inflate(R.layout.references_survey_option, referencesSurveyLayout, false);
        new OptionBinder(presenter, optionLayout).bindData(post, option);
        optionLayout.setTag(R.id.option, option);
        referencesSurveyLayout.addView(optionLayout, index);
    }

    public void setVisibility(int visibility) {
        this.referencesSurveyLayout.setVisibility(visibility);
    }
}
