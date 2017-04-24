package xyz.parti.catan.models;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Comment {
    Long id;
    String body;
    String truncated_body;
    Long upvotes_count;
    User user;
    String created_at;
    Boolean is_mentionable;
    Boolean is_upvotable;
    Boolean is_blinded;
    String choice;
}
