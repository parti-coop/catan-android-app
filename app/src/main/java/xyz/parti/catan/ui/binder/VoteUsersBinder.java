package xyz.parti.catan.ui.binder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.helper.ImageHelper;

/**
 * Created by dalikim on 2017. 4. 25..
 */

class VoteUsersBinder {
    private final LayoutInflater inflater;
    private ViewGroup view;
    private boolean reverse;

    VoteUsersBinder(ViewGroup view, boolean reverse) {
        this.view = view;
        this.reverse = reverse;
        this.inflater =  LayoutInflater.from(view.getContext());
    }

    public void bindData(User[] voteUsers) {
        if(reverse) {
            for (int i = voteUsers.length - 1; i >= 0; i--) {
                bindUser(voteUsers[i]);
            }
        } else {
            for (User user : voteUsers) {
                bindUser(user);
            }
        }
    }

    private void bindUser(User user) {
        CircleImageView imageView = (CircleImageView) inflater.inflate(R.layout.references_poll_vote_user, view, false);
        if(reverse) {
            imageView.setRotationY(180);
        }
        new ImageHelper(imageView).loadInto(user.image_url, CircleImageView.ScaleType.CENTER_CROP, CircleImageView.ScaleType.CENTER_CROP);
        view.addView(imageView);
    }
}
