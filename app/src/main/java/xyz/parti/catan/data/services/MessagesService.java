package xyz.parti.catan.data.services;

import com.google.gson.JsonNull;

import io.reactivex.Flowable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import xyz.parti.catan.data.model.Message;
import xyz.parti.catan.data.model.MessagesStatus;
import xyz.parti.catan.data.model.Page;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public interface MessagesService {
    @GET("/api/v1/messages")
    Flowable<Response<Page<Message>>> getMessagesLatest();

    @GET("/api/v1/messages")
    Flowable<Response<Page<Message>>> getMessagesAfter(
            @Query("last_id") long lastId);

    @GET("/api/v1/messages/status")
    Call<MessagesStatus> getStatus();

    @FormUrlEncoded
    @POST("/api/v1/messages/last_read_message")
    Flowable<Response<JsonNull>> read(@Field("last_id") long messageId);
}
