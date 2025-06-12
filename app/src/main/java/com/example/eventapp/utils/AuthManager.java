package com.example.eventapp.utils;

import android.content.Context;
import android.util.Log;
import com.example.eventapp.api.ApiService;
import com.example.eventapp.models.AuthResponse;
import com.example.eventapp.models.LoginRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import javax.inject.Inject;
import javax.inject.Singleton;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class AuthManager {
    private static final String TAG = "AuthManager";
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Context context;

    @Inject
    public AuthManager(
        ApiService apiService,
        SessionManager sessionManager,
        @ApplicationContext Context context
    ) {
        this.apiService = apiService;
        this.sessionManager = sessionManager;
        this.context = context;
    }

    public void login(String email, String password, AuthCallback callback) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess() && authResponse.getData() != null) {
                        // Save token and user info
                        sessionManager.saveToken(authResponse.getData().getToken());
                        if (authResponse.getUser() != null) {
                            sessionManager.saveUserEmail(authResponse.getUser().getEmail());
                            sessionManager.saveUserId(authResponse.getUser().getId());
                            if (authResponse.getUser().getName() != null) {
                                sessionManager.saveUserName(authResponse.getUser().getName());
                            }
                        }
                        callback.onSuccess();
                    } else {
                        callback.onError(authResponse.getMessage());
                    }
                } else {
                    String errorMessage = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Login network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void logout() {
        sessionManager.logout();
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }
} 