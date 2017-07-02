package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Parcel
public class User {
    public Long id;
    public String email;
    public String nickname;
    public String image_url;
    public String profile_url;

    public Collection<? extends String> getPreloadImageUrls() {
        List<String> result = new ArrayList<>();
        result.add(image_url);
        return result;
    }
}

