package xyz.parti.catan.data.model;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Upvote {
    public Long id;
    public User user;
    public Long upvotable_id;
    public String upvotable_type;
}
