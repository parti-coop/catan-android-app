package xyz.parti.catan.data.services;

import com.google.gson.JsonNull;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import xyz.parti.catan.data.model.Comment;

/**
 * Created by dalikim on 2017. 5. 15..
 */

public interface DeviceTokenService {
    @FormUrlEncoded
    @POST("/api/v1/device_tokens")
    Flowable<Response<JsonNull>> create(@Field("registration_id") String token);

    @DELETE("/api/v1/device_tokens")
    Flowable<Response<JsonNull>> destroy(@Query("registration_id") String token);
}
