package xyz.parti.catan.services;

import com.google.gson.JsonNull;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by dalikim on 2017. 4. 28..
 */

public interface FeedbacksService {
    @FormUrlEncoded
    @POST("/api/v1/feedbacks")
    Call<JsonNull> feedback(@Field("option_id") long option_id, @Field("selected") boolean selected);
}
