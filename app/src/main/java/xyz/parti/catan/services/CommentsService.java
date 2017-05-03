package xyz.parti.catan.services;

import com.google.gson.JsonObject;

import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import xyz.parti.catan.models.Comment;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public interface CommentsService {
    @GET("/api/v1/posts/{post_id}/comments")
    Call<Page<Comment>> getComments(@Path(value = "post_id") Long postId);

    @GET("/api/v1/posts/{post_id}/comments")
    Call<Page<Comment>> getComments(@Path(value = "post_id") Long postId, @Query("last_comment_id") long lastCommentId);

    @FormUrlEncoded
    @POST("/api/v1/comments")
    Call<Comment> createComment(@Field(value= "comment[post_id]") Long postId, @Field(value="comment[body]") String body);
}
