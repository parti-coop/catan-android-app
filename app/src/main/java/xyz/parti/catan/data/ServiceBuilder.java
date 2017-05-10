package xyz.parti.catan.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.model.Update;
import xyz.parti.catan.data.services.AuthTokenService;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public class ServiceBuilder {
    private static Retrofit.Builder retrofitbuilder = createDefaultBuilder();

    public static <S> S createUnsignedService(Class<S> serviceClass) {
        OkHttpClient.Builder httpClient = getHttpClientBuilder();

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitbuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createNoRefreshService(Class<S> serviceClass, PartiAccessToken token) {
        final OkHttpClient.Builder httpClient = getHttpClientBuilder();

        oAuthIntercept(httpClient, token);

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitbuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, @NonNull final SessionManager session) {
        final OkHttpClient.Builder httpClient = getHttpClientBuilder();

        oAuthIntercept(httpClient, session);
        refreshableAuthenticate(httpClient, session);

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitbuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    @NonNull
    private static OkHttpClient.Builder getHttpClientBuilder() {
        return new OkHttpClient.Builder()
                //.addNetworkInterceptor(new StethoInterceptor())
                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .readTimeout(30000, TimeUnit.MILLISECONDS);
    }

    private static void refreshableAuthenticate(OkHttpClient.Builder httpBuilder, final SessionManager session) {
        final PartiAccessToken currentToken = session.getPartiAccessToken();
        httpBuilder.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                PartiAccessToken prefToken = session.getPartiAccessToken();
                if (currentToken == null || prefToken == null) {
                    throw new AuthFailError();
                }

                if(!prefToken.access_token.equals(currentToken.access_token)) {
                    return response.request().newBuilder()
                            .header("Authorization", prefToken.getValidTokenType() + " " + prefToken.access_token)
                            .build();
                }

                if (responseCount(response) >= 2) {
                    // If both the original call and the call with refreshed token failed,
                    // it will probably keep failing, so don't try again.
                    session.logoutUser();
                    throw new AuthFailError();
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
                        session.logoutUser();
                        throw new AuthFailError();
                    }
                } catch (IOException e) {
                    Log.e(Constants.TAG, "Response Error 004 " + e.getMessage(), e);
                    session.logoutUser();
                    throw new AuthFailError();
                }
            }
        });
    }

    private static void oAuthIntercept(OkHttpClient.Builder httpBuilder, final SessionManager session) {
        httpBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                PartiAccessToken token = session.getPartiAccessToken();
                return getResponseWithOAuth(chain, token);
            }
        });
    }

    private static void oAuthIntercept(OkHttpClient.Builder httpBuilder, final PartiAccessToken token) {
        httpBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return getResponseWithOAuth(chain, token);
            }
        });
    }

    private static Response getResponseWithOAuth(Interceptor.Chain chain, PartiAccessToken token) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-type", "application/json");
        if(token != null) {
            requestBuilder = requestBuilder.header("Authorization",
                    token.getValidTokenType() + " " + token.access_token);
        }
        requestBuilder = requestBuilder.method(original.method(), original.body());

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private static Retrofit.Builder createDefaultBuilder() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    private static class AuthFailError extends IOException {}
}
