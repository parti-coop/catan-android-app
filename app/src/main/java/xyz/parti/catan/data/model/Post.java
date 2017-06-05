package xyz.parti.catan.data.model;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Post implements RecyclableModel {
    public static final String IS_UPVOTED_BY_ME = "is_upvoted_by_me";
    public static final String PLAYLOAD_LATEST_COMMENT = "latest_comment";

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
    public Boolean is_upvoted_by_me;
    public Long upvotes_count;
    public Long comments_count;
    public Comment[] latest_comments;
    public LinkSource link_source;
    public Poll poll;
    public Survey survey;
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

    public boolean hasMoreComments() {
        return (this.comments_count > 0 && this.latest_comments != null && this.comments_count > this.latest_comments.length);
    }

    @Override
    public boolean isSame(Object other) {
        return other != null && other instanceof Post && id != null && id.equals(((Post)other).id);
    }

    @Override
    public List<String> getPreloadImageUrls() {
        List<String> result = new ArrayList<>();
        result.addAll(parti.getPreloadImageUrls());
        if(user != null) {
            result.addAll(user.getPreloadImageUrls());
        }
        if(link_source != null) {
            result.addAll(link_source.getPreloadImageUrls());
        }
        if(latest_comments != null) {
            for(Comment comment : latest_comments) {
                result.addAll(comment.getPreloadImageUrls());
            }
        }
        if(file_sources != null) {
            for(FileSource fileSource : file_sources) {
                result.addAll(fileSource.getPreloadImageUrls());
            }
        }
        return result;
    }

    public void addComment(Comment comment) {
        this.comments_count++;
        List<Comment> temp = new ArrayList<>(Arrays.asList(this.latest_comments));
        temp.add(comment);
        latest_comments = temp.toArray(new Comment[temp.size()]);
    }

    public void toggleUpvoting() {
        if(is_upvoted_by_me) {
            this.upvotes_count--;
            this.is_upvoted_by_me = false;
        } else {
            this.upvotes_count++;
            this.is_upvoted_by_me = true;
        }
    }

    public void toggleCommentUpvoting(Comment comment) {
        comment.toggleUpvoting();
        for(Comment currentComment : latest_comments) {
            if(comment != null && comment.id != null && comment.id.equals(currentComment.id)) {
                if(currentComment != comment) {
                    currentComment.toggleUpvoting();
                }
            }
        }
    }

    public List<Comment> lastComments(int limit) {
        if(latest_comments == null) return new ArrayList<>();
        List<Comment> result = new ArrayList<>();
        if (latest_comments.length >= limit) {
            for (int i = latest_comments.length - limit; i < latest_comments.length; i++) {
                result.add(latest_comments[i]);
            }
        } else {
            result.addAll(Arrays.asList(latest_comments));
        }
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + " - post id : " + id;
    }
}
