package xyz.parti.catan.data.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import xyz.parti.catan.Constants;

/**
 * Created by dalikim on 2017. 6. 7..
 */

public class ReadPostFeed  extends RealmObject {
    @PrimaryKey
    public long postFeedId;
    public Date lastReadAt;
    public Date lastStrokedAt;

    public boolean isUnread() {
        if(lastStrokedAt == null) return false;
        if(lastReadAt == null && lastStrokedAt != null ) return true;
        return (lastStrokedAt.getTime() > lastReadAt.getTime());
    }


    public boolean isDashboard() {
        return postFeedId == Constants.POST_FEED_DASHBOARD;
    }

}
