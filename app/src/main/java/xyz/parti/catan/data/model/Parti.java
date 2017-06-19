package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Parti extends RealmObject {
    @PrimaryKey
    public Long id;
    @Required
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
    @Ignore
    public Share share;

    public Collection<? extends String> getPreloadImageUrls() {
        List<String> result = new ArrayList<>();
        result.add(logo_url);
        return result;
    }
}
