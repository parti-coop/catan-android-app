package xyz.parti.catan.services;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public interface AppVersionService {
    @GET("/api/v1/app_version/last")
    Call<JsonObject> getLastVersion();
}
