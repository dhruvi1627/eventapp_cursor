package com.example.eventapp.api;

import com.example.eventapp.utils.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.2:5000/"; // Android emulator localhost
    private final SessionManager sessionManager;
    private final Retrofit retrofit;

    @Inject
    public ApiClient(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create auth interceptor to add token to requests
        Interceptor authInterceptor = chain -> {
            okhttp3.Request.Builder requestBuilder = chain.request().newBuilder();
            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }
            return chain.proceed(requestBuilder.build());
        };

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build();

        retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();
    }

    public ApiService getAuthService() {
        return retrofit.create(ApiService.class);
    }

    public EventApiService getEventService() {
        return retrofit.create(EventApiService.class);
    }
} 