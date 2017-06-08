package xyz.parti.catan.data.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dalikim on 2017. 6. 7..
 */

@Table(name = "ReadPostFeeds")
public class ReadPostFeed extends Model {
    public final static long DASHBOARD = 0;

    @Column(name = "PartiId")
    public long partiId;

    @Column(name = "LastReadAt")
    public Date lastReadAt;

    @Column(name = "LastStrokedAt")
    public Date lastStrokedAt;

    public boolean isUnread() {
        if(lastStrokedAt == null) return false;
        if(lastReadAt == null &&lastStrokedAt != null ) return true;
        return (lastStrokedAt.getTime() > lastReadAt.getTime());
    }

    public static ReadPostFeed forDashboard() {
        return forPartiOrDashboard(null);
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

    public static ReadPostFeed forPartiOrDashboard(Parti currentParti) {
        return forPartiOrDashboard(currentParti == null ? DASHBOARD : currentParti.id);
    }

    public static List<Long> unreads() {
        List<Long> result = new ArrayList<>();
        
        List<ReadPostFeed> all = new Select().from(ReadPostFeed.class).execute();
        for(ReadPostFeed readPostFeed : all) {
            if(readPostFeed.isUnread()) {
                result.add(readPostFeed.partiId);
            }
        }

        return result;
    }

    public static void destroyIfExist(long partiId) {
        ReadPostFeed readPostFeed = forPartiOrDashboard(partiId);
        if(readPostFeed.getId() == null) return;
        readPostFeed.delete();
    }

    public boolean isDashboard() {
        return partiId == DASHBOARD;
    }
}
