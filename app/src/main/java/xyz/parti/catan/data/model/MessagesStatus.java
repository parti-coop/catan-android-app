package xyz.parti.catan.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class MessagesStatus extends RealmObject {
    @PrimaryKey
    public long user_id;
    public long last_created_message_id = 0;
    public long last_local_read_messag_id = 0;
    public long last_server_read_messag_id = 0;

    public boolean hasUnread() {
        return getLastReadMessageId() < last_created_message_id;
    }

    private long getLastReadMessageId() {
        return Math.max(last_local_read_messag_id, last_server_read_messag_id);
    }
}
