package com.example.eventapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventapp.R;
import com.example.eventapp.adapters.EventAdapter;
import com.example.eventapp.api.EventApiService;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.ApiResponse;
import com.example.eventapp.utils.SessionManager;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MyEventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private View emptyStateLayout;
    private View loadingView;

    @Inject
    EventApiService eventApiService;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        // Initialize views
        recyclerView = findViewById(R.id.eventsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingView = findViewById(R.id.loadingView);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Events");
        }

        // Setup RecyclerView
        adapter = new EventAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load events
        loadMyEvents();
    }

    private void loadMyEvents() {
        showLoading();

        String token = sessionManager.getToken();
        if (token == null) {
            showError("Please log in to view your events");
            return;
        }

        eventApiService.getUserEvents("Bearer " + token).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Event> events = apiResponse.getData();
                        if (events.isEmpty()) {
                            showEmptyState();
                        } else {
                            showEvents(events);
                        }
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Failed to load events");
                    }
                } else {
                    showError("Failed to load events");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                hideLoading();
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showEvents(List<Event> events) {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.updateEvents(events);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        hideLoading();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventClick(Event event) {
        // TODO: Handle event click - Navigate to event details
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 