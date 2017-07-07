package xyz.parti.catan.data.dao;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import xyz.parti.catan.data.model.Parti;

public class PartiDAO {
    private RealmResults<Parti> result;
    private Realm realm;

    public PartiDAO(Realm realm) {
        this.realm = realm;
    }

    public void watchAll(final ChangeListener listener) {
        if(result == null) {
            result = realm.where(Parti.class).findAllAsync();
        }
        result.addChangeListener(new RealmChangeListener<RealmResults<Parti>>() {
            @Override
            public void onChange(RealmResults<Parti> list) {
                listener.onChange(realm.copyFromRealm(list));
            }
        });
    }

    public void unwatchAll() {
        if(result != null) {
            result.removeAllChangeListeners();
        }
    }

    public void save(final List<Parti> parties, Realm.Transaction.OnSuccess onSuccess) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                for(Parti parti : parties) {
                    bgRealm.copyToRealmOrUpdate(parti);
                }
                List<Long> ids = new ArrayList<>();
                for(Parti parti : parties) {
                    ids.add(parti.id);
                }
                Long[] idArray = ids.toArray(new Long[parties.size()]);
                if(idArray.length > 0) {
                    bgRealm.where(Parti.class).not().in("id", idArray).findAll().deleteAllFromRealm();
                }
            }
        }, onSuccess, null);
    }

    public interface ChangeListener {
        void onChange(List<Parti> list);
    }
}
