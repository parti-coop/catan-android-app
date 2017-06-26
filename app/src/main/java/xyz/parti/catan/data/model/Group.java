package xyz.parti.catan.data.model;

import android.support.annotation.NonNull;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import xyz.parti.catan.helper.OrderingByKoreanEnglishNumbuerSpecial;

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

    public static List<Group> sortByTitle(Set<Group> groups) {
        ArrayList<Group> result = new ArrayList<>(groups);
        Collections.sort(result, new Comparator<Group>() {
            public int compare(Group left, Group right) {
                return OrderingByKoreanEnglishNumbuerSpecial.compare(left.title, right.title);
            }
        });
        return result;
    }
}
