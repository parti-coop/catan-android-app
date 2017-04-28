package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonNull;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.R;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.Option;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.FeedbacksService;
import xyz.parti.catan.sessions.SessionManager;

/**
 * Created by dalikim on 2017. 4. 28..
 */

public class OptionBinder {
    @BindView(R.id.optionBody)
    TextView optionBodyText;
    @BindView(R.id.optionCheckBox)
    CheckBox optionCheckBox;
    @BindView(R.id.feedbacksCount)
    TextView feedbacksCountView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.optionSelected)
    IconTextView optionSelectedText;

    private final Context context;
    private final FeedbacksService feedbacksService;
    private final SessionManager session;

    public OptionBinder(ViewGroup view, SessionManager session) {
        this.context = view.getContext();
        this.session = session;
        ButterKnife.bind(this, view);

        feedbacksService = ServiceGenerator.createService(FeedbacksService.class, session);
    }

    public void bindData(final SurveyBinder binder, final Post post, final Option option) {
        optionBodyText.setText(option.body);
        if(post.survey.is_open) {
            optionBodyText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    optionCheckBox.setChecked(!optionCheckBox.isChecked());
                }
            });
        }

        if(post.survey.is_open) {
            optionCheckBox.setVisibility(View.VISIBLE);
            optionCheckBox.setChecked(option.is_my_select);
            optionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    changeSelection(binder, post, option, b);
                }
            });
            optionSelectedText.setVisibility(View.GONE);
        } else {
            optionCheckBox.setVisibility(View.GONE);
            if(option.is_my_select) {
                optionSelectedText.setVisibility(View.VISIBLE);
            } else {
                optionSelectedText.setVisibility(View.GONE);
            }
        }

        if(post.survey.is_feedbacked_by_me || !post.survey.is_open) {
            if(option.is_mvp) {
                feedbacksCountView.setText(option.feedbacks_count + "표 \u2022 최다득표");
            } else {
                feedbacksCountView.setText(option.feedbacks_count + "표");
            }
            feedbacksCountView.setVisibility(View.VISIBLE);

            progressBar.setMax(post.survey.feedback_users_count);
            progressBar.setProgress(option.feedbacks_count);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            feedbacksCountView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void changeSelection(final SurveyBinder binder, final Post post, Option option, boolean isChecked) {
        Call<JsonNull> call = feedbacksService.feedback(option.id, isChecked);
        call.enqueue(new Callback<JsonNull>() {
            @Override
            public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                if(response.isSuccessful()) {
                    binder.reload(post);
                } else {
                    ReportHelper.wtf(context.getApplicationContext(), "Feedback error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonNull> call, Throwable t) {
                ReportHelper.wtf(context.getApplicationContext(), t);
            }
        });
    }

}
