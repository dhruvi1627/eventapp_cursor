package com.example.eventapp.di;

import android.content.Context;
import com.example.eventapp.api.ApiClient;
import com.example.eventapp.api.ApiService;
import com.example.eventapp.api.EventApiService;
import com.example.eventapp.utils.SessionManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(SessionManager sessionManager) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }
            return chain.proceed(requestBuilder.build());
        };

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("http://192.168.1.2:5000/") // Replace with your actual backend URL
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    ApiClient provideApiClient(SessionManager sessionManager) {
        return new ApiClient(sessionManager);
    }

    @Provides
    @Singleton
    ApiService provideAuthService(ApiClient apiClient) {
        return apiClient.getAuthService();
    }

    @Provides
    @Singleton
    EventApiService provideEventService(ApiClient apiClient) {
        return apiClient.getEventService();
    }
} 