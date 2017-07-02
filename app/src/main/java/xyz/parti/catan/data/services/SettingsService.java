package xyz.parti.catan.data.services;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.GET;
import xyz.parti.catan.data.model.Setting;


public interface SettingsService {
    @GET("/api/v1/settings")
    Flowable<Response<Setting>> getAll();
}
