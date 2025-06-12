package com.example.eventapp.api;

import com.example.eventapp.models.Event;
import com.example.eventapp.models.ApiResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface EventApiService {
    @GET("api/events")
    Call<ApiResponse<List<Event>>> getAllEvents(@Header("Authorization") String token);

    // FIXED: Changed from "events/my-events" to "api/events/my-events"
    @GET("api/events/my-events")
    Call<ApiResponse<List<Event>>> getUserEvents(@Header("Authorization") String token);

    @GET("api/events/category/{categoryId}")
    Call<ApiResponse<List<Event>>> getEventsByCategory(
            @Header("Authorization") String token,
            @Path("categoryId") String categoryId
    );

    @GET("api/events/{id}")
    Call<ApiResponse<Event>> getEventById(
            @Header("Authorization") String token,
            @Path("id") String eventId
    );

    @POST("api/events")
    Call<ApiResponse<Event>> createEvent(
            @Header("Authorization") String token,
            @Body Event event
    );

    @PUT("api/events/{id}")
    Call<ApiResponse<Event>> updateEvent(
            @Header("Authorization") String token,
            @Path("id") String eventId,
            @Body Event event
    );

    @DELETE("api/events/{id}")
    Call<ApiResponse<Void>> deleteEvent(
            @Header("Authorization") String token,
            @Path("id") String eventId
    );

    @POST("api/events/{id}/join")
    Call<ApiResponse<Event>> joinEvent(
            @Header("Authorization") String token,
            @Path("id") String eventId
    );

    @DELETE("api/events/{id}/join")
    Call<ApiResponse<Event>> leaveEvent(
            @Header("Authorization") String token,
            @Path("id") String eventId
    );

    @Multipart
    @POST("api/events")
    Call<ApiResponse<Event>> createEventWithImage(
            @Header("Authorization") String token,
            @Part("event") Event event,
            @Part MultipartBody.Part image
    );

    @Multipart
    @PUT("api/events/{id}")
    Call<ApiResponse<Event>> updateEventWithImage(
            @Header("Authorization") String token,
            @Path("id") String eventId,
            @Part("event") Event event,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("api/events")
    Call<ApiResponse<Event>> createEventWithFormData(
            @Header("Authorization") String token,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("date") RequestBody date,
            @Part("location") RequestBody location,
            @Part("capacity") RequestBody capacity,
            @Part("price") RequestBody price,
            @Part("category") RequestBody category,
            @Part MultipartBody.Part image
    );
}