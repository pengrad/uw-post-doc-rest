package io.github.pengrad.uw_post_doc_rest;

import com.google.gson.JsonElement;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * stas
 * 8/9/15
 */
public class ApiManager {

    public static Api createApi() {
        return new RestAdapter.Builder()
                .setEndpoint("http://pengrad-pengrad.rhcloud.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build()
                .create(Api.class);
    }

    public interface Api {
        @GET("/json")
        void sendData(@Query("data") String data, Callback<JsonElement> callback);
    }
}
