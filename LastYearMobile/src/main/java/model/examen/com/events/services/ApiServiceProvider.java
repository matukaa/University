package model.examen.com.events.services;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.apache.commons.lang.time.DateUtils;

import java.io.IOException;
import java.util.Date;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import roboguice.inject.InjectView;

/**
 * Created by iulia on 1/10/2017.
 */

public class ApiServiceProvider implements Provider<ApiService> {

    private static Context context;
    protected static HttpLoggingInterceptor logging;
    private static OkHttpClient httpClient;
    private static Retrofit.Builder builder;

    private static final String BASE_URL = "http://10.0.2.2:3000/";

    @Inject
    public ApiServiceProvider(Context context, OkHttpClient httpClient, SharedPreferences sharedPreferences) {
        this.context = context;

        logging = new HttpLoggingInterceptor();
        this.httpClient = httpClient;

        builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    @Override
    public ApiService get() {

        Log.d(getClass().getSimpleName(), " get call");
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestbuilder = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json");

                Request request = requestbuilder.build();
                return chain.proceed(request);
            }
        });

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.interceptors().add(logging);

        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(ApiService.class);
    }
}
