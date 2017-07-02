package xyz.parti.catan.data.services;

import io.reactivex.Flowable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import xyz.parti.catan.data.model.PartiAccessToken;


public interface AuthTokenService {
    @FormUrlEncoded
    @POST("/oauth/token")
    Flowable<Response<PartiAccessToken>> getNewAccessTokenByAssertion(
            @Field("provider") String provider,
            @Field("assertion") String assertion,
            @Field("secret") String secret,
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret);

    @FormUrlEncoded
    @POST("/oauth/token")
    Flowable<Response<PartiAccessToken>> getNewAccessTokenByCredentials(
            @Field("email") String email,
            @Field("password") String password,
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret);

    @FormUrlEncoded
    @POST("/oauth/token")
    Call<PartiAccessToken> getRefreshAccessToken(
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret);
}
