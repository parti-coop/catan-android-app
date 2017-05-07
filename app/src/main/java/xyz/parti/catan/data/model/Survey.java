package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Created by dalikim on 2017. 4. 27..
 */

@Parcel
public class Survey {
    public long id;
    public int feedbacks_count;
    public int feedback_users_count;
    public Date expires_at;
    public boolean multiple_select;
    public Option[] options;
    public boolean is_open;
    public boolean is_feedbacked_by_me;
    public String remain_time_human;
}
