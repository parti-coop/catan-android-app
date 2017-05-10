package xyz.parti.catan.data.services;

import com.google.gson.JsonObject;

import io.reactivex.Flowable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Post;

/**
 * Created by dalikim on 2017. 5. 6..
 */

public interface AppVersionService {
    @GET("/api/v1/app_version/last")
    Flowable<Response<JsonObject>> getLastVersion();
}
