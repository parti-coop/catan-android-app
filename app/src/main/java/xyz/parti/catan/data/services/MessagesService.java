package xyz.parti.catan.data.services;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.parti.catan.data.model.Member;
import xyz.parti.catan.data.model.Message;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.Post;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public interface MessagesService {
    @GET("/api/v1/messages")
    Flowable<Response<Page<Message>>> getMessagesLatest();

    @GET("/api/v1/messages")
    Flowable<Response<Page<Message>>> getMessagesAfter(
            @Query("last_id") long lastId);
}
