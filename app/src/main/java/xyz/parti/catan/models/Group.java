package xyz.parti.catan.models;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class Group {
    public static final String INDIE_SLUG = "indie";
    public String title;
    public String slug;

    public boolean isIndie() {
        return INDIE_SLUG.equals(slug);
    }
}
