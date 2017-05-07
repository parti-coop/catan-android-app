package xyz.parti.catan.data.services;

import io.reactivex.Flowable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import xyz.parti.catan.data.model.User;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public interface UsersService {
    @GET("/api/v1/users/me")
    Flowable<Response<User>> getCurrentUser();
}
