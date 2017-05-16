package xyz.parti.catan.data.model;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 5. 16..
 */

@Parcel
public class Setting {
    public String profile_url;
    public String help_url;
    public String terms_url;
    public String privacy_url;

    public Setting() {
        profile_url = "https://parti.xyz/users/edit";
        help_url = "http://docs.parti.xyz/docs/help/";
        terms_url = "https://parti.xyz/terms/";
        privacy_url = "https://parti.xyz/privacy/";
    }

}
