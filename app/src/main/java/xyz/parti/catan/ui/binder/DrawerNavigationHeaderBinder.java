package xyz.parti.catan.ui.binder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.R;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.models.User;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public class DrawerNavigationHeaderBinder {
    @BindView(R.id.imageview_user_image)
    CircleImageView userImageImageView;
    @BindView(R.id.textview_user_nickname)
    TextView userNicknameTextView;
    @BindView(R.id.textview_user_email)
    TextView userEmailTextView;

    public DrawerNavigationHeaderBinder(View view) {
        ButterKnife.bind(this, view);
    }

    public void bindData(User currentUser) {
        ImageHelper.loadInto(userImageImageView, currentUser.image_url, ImageView.ScaleType.CENTER_CROP, ImageView.ScaleType.CENTER_CROP);
        userNicknameTextView.setText(currentUser.nickname);
        userEmailTextView.setText(currentUser.email);
    }
}
