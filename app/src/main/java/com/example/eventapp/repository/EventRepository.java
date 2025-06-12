package com.example.eventapp.repository;

import android.util.Log;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.ApiResponse;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.eventapp.api.EventApiService;
import com.example.eventapp.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import dagger.hilt.android.scopes.ActivityScoped;
import javax.inject.Inject;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import java.io.IOException;

@ActivityScoped
public class EventRepository {
    private static final String TAG = "EventRepository";
    private final EventApiService eventApiService;
    private final SessionManager sessionManager;
    private final MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @Inject
    public EventRepository(EventApiService eventApiService, SessionManager sessionManager) {
        this.eventApiService = eventApiService;
        this.sessionManager = sessionManager;
    }

    public LiveData<List<Event>> getEventsLiveData() {
        return eventsLiveData;
    }

    public LiveData<Event> getEventLiveData() {
        return eventLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<List<Event>> getEvents() {
        String token = sessionManager.getToken();
        if (token == null) {
            String errorMsg = "Authentication required";
            errorLiveData.setValue(errorMsg);
            Log.e(TAG, errorMsg);
            return eventsLiveData;
        }

        eventApiService.getAllEvents("Bearer " + token).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventsLiveData.setValue(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error occurred";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, "Error fetching events: " + errorMsg);
                    }
                } else {
                    String errorMsg = getErrorMessage(response.errorBody());
                    errorLiveData.setValue(errorMsg);
                    Log.e(TAG, "Error fetching events: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                errorLiveData.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
        return eventsLiveData;
    }

    public LiveData<Event> getEventById(String id) {
        String token = sessionManager.getToken();
        if (token == null) {
            String errorMsg = "Authentication required";
            errorLiveData.setValue(errorMsg);
            Log.e(TAG, errorMsg);
            return eventLiveData;
        }

        // FIXED: Correct parameter order - token first, then id
        eventApiService.getEventById("Bearer " + token, id).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventLiveData.setValue(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error occurred";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, "Error fetching event: " + errorMsg);
                    }
                } else {
                    String errorMsg = getErrorMessage(response.errorBody());
                    errorLiveData.setValue(errorMsg);
                    Log.e(TAG, "Error fetching event: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                errorLiveData.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
        return eventLiveData;
    }

    public LiveData<Event> createEvent(Event event) {
        String token = sessionManager.getToken();
        if (token == null) {
            String errorMsg = "Authentication required";
            errorLiveData.setValue(errorMsg);
            Log.e(TAG, errorMsg);
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
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error occurred";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, "Error creating event: " + errorMsg);
                    }
                } else {
                    String errorMsg = getErrorMessage(response.errorBody());
                    errorLiveData.setValue(errorMsg);
                    Log.e(TAG, "Error creating event: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                errorLiveData.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
        return eventLiveData;
    }

    public LiveData<Event> updateEvent(String id, Event event) {
        String token = sessionManager.getToken();
        if (token == null) {
            String errorMsg = "Authentication required";
            errorLiveData.setValue(errorMsg);
            Log.e(TAG, errorMsg);
            return eventLiveData;
        }

        // FIXED: Correct parameter order - token first, then id, then event
        eventApiService.updateEvent("Bearer " + token, id, event).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventLiveData.setValue(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error occurred";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, "Error updating event: " + errorMsg);
                    }
                } else {
                    String errorMsg = getErrorMessage(response.errorBody());
                    errorLiveData.setValue(errorMsg);
                    Log.e(TAG, "Error updating event: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                errorLiveData.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
        return eventLiveData;
    }

    public LiveData<Boolean> deleteEvent(String id) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String token = sessionManager.getToken();
        if (token == null) {
            String errorMsg = "Authentication required";
            errorLiveData.setValue(errorMsg);
            Log.e(TAG, errorMsg);
            result.setValue(false);
            return result;
        }

        // FIXED: Correct parameter order - token first, then id
        eventApiService.deleteEvent("Bearer " + token, id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    result.setValue(apiResponse.isSuccess());
                    if (!apiResponse.isSuccess()) {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error occurred";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, "Error deleting event: " + errorMsg);
                    }
                } else {
                    String errorMsg = getErrorMessage(response.errorBody());
                    errorLiveData.setValue(errorMsg);
                    result.setValue(false);
                    Log.e(TAG, "Error deleting event: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                errorLiveData.setValue(errorMsg);
                result.setValue(false);
                Log.e(TAG, errorMsg, t);
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Event>> joinEvent(String eventId) { // Changed return type
        final MutableLiveData<ApiResponse<Event>> joinEventResponseLiveData = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null) {
            String errorMsg = "Authentication required";
            Log.e(TAG, errorMsg);
            errorLiveData.postValue(errorMsg); // Notify via general error LiveData
            // Post null or a specific error ApiResponse if the fragment is set up to handle it
            // Based on instructions, posting null is acceptable as fragment checks for it.
            joinEventResponseLiveData.postValue(null);
            return joinEventResponseLiveData;
        }

        eventApiService.joinEvent("Bearer " + token, eventId).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    joinEventResponseLiveData.postValue(apiResponse); // Post the actual ApiResponse

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Successfully joined, update the specific event's details if needed elsewhere
                        eventLiveData.postValue(apiResponse.getData());
                    } else {
                        // API returned success=false, but valid response (e.g. "already joined")
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to join event (API logic error)";
                        // errorLiveData.postValue(errorMsg); // General error LiveData updated by Fragment if apiResponse.getMessage is used
                        Log.e(TAG, "API error joining event: " + errorMsg);
                    }
                } else {
                    // HTTP error (e.g., 404, 500)
                    String errorMsg = getErrorMessage(response.errorBody());
                    Log.e(TAG, "HTTP error joining event: " + errorMsg);
                    // Post null as ApiResponse to be handled by fragment's null check
                    joinEventResponseLiveData.postValue(null);
                    errorLiveData.postValue(errorMsg); // Update general error LiveData
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                // Network failure or other unexpected error
                String errorMsg = "Network error joining event: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                // Post null as ApiResponse
                joinEventResponseLiveData.postValue(null);
                errorLiveData.postValue(errorMsg); // Update general error LiveData
            }
        });
        return joinEventResponseLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    private String getErrorMessage(ResponseBody errorBody) {
        try {
            if (errorBody != null) {
                String errorString = errorBody.string();
                JSONObject errorJson = new JSONObject(errorString);
                return errorJson.optString("message", "Unknown error occurred");
            }
        } catch (IOException | org.json.JSONException e) {
            Log.e(TAG, "Error parsing error response", e);
        }
        return "Unknown error occurred";
    }

    public void getUserEvents() {
        String token = sessionManager.getToken();
        if (token == null) {
            String errorMsg = "Authentication required";
            errorLiveData.setValue(errorMsg);
            Log.e(TAG, errorMsg);
            return;
        }

        eventApiService.getUserEvents("Bearer " + token).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        eventsLiveData.setValue(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error occurred";
                        errorLiveData.setValue(errorMsg);
                        Log.e(TAG, "Error fetching user events: " + errorMsg);
                    }
                } else {
                    String errorMsg = getErrorMessage(response.errorBody());
                    errorLiveData.setValue(errorMsg);
                    Log.e(TAG, "Error fetching user events: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                errorLiveData.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
    }
}