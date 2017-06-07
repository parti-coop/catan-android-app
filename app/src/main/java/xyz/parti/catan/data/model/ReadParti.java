package xyz.parti.catan.data.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;

import xyz.parti.catan.Constants;

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

    public boolean isDashboard() {
        return partiId == DASHBOARD;
    }

    public static ReadParti forParti(Parti currentParti) {
        ReadParti readParti = new Select()
                .from(ReadParti.class)
                .where("PartiId = ?", (currentParti == null ? DASHBOARD : currentParti.id))
                .executeSingle();
        if(readParti == null) {
            readParti = new ReadParti();
            if(currentParti == null) {
                readParti.partiId = DASHBOARD;
            } else {
                readParti.partiId = currentParti.id;
            }
        }
        return readParti;
    }
}
