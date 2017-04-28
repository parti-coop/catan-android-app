package xyz.parti.catan.models;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 4. 27..
 */

@Parcel
public class Option {
    public long id;
    public int feedbacks_count;
    public String body;
    public float percentage;
    public User user;
    public boolean is_my_select;
    public boolean is_mvp;
}
