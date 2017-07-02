package xyz.parti.catan.data.services;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.GET;
import xyz.parti.catan.data.model.User;


public interface UsersService {
    @GET("/api/v1/users/me")
    Flowable<Response<User>> getCurrentUser();
}
