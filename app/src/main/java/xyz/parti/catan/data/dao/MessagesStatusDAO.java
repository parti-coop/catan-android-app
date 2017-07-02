package xyz.parti.catan.data.dao;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import xyz.parti.catan.data.model.Message;
import xyz.parti.catan.data.model.MessagesStatus;
import xyz.parti.catan.data.model.User;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class MessagesStatusDAO {
    private final Realm realm;
    private MessagesStatus result;

    public MessagesStatusDAO(Realm realm) {
        this.realm = realm;
    }

    public void watch(User currentUser, final MessagesStatusDAO.ChangeListener listener) {
        if(result == null) {
            result = findMessagesStatus(realm, currentUser.id);
            if(result == null) {
                realm.beginTransaction();
                result = realm.createObject(MessagesStatus.class, currentUser.id);
                realm.commitTransaction();
            }
        }
        listener.onInit(result);
        result.addChangeListener(new RealmChangeListener<MessagesStatus>() {
            @Override
            public void onChange(MessagesStatus messagesStatus) {
                listener.onChange(realm.copyFromRealm(messagesStatus));
            }
        });
    }

    public void unwatchAll() {
        if(result != null) {
            result.removeAllChangeListeners();
        }
    }

    public void saveLocalStatus(final User currentUser, final List<Message> messages) {
        if(messages.size() <= 0) {
            return;
        }
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                long maxId = 0;
                for(Message message : messages) {
                    maxId = Math.max(message.id, maxId);
                }

                MessagesStatus messagesStatus = findMessagesStatus(bgRealm, currentUser.id);
                messagesStatus.last_local_read_messag_id = maxId;
                bgRealm.copyToRealmOrUpdate(messagesStatus);
            }
        });
    }

    public void saveServerStatusSync(final User currentUser, final MessagesStatus statusFromServer) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                MessagesStatus messagesStatus = findMessagesStatus(bgRealm, currentUser.id);
                messagesStatus.last_created_message_id = statusFromServer.last_created_message_id;
                messagesStatus.last_server_read_messag_id = statusFromServer.last_server_read_messag_id;
                bgRealm.copyToRealmOrUpdate(messagesStatus);
            }
        });
    }

    public void saveServerCreatedMessageIdSyncIfNew(final long userId, final long messageId) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                MessagesStatus messagesStatus = findMessagesStatus(bgRealm, userId);
                if(messagesStatus.last_created_message_id < messageId) {
                    messagesStatus.last_created_message_id = messageId;
                    bgRealm.copyToRealmOrUpdate(messagesStatus);
                }
            }
        });
    }

    public void saveLocalReadMessageIdIfNew(final long userId, final long messageId) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                MessagesStatus messagesStatus = findMessagesStatus(bgRealm, userId);
                if(messagesStatus.last_local_read_messag_id < messageId) {
                    messagesStatus.last_local_read_messag_id = messageId;
                    bgRealm.copyToRealmOrUpdate(messagesStatus);
                }
            }
        });
    }

    private MessagesStatus findMessagesStatus(Realm aRealm, long userId) {
        return aRealm.where(MessagesStatus.class).equalTo("user_id", userId).findFirst();
    }

    public interface ChangeListener {
        void onInit(MessagesStatus messagesStatus);
        void onChange(MessagesStatus messagesStatus);
    }
}
