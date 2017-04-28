package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.sessions.SessionManager;

/**
 * Created by dalikim on 2017. 4. 28..
 */

class SurveyBinder {
    @BindView(R.id.options)
    LinearLayout optionsLayout;
    @BindView(R.id.footnote)
    TextView footnoteText;

    private final Context context;
    private final SessionManager session;
    private LayoutInflater inflater;

    private final PostsService postsService;

    public SurveyBinder(ViewGroup view, SessionManager session) {
        this.context = view.getContext();
        this.session = session;
        this.inflater =  LayoutInflater.from(view.getContext());
        ButterKnife.bind(this, view);

        postsService = ServiceGenerator.createService(PostsService.class, session);
    }

    public void bindData(Post post) {
        for(Option option : post.survey.options) {
            bindOption(post, option);
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
        footnoteText.setText(footnote);
    }

    private void bindOption(Post post, Option option) {
        LinearLayout optionLayout = (LinearLayout) inflater.inflate(R.layout.references_survey_option, optionsLayout, false);
        new OptionBinder(optionLayout, session).bindData(this, post, option);
        optionsLayout.addView(optionLayout);
    }

    public void reload(final Post post) {
        Call<Post> call = postsService.getPost(post.id);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(response.isSuccessful()) {
                    post.survey = response.body().survey;
                    optionsLayout.removeAllViews();
                    SurveyBinder.this.bindData(post);
                } else {
                    ReportHelper.wtf(context.getApplicationContext(), "Rebind survey error : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                ReportHelper.wtf(context.getApplicationContext(), t);
            }
        });
    }
}