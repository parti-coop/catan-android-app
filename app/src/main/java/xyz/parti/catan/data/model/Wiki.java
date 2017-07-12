package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Wiki {
    public long id;
    public String title;
    public String thumbnail_md_url;
    public float image_ratio;
    public User[] authors;
    public String latest_activity_body;
    public Date latest_activity_at;
    public String url;
}
