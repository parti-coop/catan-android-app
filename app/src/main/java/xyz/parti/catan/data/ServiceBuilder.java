package xyz.parti.catan.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.services.AuthTokenService;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public class ServiceBuilder {
    private static final Retrofit.Builder retrofitbuilder = createDefaultBuilder();

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

    private static void oAuthIntercept(OkHttpClient.Builder httpBuilder, final SessionManager session) {
        httpBuilder.addInterceptor(chain -> {
            PartiAccessToken token = session.getPartiAccessToken();
            return getResponseWithOAuth(chain, token, session);
        });
    }

    private static void oAuthIntercept(OkHttpClient.Builder httpBuilder, final PartiAccessToken token) {
        httpBuilder.addInterceptor(chain -> getResponseWithOAuth(chain, token, null));
    }

    private static Response getResponseWithOAuth(Interceptor.Chain chain, PartiAccessToken token, SessionManager session) throws IOException {
        String originalAccessToken = null;
        if(token != null) {
            originalAccessToken = token.access_token;
        }

        // We need a new client, since we don't want to make another call using our client with access token
        AuthTokenService tokenService = createUnsignedService(AuthTokenService.class);

        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-type", "application/json");
        setAuthHeader(requestBuilder, token);
        requestBuilder = requestBuilder.method(original.method(), original.body());
        Request request = requestBuilder.build();
        Response response = chain.proceed(request);

        if (session != null && response.code() == 401 && originalAccessToken != null) {
            synchronized (retrofitbuilder) { //perform all 401 in sync blocks, to avoid multiply token updates
                String currentAccessToken = token.access_token; //get currently stored token
                if(currentAccessToken != null && currentAccessToken.equals(originalAccessToken)) { //compare current token with token that was stored before, if it was not updated - do update
                    Call<PartiAccessToken> tokenCall = tokenService.getRefreshAccessToken(token.refresh_token,
                            "refresh_token", BuildConfig.PARTI_APP_ID, BuildConfig.PARTI_SECRET_KEY);
                    return getResponseRefreshToken(tokenCall, chain, session, requestBuilder, response);
                }
            }
        }

        return response;
    }

    private static Response getResponseRefreshToken(Call<PartiAccessToken> tokenCall, Interceptor.Chain chain, SessionManager session,
                                                    Request.Builder requestBuilder, Response response) throws AuthFailError {
        try {
            retrofit2.Response<PartiAccessToken> tokenResponse = tokenCall.execute();
            if (tokenResponse.isSuccessful()) {
                PartiAccessToken newToken = tokenResponse.body();
                if(newToken == null || newToken.access_token == null) {
                    return response;
                }

                session.updateAccessToken(newToken);
                setAuthHeader(requestBuilder, newToken);
                Request newRequest = requestBuilder.build();
                return chain.proceed(newRequest); //rep
            } else {
                session.logoutUser();
                throw new AuthFailError();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "Response Error 004 " + e.getMessage(), e);
            throw new AuthFailError();
        }
    }

    private static void setAuthHeader(Request.Builder requestBuilder, PartiAccessToken token) {
        if(token == null || token.access_token == null) return;
        requestBuilder.header("Authorization",
                token.getValidTokenType() + " " + token.access_token);
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
