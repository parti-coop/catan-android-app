package xyz.parti.catan.data.services;

import com.google.gson.JsonNull;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface DeviceTokensService {
    @FormUrlEncoded
    @POST("/api/v1/device_tokens")
    Flowable<Response<JsonNull>> create(@Field("registration_id") String token, @Field("application_id") String applicationId);

    @DELETE("/api/v1/device_tokens")
    Flowable<Response<JsonNull>> destroy(@Query("registration_id") String token);
}
