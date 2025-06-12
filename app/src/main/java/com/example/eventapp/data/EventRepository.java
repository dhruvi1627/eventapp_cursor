package com.example.eventapp.data;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.ApiResponse;
import com.example.eventapp.api.EventApiService;
import com.example.eventapp.api.ApiConfig;
import com.example.eventapp.utils.SessionManager;
import java.util.List;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import dagger.hilt.android.scopes.ActivityScoped;
import javax.inject.Inject;

@ActivityScoped
public class EventRepository {
    private static final String TAG = "EventRepository";
    private final EventApiService eventApiService;
    private final SessionManager sessionManager;
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event> selectedEventLiveData = new MutableLiveData<>();

    @Inject
    public EventRepository(EventApiService eventApiService, SessionManager sessionManager) {
        this.eventApiService = eventApiService;
        this.sessionManager = sessionManager;
    }

    public void fetchAllEvents() {
        String token = sessionManager.getToken();
        if (token == null) {
            errorLiveData.setValue("Authentication required");
            return;
        }

        eventApiService.getAllEvents("Bearer " + token).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventsLiveData.setValue(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to fetch events";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, errorMsg);
                    }
                } else {
                    Log.e(TAG, "Error fetching events: " + response.message());
                    errorLiveData.setValue("Failed to fetch events");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch events", t);
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public LiveData<List<Event>> getEvents() {
        return eventsLiveData;
    }

    public LiveData<Event> getEventById(String id) {
        String token = sessionManager.getToken();
        if (token == null) {
            errorLiveData.setValue("Authentication required");
            return eventLiveData;
        }

        eventApiService.getEventById(id, "Bearer " + token).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventLiveData.setValue(apiResponse.getData());
                    } else {
                        errorLiveData.setValue(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to fetch event");
                    }
                } else {
                    errorLiveData.setValue("Failed to fetch event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
        return eventLiveData;
    }

    public LiveData<Event> createEvent(Event event) {
        String token = sessionManager.getToken();
        if (token == null) {
            errorLiveData.setValue("Authentication required");
            return eventLiveData;
        }

        eventApiService.createEvent("Bearer " + token, event).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventLiveData.setValue(apiResponse.getData());
                    } else {
                        errorLiveData.setValue(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to create event");
                    }
                } else {
                    errorLiveData.setValue("Failed to create event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
        return eventLiveData;
    }

    public LiveData<Event> updateEvent(String id, Event event) {
        String token = sessionManager.getToken();
        if (token == null) {
            errorLiveData.setValue("Authentication required");
            return eventLiveData;
        }

        eventApiService.updateEvent(id, "Bearer " + token, event).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventLiveData.setValue(apiResponse.getData());
                    } else {
                        errorLiveData.setValue(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to update event");
                    }
                } else {
                    errorLiveData.setValue("Failed to update event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
        return eventLiveData;
    }

    public LiveData<Boolean> deleteEvent(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String token = sessionManager.getToken();
        if (token == null) {
            errorLiveData.setValue("Authentication required");
            result.setValue(false);
            return result;
        }

        eventApiService.deleteEvent(id, "Bearer " + token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    result.setValue(apiResponse.isSuccess());
                    if (!apiResponse.isSuccess()) {
                        errorLiveData.setValue(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to delete event");
                    }
                } else {
                    result.setValue(false);
                    errorLiveData.setValue("Failed to delete event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                result.setValue(false);
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
        return result;
    }

    public LiveData<Event> joinEvent(String id) {
        String token = sessionManager.getToken();
        if (token == null) {
            errorLiveData.setValue("Authentication required");
            return eventLiveData;
        }

        eventApiService.joinEvent("Bearer " + token, id).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventLiveData.setValue(apiResponse.getData());
                    } else {
                        errorLiveData.setValue(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to join event");
                    }
                } else {
                    errorLiveData.setValue("Failed to join event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                errorLiveData.setValue("Network error: " + t.getMessage());
            }
        });
        return eventLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void setSelectedEvent(Event event) {
        selectedEventLiveData.setValue(event);
    }

    public LiveData<Event> getSelectedEvent() {
        return selectedEventLiveData;
    }
} 