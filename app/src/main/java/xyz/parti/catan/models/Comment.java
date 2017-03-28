package xyz.parti.catan.models;

/**
 * Created by dalikim on 2017. 3. 28..
 */

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
