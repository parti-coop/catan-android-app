package xyz.parti.catan.data.activerecord;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.Parti;

/**
 * Created by dalikim on 2017. 6. 7..
 */

@Table(name = "ReadPostFeeds")
public class ReadPostFeed extends Model {
    @Column(name = "PartiId")
    public long partiId;

    @Column(name = "LastReadAt")
    public Date lastReadAt;

    @Column(name = "LastStrokedAt")
    public Date lastStrokedAt;

    public boolean isUnread() {
        if(lastStrokedAt == null) return false;
        if(lastReadAt == null && lastStrokedAt != null ) return true;
        Log.d(Constants.TAG_TEST, "READ POST FEED id " + partiId);
        Log.d(Constants.TAG_TEST, "READ POST FEED lastStrokedAt " + lastStrokedAt.getTime());
        Log.d(Constants.TAG_TEST, "READ POST FEED lastReadAt " + lastReadAt.getTime());
        return (lastStrokedAt.getTime() > lastReadAt.getTime());
    }

    public static ReadPostFeed forDashboard() {
        return forPartiOrDashboard(Constants.POST_FEED_DASHBOARD);
    }

    public static ReadPostFeed forPartiOrDashboard(long partiId) {
        ReadPostFeed readPostFeed = new Select()
                .from(ReadPostFeed.class)
                .where("PartiId = ?", partiId)
                .executeSingle();
        if(readPostFeed == null) {
            readPostFeed = new ReadPostFeed();
            readPostFeed.partiId = partiId;
        }
        return readPostFeed;
    }

    public static void destroyIfExist(long partiId) {
        ReadPostFeed readPostFeed = forPartiOrDashboard(partiId);
        if(readPostFeed.getId() == null) return;
        readPostFeed.delete();
    }

    public boolean isDashboard() {
        return partiId == Constants.POST_FEED_DASHBOARD;
    }
}
