package xyz.parti.catan.data.model;

import android.support.annotation.NonNull;

import org.parceler.Parcel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Group extends RealmObject implements Comparable<Group> {
    public static final String INDIE_SLUG = "indie";

    public String title;
    @PrimaryKey
    public String slug;

    public boolean isIndie() {
        return INDIE_SLUG.equals(slug);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass() || slug == null) return false;
        Group that = (Group) o;
        return slug.equals(that.slug);
    }

    @Override
    public int compareTo(@NonNull Group o) {
        if(o.title == null || o.slug == null) {
            return 1;
        }

        if(o.slug.equals(slug)) return 0;
        return o.title.compareTo(title) * -1;
    }
}
