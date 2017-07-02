package xyz.parti.catan.ui.binder;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import xyz.parti.catan.R;
import xyz.parti.catan.data.model.User;

/**
 * Created by dalikim on 2017. 4. 25..
 */

class VoteUsersBinder {
    private ViewGroup view;

    VoteUsersBinder(ViewGroup view) {
        this.view = view;
    }

    public void bind(User[] voteUsers) {
        for (User user : voteUsers) {
            bindUser(user);
        }
    }

    private void bindUser(User user) {
        SimpleDraweeView imageView = new SimpleDraweeView(view.getContext());

        int size = view.getContext().getResources().getDimensionPixelSize(R.dimen.poll_vote_user_size);
        int margin = view.getContext().getResources().getDimensionPixelSize(R.dimen.poll_vote_user_margin);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.setMargins(margin, 0, margin, 0);
        imageView.setLayoutParams(layoutParams);


        imageView.getHierarchy().setFailureImage(R.drawable.ic_account_circle_gray_24dp);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        imageView.getHierarchy().setRoundingParams(roundingParams);

        imageView.setImageURI(user.image_url);
        view.addView(imageView);
    }
}
