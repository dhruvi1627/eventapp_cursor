package com.example.eventapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.eventapp.R;
import com.example.eventapp.activities.CreateEventActivity;
import com.example.eventapp.activities.LoginActivity;
import com.example.eventapp.adapters.EventAdapter;
import com.example.eventapp.api.EventApiService;
import com.example.eventapp.models.Event;
import com.example.eventapp.models.ApiResponse;
import com.example.eventapp.utils.SessionManager;

import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class CategoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private View emptyStateLayout;
    private View loadingView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private ImageButton menuButton;
    private TextView toolbarTitle;
    private Button logoutButton;
    private Toolbar toolbar;

    @Inject
    EventApiService eventApiService;

    @Inject
    SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        initializeViews(view);
        setupToolbarAndDrawer();
        setupBottomNavigation();
        setupSwipeRefresh();
        setupRecyclerView();
        loadEvents();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        loadingView = view.findViewById(R.id.loadingView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);
        bottomNavigation = requireActivity().findViewById(R.id.bottomNavigationView);
        menuButton = view.findViewById(R.id.menuButton);
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        logoutButton = view.findViewById(R.id.logoutButton);
        toolbar = view.findViewById(R.id.toolbar);

        // Set toolbar title
        toolbarTitle.setText(R.string.categories);
    }

    private void setupToolbarAndDrawer() {
        if (getActivity() != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_categoryFragment_to_homeFragment);
                } else if (id == R.id.nav_profile) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_categoryFragment_to_profileFragment);
                } else if (id == R.id.nav_my_events) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_categoryFragment_to_myEventsFragment);
                } else if (id == R.id.nav_favorites) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_categoryFragment_to_favoritesFragment);
                } else if (id == R.id.nav_logout) {
                    sessionManager.logout();
                    Navigation.findNavController(requireView()).navigate(R.id.action_categoryFragment_to_loginFragment);
                }
                drawerLayout.closeDrawers();
                return true;
            });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_category);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (!isAdded() || getView() == null) {
                return false;
            }

            int id = item.getItemId();
            NavController navController = Navigation.findNavController(getView());
            
            if (id == R.id.navigation_home) {
                navController.navigateUp();
                return true;
            } else if (id == R.id.navigation_category) {
                return true; // Already on category
            } else if (id == R.id.navigation_create_event) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                navController.navigate(R.id.action_categoryFragment_to_favoritesFragment);
                return true;
            } else if (id == R.id.navigation_profile) {
                navController.navigate(R.id.action_categoryFragment_to_profileFragment);
                return true;
            }
            return false;
        });
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadEvents);
        }
    }

    private void setupRecyclerView() {
        if (recyclerView != null) {
            adapter = new EventAdapter(event -> {
                Bundle args = new Bundle();
                args.putString("eventId", event.getId());
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_categoryFragment_to_eventDetailsFragment, args);
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);
        }
    }

    private void loadEvents() {
        showLoading();
        
        String token = sessionManager.getToken();
        if (token == null) {
            showError("Please log in to view events");
            return;
        }

        // Get category ID from arguments
        String categoryId = getArguments() != null ? getArguments().getString("categoryId") : null;
        if (categoryId == null) {
            showError("Category not found");
            return;
        }

        eventApiService.getEventsByCategory("Bearer " + token, categoryId).enqueue(new Callback<ApiResponse<List<Event>>>() {
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
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                hideLoading();
                showError("Network error: " + t.getMessage());
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void showEvents(List<Event> events) {
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.updateEvents(events);
    }

    private void showEmptyState() {
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        if (loadingView != null) loadingView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        hideLoading();
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }
} 