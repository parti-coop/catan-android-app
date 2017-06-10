package xyz.parti.catan.data.activerecord;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;

import xyz.parti.catan.Constants;

/**
 * Created by dalikim on 2017. 6. 7..
 */

@Table(name = "ReadPostFeeds")
public class ReadPostFeed extends Model {
    @Column(name = "PostFeedId")
    public long postFeedId;

    @Column(name = "LastReadAt")
    public Date lastReadAt;

    @Column(name = "LastStrokedAt")
    public Date lastStrokedAt;

    public boolean isUnread() {
        if(lastStrokedAt == null) return false;
        if(lastReadAt == null && lastStrokedAt != null ) return true;
        return (lastStrokedAt.getTime() > lastReadAt.getTime());
    }

    public static ReadPostFeed forDashboard() {
        return forPartiOrDashboard(Constants.POST_FEED_DASHBOARD);
    }

    public static ReadPostFeed forPartiOrDashboard(long postFeedId) {
        ReadPostFeed readPostFeed = ReadPostFeed.fetch(postFeedId);
        if(readPostFeed == null) {
            readPostFeed = new ReadPostFeed();
            readPostFeed.postFeedId = postFeedId;
        }
        return readPostFeed;
    }

    public static void destroyIfExist(long partiId) {
        ReadPostFeed readPostFeed = forPartiOrDashboard(partiId);
        if(readPostFeed.getId() == null) return;
        readPostFeed.delete();
    }

    public boolean isDashboard() {
        return postFeedId == Constants.POST_FEED_DASHBOARD;
    }

    private static ReadPostFeed fetch(long postFeedId) {
        return new Select()
                .from(ReadPostFeed.class)
                .where("PostFeedId = ?", postFeedId)
                .executeSingle();
    }

    public void updateLastStrokedAtSeconds(Long lastStrokedSecondTime) {
        if(lastStrokedSecondTime == null || lastStrokedSecondTime < 0) {
            lastStrokedAt = null;
        } else {
            lastStrokedAt = new Date(lastStrokedSecondTime * 1000);

            if(!isDashboard()) {
                ReadPostFeed readDashboard = ReadPostFeed.forDashboard();
                if (readDashboard.lastStrokedAt == null || lastStrokedAt.getTime() > readDashboard.lastStrokedAt.getTime()) {
                    readDashboard.lastStrokedAt = lastStrokedAt;
                    if (readDashboard.lastReadAt == null) {
                        readDashboard.lastReadAt = lastStrokedAt;
                    }
                    readDashboard.save();
                }
            }
        }
        if(lastReadAt == null) {
            lastReadAt = lastStrokedAt;
        }
        save();
    }
}
