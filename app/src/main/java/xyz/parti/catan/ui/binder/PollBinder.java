package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonNull;
import com.joanzapata.iconify.widget.IconButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.parti.catan.R;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.services.VotingsService;
import xyz.parti.catan.sessions.SessionManager;

/**
 * Created by dalikim on 2017. 4. 25..
 */

public class PollBinder {
    @BindView(R.id.pollAgreeVotes)
    LinearLayout pollAgreeVotesLayout;
    @BindView(R.id.pollDisagreeVotes)
    LinearLayout pollDisagreeVotesLayout;
    @BindView(R.id.pollTitle)
    TextView pollTitle;
    @BindView(R.id.pollAgreeButton)
    IconButton pollAgreeButton;
    @BindView(R.id.pollDisagreeButton)
    IconButton pollDisagreeButton;

    private final VotingsService votingsService;
    private final Context context;
    private SessionManager session;

    public PollBinder(ViewGroup view, SessionManager session) {
        this.context = view.getContext();
        this.session = session;
        ButterKnife.bind(this, view);

        votingsService = ServiceGenerator.createService(VotingsService.class, session);
    }

    public void bindData(final Post post) {
        pollTitle.setText(post.poll.title);
        bindVotings(post);

        pollAgreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newChoice = (post.poll.isAgreed() ? "unsure" : "agree");
                Call<JsonNull> call = votingsService.voting(post.poll.id, newChoice);
                call.enqueue(new Callback<JsonNull>() {
                    @Override
                    public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                        if(response.isSuccessful()) {
                            post.poll.updateChoice(PollBinder.this.session.getCurrentUser(), newChoice);
                            bindVotings(post);
                        } else {
                            ReportHelper.wtf(context.getApplicationContext(), "Agree error : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonNull> call, Throwable t) {
                        ReportHelper.wtf(context.getApplicationContext(), t);
                    }
                });
            }
        });
        pollDisagreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String newChoice = (post.poll.isDisagreed()  ? "unsure" : "disagree");
                Call<JsonNull> call = votingsService.voting(post.poll.id, newChoice);
                call.enqueue(new Callback<JsonNull>() {
                    @Override
                    public void onResponse(Call<JsonNull> call, Response<JsonNull> response) {
                        if(response.isSuccessful()) {
                            post.poll.updateChoice(PollBinder.this.session.getCurrentUser(), newChoice);
                            bindVotings(post);
                        } else {
                            ReportHelper.wtf(context.getApplicationContext(), "Disagree error : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonNull> call, Throwable t) {
                        ReportHelper.wtf(context.getApplicationContext(), t);
                    }
                });
            }
        });
    }

    private void bindVotings(Post post) {
        pollAgreeVotesLayout.removeAllViews();
        pollDisagreeVotesLayout.removeAllViews();
        new VoteUsersBinder(pollAgreeVotesLayout, true).bindData(post.poll.latest_agreed_voting_users);
        new VoteUsersBinder(pollDisagreeVotesLayout, false).bindData(post.poll.latest_disagreed_voting_users);

        unselectedStyle(pollAgreeButton);
        unselectedStyle(pollDisagreeButton);

        if(post.poll.isVoted()) {
            if(post.poll.isAgreed()) {
                selectedStyle(pollAgreeButton);
            } else if(post.poll.isDisagreed()) {
                selectedStyle(pollDisagreeButton);
            }
            pollAgreeButton.setText(context.getText(R.string.references_poll_agree_button) + "\n" + post.poll.agreed_votings_count);
            pollDisagreeButton.setText(context.getText(R.string.references_poll_disagree_button) + "\n" + post.poll.disagreed_votings_count);
        } else {
            pollAgreeButton.setText(R.string.references_poll_agree_button);
            pollDisagreeButton.setText(R.string.references_poll_disagree_button);
        }
    }

    private void selectedStyle(IconButton button) {
        switch(button.getId()) {
            case R.id.pollAgreeButton:
                button.setTextColor(ContextCompat.getColor(context, R.color.selected_vote_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_agree));
                break;
            case R.id.pollDisagreeButton:
                button.setTextColor(ContextCompat.getColor(context, R.color.selected_vote_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_disagree));
                break;
        }
    }

    private void unselectedStyle(IconButton button) {
        switch(button.getId()) {
            case R.id.pollAgreeButton:
                button.setTextColor(ContextCompat.getColor(context, R.color.vote_agree_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_transparent));
                break;
            case R.id.pollDisagreeButton:
                button.setTextColor(ContextCompat.getColor(context, R.color.vote_disagree_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_transparent));
                break;
        }
    }
}
