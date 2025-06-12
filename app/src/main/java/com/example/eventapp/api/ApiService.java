package com.example.eventapp.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.example.eventapp.models.AuthResponse;
import com.example.eventapp.models.LoginRequest;
import com.example.eventapp.models.RegisterRequest;

public interface ApiService {
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);
} 