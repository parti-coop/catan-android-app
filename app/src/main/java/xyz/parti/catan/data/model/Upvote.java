package xyz.parti.catan.data.model;

import org.parceler.Parcel;


@Parcel
public class Upvote {
    public Long id;
    public User user;
    public Long upvotable_id;
    public String upvotable_type;
}
