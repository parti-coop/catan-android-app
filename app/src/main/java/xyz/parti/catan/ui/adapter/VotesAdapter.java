package xyz.parti.catan.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.parti.catan.Constants;
import xyz.parti.catan.helper.ImageHelper;
import xyz.parti.catan.models.User;

/**
 * Created by dalikim on 2017. 4. 25..
 */

public class VotesAdapter extends BaseAdapter {
    final Context context;
    final User[] users;
    private boolean rtl;

    public VotesAdapter(Context context, User[] users, boolean rtl) {
        this.context = context;
        this.users = users;
        this.rtl = rtl;
    }

    @Override
    public int getCount() {
        return users.length;
    }

    @Override
    public Object getItem(int position) {
        return users[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CircleImageView imageView;

        if (convertView == null) {
            imageView = new CircleImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(35, 35));
            imageView.setScaleType(CircleImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (CircleImageView) convertView;
        }
        ImageHelper.loadInto(imageView, users[position].image_url);
        if(rtl) {
            imageView.setRotationY(180);
        }
        return imageView;
    }
}
