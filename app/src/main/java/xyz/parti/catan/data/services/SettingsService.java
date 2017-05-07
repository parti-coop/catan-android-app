package xyz.parti.catan.data.services;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public interface SettingsService {
    @GET("/api/v1/settings")
    Call<JsonElement> getAll();
}
