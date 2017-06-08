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

@Table(name = "ReadParties")
public class ReadParti extends Model {
    public final static long DASHBOARD = 0;

    @Column(name = "PartiId")
    public long partiId;

    @Column(name = "LastReadAt")
    public Date lastReadAt;

    @Column(name = "LastStrokedAt")
    public Date lastStrokedAt;

    public boolean isUnread() {
        if(lastReadAt == null || lastStrokedAt == null) return false;
        return (lastStrokedAt.getTime() > lastReadAt.getTime());
    }

    public static ReadParti forDashboard() {
        return forParti(null);
    }

    public static ReadParti forParti(long partiId) {
        ReadParti readParti = new Select()
                .from(ReadParti.class)
                .where("PartiId = ?", partiId)
                .executeSingle();
        if(readParti == null) {
            readParti = new ReadParti();
            readParti.partiId = partiId;
        }
        return readParti;
    }

    public static ReadParti forParti(Parti currentParti) {
        return forParti(currentParti == null ? DASHBOARD : currentParti.id);
    }

    public static List<Long> unreads() {
        List<Long> result = new ArrayList<>();
        
        List<ReadParti> all = new Select().from(ReadParti.class).execute();
        for(ReadParti readParti : all) {
            if(readParti.isUnread()) {
                result.add(readParti.partiId);
            }
        }

        return result;
    }

    public static void destroyIfExist(long partiId) {
        ReadParti readParti = forParti(partiId);
        if(readParti.getId() == null) return;
        readParti.delete();
    }
}
