package xyz.parti.catan.data.services;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.GET;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.model.Post;

/**
 * Created by dalikim on 2017. 5. 24..
 */

public interface PartiesService {
    @GET("/api/v1/parties/my_joined")
    Flowable<Response<Parti[]>> getMyJoined();
}
