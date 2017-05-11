package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class LinkSource {
    public String url;
    public String title;
    public String title_or_url;
    public String body;
    public String site_name;
    public String image_url;
    public Boolean is_video;
    public String video_embeded_code;
    public String video_app_url;

    public Collection<? extends String> getPreloadImageUrls() {
        List<String> result = new ArrayList<>();
        result.add(image_url);
        return result;
    }
}
