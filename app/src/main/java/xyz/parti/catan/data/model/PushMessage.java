package xyz.parti.catan.data.model;

import org.parceler.Parcel;


@Parcel
public class PushMessage {
    public long id;
    public String title;
    public String body;
    public String type;
    public String param;
    public String priority;
    public String url;
    public long user_id;
    public boolean isSound = false;
    public long timestamp = 0;
}
