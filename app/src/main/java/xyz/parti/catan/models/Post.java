package xyz.parti.catan.models;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Post {
    public Long id;
    public Boolean full;
    public String parsed_title;
    public String parsed_body;
    public String truncated_parsed_body;
    public String specific_desc_striped_tags;
    public Parti parti;
    public User user;
    public Date created_at;
    public Date last_stroked_at;
    public Boolean is_upvotable;
    public Long upvotes_count;
    public User[] latest_upvote_users;
    public Upvote[] latest_upvotes;
    public Long comments_count;
    public Comment[] latest_comments;
    public LinkSource link_source;
    public Poll poll;
    public Share share;
    public FileSource[] file_sources;

    private transient List<FileSource> _imageFileSources = null;
    private transient List<FileSource> _docFileSources = null;

    public List<FileSource> getImageFileSources() {
        if(_imageFileSources != null) {
            return this._imageFileSources;
        }

        _imageFileSources = new ArrayList<>();
        for(FileSource fileSource: file_sources) {
            if(fileSource.isImage()) {
                _imageFileSources.add(fileSource);
            }
        }
        return this._imageFileSources;
    }

    public List<FileSource> getDocFileSources() {
        if(_docFileSources != null) {
            return this._docFileSources;
        }

        _docFileSources = new ArrayList<>();
        for(FileSource fileSource: file_sources) {
            if(fileSource.isDoc()) {
                _docFileSources.add(fileSource);
            }
        }
        return this._docFileSources;
    }
}
