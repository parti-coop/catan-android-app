package xyz.parti.catan.services;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import xyz.parti.catan.models.PartiAccessToken;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public interface AuthTokenService {
    @FormUrlEncoded
    @POST("/oauth/token")
    Call<PartiAccessToken> getNewAccessToken(
            @Field("provider") String provider,
            @Field("assertion") String assertion,
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
