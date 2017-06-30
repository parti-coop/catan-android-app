package xyz.parti.catan.data.services;

import com.google.gson.JsonNull;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by dalikim on 2017. 6. 30..
 */

public interface OptionsService {
    @FormUrlEncoded
    @POST("/api/v1/options")
    Flowable<Response<JsonNull>> create(@Field("option[survey_id]") Long surveyId, @Field("option[body]") String body);
}
