package xyz.parti.catan.models;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class Parti {
    public Long id;
    public String title;
    public String body;
    public String slug;
    public String logo_url;
    public Group group;
    public String updated_at;
    public Long latest_members_count;
    public Long latest_posts_count;
    public Long members_count;
    public Long posts_count;
    public Boolean is_member;
    public Boolean is_made_by;
    public Boolean is_made_by_target_user;
    public Share share;
}
