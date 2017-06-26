package xyz.parti.catan.data.model;


import java.util.Date;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class Message {
    public Long id;
    public Comment comment_messagable;
    public Upvote upvote_messagable;
    public Parti parti_messagable;
    public Member member_messagable;
    public MemberRequest member_request_messagable;
    public Option option_messagable;
    public Survey survey_messagable;
    public String messagable_type;
    public User user;
    public User sender;
    public Post post;
    public Parti parti;
    public String header;
    public String title;
    public String body;
    public Date read_at;
    public Date created_at;
    public Fcm fcm;
}
