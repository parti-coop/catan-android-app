package xyz.parti.catan.data.services;

import com.google.gson.JsonNull;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by dalikim on 2017. 4. 26..
 */

public interface VotingsService {
    @FormUrlEncoded
    @POST("/api/v1/votings")
    Call<JsonNull> voting(@Field("poll_id") long pollId, @Field("choice") String choice);
}
