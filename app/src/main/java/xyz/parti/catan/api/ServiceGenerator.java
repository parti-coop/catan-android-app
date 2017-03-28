package xyz.parti.catan.api;

import android.content.Context;

import com.facebook.AccessToken;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.services.AuthTokenService;
import xyz.parti.catan.sessions.SessionManager;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public class ServiceGenerator {
    private static OkHttpClient.Builder httpClient;
    private static Retrofit.Builder builder;

    public static <S> S createUnsignedService(Class<S> serviceClass) {
        httpClient = new OkHttpClient.Builder();
        builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createNoRefreshService(Class<S> serviceClass, final PartiAccessToken currentToken) {
        return createService(serviceClass, null, currentToken);
    }

    public static <S> S createService(Class<S> serviceClass, final SessionManager session) {
        return createService(serviceClass, session, session.getPartiAccessToken());
    }

    private static <S> S createService(Class<S> serviceClass, final SessionManager session, final PartiAccessToken currentToken) {
        httpClient = new OkHttpClient.Builder();
        builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        if(currentToken != null) {
            final PartiAccessToken token = currentToken;
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-type", "application/json")
                            .header("Authorization",
                                    token.getValidTokenType() + " " + token.access_token)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            if(session != null) {
                httpClient.authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (responseCount(response) >= 2) {
                            // If both the original call and the call with refreshed token failed,
                            // it will probably keep failing, so don't try again.
                            return null;
                        }

                        // We need a new client, since we don't want to make another call using our client with access token
                        AuthTokenService tokenService = createUnsignedService(AuthTokenService.class);
                        Call<PartiAccessToken> tokenCall = tokenService.getRefreshAccessToken(currentToken.refresh_token,
                                "refresh_token", BuildConfig.PARTI_APP_ID, BuildConfig.PARTI_SECRET_KEY);

                        try {
                            retrofit2.Response<PartiAccessToken> tokenResponse = tokenCall.execute();
                            if (tokenResponse.code() == 200) {
                                PartiAccessToken newToken = tokenResponse.body();
                                session.updateAccessToken(newToken);
                                return response.request().newBuilder()
                                        .header("Authorization", newToken.getValidTokenType() + " " + newToken.access_token)
                                        .build();
                            } else {
                                return null;
                            }
                        } catch (IOException e) {
                            return null;
                        }
                    }
                });
            }
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
