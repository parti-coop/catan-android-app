package xyz.parti.catan.data.dao;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.ReadPostFeed;

public class ReadPostFeedDAO {
    private Realm realm;

    public ReadPostFeedDAO(Realm realm) {
        this.realm = realm;
    }

    public ReadPostFeed forDashboard() {
        return forPartiOrDashboard(Constants.POST_FEED_DASHBOARD);
    }

    public ReadPostFeed forPartiOrDashboard(long postFeedId) {
        ReadPostFeed readPostFeed = fetch(postFeedId).findFirst();
        if(readPostFeed == null) {
            readPostFeed = new ReadPostFeed();
            readPostFeed.postFeedId = postFeedId;
            save(readPostFeed);
            return readPostFeed;
        } else {
            return realm.copyFromRealm(readPostFeed);
        }
    }

    private RealmQuery<ReadPostFeed> fetch(long postFeedId) {
        return realm.where(ReadPostFeed.class).equalTo("postFeedId", postFeedId);
    }

    public void destroyIfExist(long partiId) {
        forPartiOrDashboard(partiId).deleteFromRealm();
    }

    public void updateLastStrokedAtSeconds(ReadPostFeed postFeed, long lastStrokedSecondTime) {
        if(lastStrokedSecondTime < 0) {
            postFeed.lastStrokedAt = null;
        } else {
            postFeed.lastStrokedAt = new Date(lastStrokedSecondTime * 1000);

            if(!postFeed.isDashboard()) {
                ReadPostFeed dashboard = forDashboard();
                if (dashboard.lastStrokedAt == null || postFeed.lastStrokedAt.getTime() > dashboard.lastStrokedAt.getTime()) {
                    dashboard.lastStrokedAt = postFeed.lastStrokedAt;
                    if (dashboard.lastReadAt == null) {
                        dashboard.lastReadAt = postFeed.lastStrokedAt;
                    }
                    save(dashboard);
                }
            }
        }
        if(postFeed.lastReadAt == null) {
            postFeed.lastReadAt = postFeed.lastStrokedAt;
        }
        save(postFeed);
    }

    public void save(final ReadPostFeed readPostFeed) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.copyToRealmOrUpdate(readPostFeed);
            }
        });
    }

    public void init(List<Parti> list) {
        for(Parti parti : list) {
            ReadPostFeed postFeed = forPartiOrDashboard(parti.id);
            if(postFeed.lastReadAt == null) {
                postFeed.lastReadAt = new Date();
                save(postFeed);
            }
        }

        ReadPostFeed dashboard = forDashboard();
        if(dashboard.lastReadAt == null) {
            dashboard.lastReadAt = new Date();
            save(dashboard);
        }
    }
}
