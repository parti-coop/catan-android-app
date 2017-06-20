package xyz.parti.catan.ui.binder;

import xyz.parti.catan.data.model.Comment;

/**
 * Created by dalikim on 2017. 6. 20..
 */
public class CommentDiff {
    private final Comment comment;
    private final String payload;

    public CommentDiff(Comment comment, String payload) {
        this.comment = comment;
        this.payload = payload;
    }

    public Comment getComment() {
        return comment;
    }

    public String getPayload() {
        return payload;
    }
}
