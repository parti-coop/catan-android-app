package xyz.parti.catan.services;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.models.Update;
import xyz.parti.catan.models.User;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public interface PostsService {
    @GET("/api/v1/posts/dashboard_latest")
    Call<Page<Post>> getDashBoardLastest();

    @GET("/api/v1/posts/dashboard_after")
    Call<Page<Post>> getDashboardAfter(
            @Query("last_id") long lastId);

    @GET("/api/v1/posts/has_updated")
    Call<Update> hasUpdated(
            @Query("last_stroked_at") Date lastStrokedAt);

    @GET("/api/v1/posts/{id}/download_file/{file_source_id}")
    @Streaming
    Call<ResponseBody> downloadFile(
            @Path(value = "id") Long id,
            @Path(value = "file_source_id") Long fileSourceId);

    @GET("/api/v1/posts/{id}")
    Call<Post> getPost(@Path(value = "id") Long id);
}
