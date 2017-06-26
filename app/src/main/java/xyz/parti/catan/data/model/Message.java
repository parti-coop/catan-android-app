package xyz.parti.catan.data.model;


import android.text.TextUtils;

import java.util.Date;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class Message {
    public Long id;
    public Post post;
    public String url;
    public User user;
    public User sender;
    public Parti parti;
    public String header;
    public String title;
    public String body;
    public Date read_at;
    public Date created_at;

    public boolean isShowable() {
        return post != null || !TextUtils.isEmpty(url);
    }
}
