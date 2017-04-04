package xyz.parti.catan.models;

import java.util.Date;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class Post {
    public Long id;
    public Boolean full;
    public String parsed_title;
    public String parsed_body;
    public String truncated_parsed_body;
    public String specific_desc_striped_tags;
    public Parti parti;
    public User user;
    public Date created_at;
    public Date last_stroked_at;
    public Boolean is_upvotable;
    public Long upvotes_count;
    public User[] latest_upvote_users;
    public Upvote[] latest_upvotes;
    public Long comments_count;
    public Comment[] latest_comments;
    public LinkSource link_reference;
    public Poll poll;
    public Share share;
    public FileSource[] file_sources;
}
