package xyz.parti.catan.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Query;
import xyz.parti.catan.models.Page;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.models.User;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public interface PostsService {
    @GET("/api/v1/posts/dashboard_latest")
    Call<Page<Post>> getDashBoardLastest();

    @GET("/api/v1/posts/dashboard_after")
    Call<Page<Post>> getDashboardAfter(
            @Query("last_id") long last_id);
}
