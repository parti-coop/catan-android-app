package xyz.parti.catan.data.services;

import com.google.gson.JsonObject;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.GET;


public interface AppVersionService {
    @GET("/api/v1/app_version/last")
    Flowable<Response<JsonObject>> getLastVersion();
}
