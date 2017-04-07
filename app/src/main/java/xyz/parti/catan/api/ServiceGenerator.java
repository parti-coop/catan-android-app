package xyz.parti.catan.api;

import android.util.Log;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import xyz.parti.catan.BuildConfig;
import xyz.parti.catan.Constants;
import xyz.parti.catan.helper.APIHelper;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.services.AuthTokenService;
import xyz.parti.catan.sessions.SessionManager;

/**
 * Created by dalikim on 2017. 3. 27..
 */

public class ServiceGenerator {
    private static Retrofit.Builder retrofitbuilder = APIHelper.createDefaultBuilder();

    public static <S> S createUnsignedService(Class<S> serviceClass) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitbuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createNoRefreshService(Class<S> serviceClass, PartiAccessToken token) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        oAuthIntercept(httpClient, token);

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitbuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, final SessionManager session) {
        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        oAuthIntercept(httpClient, session);
        refreshableAuthenticate(httpClient, session);

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitbuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    private static void refreshableAuthenticate(OkHttpClient.Builder httpBuilder, final SessionManager session) {
        final PartiAccessToken currentToken = session.getPartiAccessToken();
        httpBuilder.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                PartiAccessToken prefToken = session.getPartiAccessToken();
                if (prefToken.access_token != currentToken.access_token) {
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
                .header("Content-type", "application/json")
                .header("Authorization",
                        token.getValidTokenType() + " " + token.access_token)
                .method(original.method(), original.body());

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

    static class AuthFailError extends IOException {}
}
