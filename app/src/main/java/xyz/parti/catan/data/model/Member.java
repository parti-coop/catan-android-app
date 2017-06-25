package xyz.parti.catan.data.model;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class Member {
    public Long id;
    public Group group_joinable;
    public Parti parti_joinable;
    public String joinable_type;
    public User user;
    public Boolean is_organizer;
}
