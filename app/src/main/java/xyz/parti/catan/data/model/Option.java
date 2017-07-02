package xyz.parti.catan.data.model;

import org.parceler.Parcel;

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
