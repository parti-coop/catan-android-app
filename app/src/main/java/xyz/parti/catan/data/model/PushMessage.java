package xyz.parti.catan.data.model;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 5. 14..
 */

@Parcel
public class PushMessage {
    public String title;
    public String body;
    public String type;
    public String param;
    public String priority;
    public String url;
    public long user_id;
}
