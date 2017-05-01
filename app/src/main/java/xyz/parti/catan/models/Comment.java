package xyz.parti.catan.models;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Comment {
    public Long id;
    public String body;
    public String truncated_body;
    public Long upvotes_count;
    public User user;
    public Date created_at;
    public Boolean is_mentionable;
    public Boolean is_upvotable;
    public Boolean is_blinded;
    public String choice;
}
