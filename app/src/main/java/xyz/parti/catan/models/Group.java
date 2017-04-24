package xyz.parti.catan.models;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Group {
    public static final String INDIE_SLUG = "indie";
    public String title;
    public String slug;

    public boolean isIndie() {
        return INDIE_SLUG.equals(slug);
    }
}
