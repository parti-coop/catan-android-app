package xyz.parti.catan.ui.binder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;

/**
 * Created by dalikim on 2017. 4. 25..
 */

public class PollBinder {
    @BindView(R.id.layout_agree_votes)
    LinearLayout agreeVotesLayout;
    @BindView(R.id.layout_disagree_votes)
    LinearLayout disagreeVotesLayout;
    @BindView(R.id.textview_title)
    TextView titleTextView;
    @BindView(R.id.button_agree)
    IconButton agreeButton;
    @BindView(R.id.button_disagree)
    IconButton disagreeButton;

    private final PostBinder.PostBindablePresenter presenter;
    private final Context context;

    PollBinder(PostBinder.PostBindablePresenter presenter, View view) {
        this.presenter = presenter;
        this.context = view.getContext();
        ButterKnife.bind(this, view);
    }

    public void bindData(final Post post) {
        titleTextView.setText(post.poll.title);
        bindVotings(post);

        agreeButton.setOnClickListener(view -> presenter.onClickPollAgree(post));
        disagreeButton.setOnClickListener(view -> presenter.onClickPollDisgree(post));
    }

    private void bindVotings(Post post) {
        agreeVotesLayout.removeAllViews();
        disagreeVotesLayout.removeAllViews();
        new VoteUsersBinder(agreeVotesLayout, true).bindData(post.poll.latest_agreed_voting_users);
        new VoteUsersBinder(disagreeVotesLayout, false).bindData(post.poll.latest_disagreed_voting_users);

        unselectedStyle(agreeButton);
        unselectedStyle(disagreeButton);

        if(post.poll.isVoted()) {
            if(post.poll.isAgreed()) {
                selectedStyle(agreeButton);
            } else if(post.poll.isDisagreed()) {
                selectedStyle(disagreeButton);
            }
            agreeButton.setText(context.getText(R.string.references_poll_agree_button) + "\n" + post.poll.agreed_votings_count);
            disagreeButton.setText(context.getText(R.string.references_poll_disagree_button) + "\n" + post.poll.disagreed_votings_count);
        } else {
            agreeButton.setText(R.string.references_poll_agree_button);
            disagreeButton.setText(R.string.references_poll_disagree_button);
        }
    }

    private void selectedStyle(IconButton button) {
        switch(button.getId()) {
            case R.id.button_agree:
                button.setTextColor(ContextCompat.getColor(context, R.color.selected_vote_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_agree));
                break;
            case R.id.button_disagree:
                button.setTextColor(ContextCompat.getColor(context, R.color.selected_vote_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_disagree));
                break;
        }
    }

    private void unselectedStyle(IconButton button) {
        switch(button.getId()) {
            case R.id.button_agree:
                button.setTextColor(ContextCompat.getColor(context, R.color.vote_agree_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_transparent));
                break;
            case R.id.button_disagree:
                button.setTextColor(ContextCompat.getColor(context, R.color.vote_disagree_button));
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_transparent));
                break;
        }
    }
}
