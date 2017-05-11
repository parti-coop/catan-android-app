package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Comment implements RecyclableModel {
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

    @Override
    public boolean isSame(Object other) {
        return other != null && other instanceof Comment && id != null && id.equals(((Comment)other).id);
    }

    @Override
    public List<String> getPreloadImageUrls() {
        List<String> result = new ArrayList<>();
        if(user != null) {
            result.addAll(user.getPreloadImageUrls());
        }
        return result;
    }
}
