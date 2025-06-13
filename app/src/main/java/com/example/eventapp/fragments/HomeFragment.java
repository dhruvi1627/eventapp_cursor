package com.example.eventapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.eventapp.R;
import com.example.eventapp.activities.LoginActivity;
import com.example.eventapp.activities.CreateEventActivity;
import com.example.eventapp.adapters.EventAdapter;
import com.example.eventapp.adapters.FeaturedEventAdapter;
import com.example.eventapp.models.Event;
import com.example.eventapp.repository.EventRepository; // Corrected import
import com.example.eventapp.utils.SessionManager;
import android.util.Log; // For logging errors

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private ViewPager2 featuredEventsViewPager;
    private TabLayout featuredEventsIndicator;
    private RecyclerView nearbyEventsRecyclerView;
    private EditText searchEditText;
    private FeaturedEventAdapter featuredEventAdapter;
    private EventAdapter nearbyEventAdapter;
    @Inject
    EventRepository eventRepository;
    private List<Event> featuredEventList;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private ImageButton menuButton;
    private TextView toolbarTitle;
    @Inject
    SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize lists
        featuredEventList = new ArrayList<>();

        // Initialize views
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);
        bottomNavigation = requireActivity().findViewById(R.id.bottomNavigationView);
        menuButton = view.findViewById(R.id.menuButton);
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        nearbyEventsRecyclerView = view.findViewById(R.id.nearbyEventsRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);

        // Set toolbar title
        toolbarTitle.setText(R.string.nav_home);

        // Setup navigation drawer
        setupNavigationDrawer();
        setupBottomNavigation();

        initializeViews(view);
        setupFeaturedEvents();
        setupNearbyEvents();
        setupSearch();
        loadFeaturedEvents();
        loadNearbyEvents();
        observeApiErrors(); // Call to observe errors

        return view;
    }

    private void initializeViews(View view) {
        featuredEventsViewPager = view.findViewById(R.id.featuredEventsViewPager);
        featuredEventsIndicator = view.findViewById(R.id.featuredEventsIndicator);
        nearbyEventsRecyclerView = view.findViewById(R.id.nearbyEventsRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
    }

    private void setupFeaturedEvents() {
        featuredEventAdapter = new FeaturedEventAdapter(featuredEventList, event -> {
            // Navigate to event details
            Bundle args = new Bundle();
            args.putString("eventId", event.getId());
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_eventDetailsFragment, args);
        });
        
        // Set up ViewPager2
        featuredEventsViewPager.setOffscreenPageLimit(1);
        featuredEventsViewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        
        // Set adapter
        featuredEventsViewPager.setAdapter(featuredEventAdapter);
        
        new TabLayoutMediator(featuredEventsIndicator, featuredEventsViewPager,
                (tab, position) -> {
                    // No title needed for dots indicator
                }).attach();
    }

    private void setupNearbyEvents() {
        nearbyEventAdapter = new EventAdapter(event -> {
            // Navigate to event details
            Bundle args = new Bundle();
            args.putString("eventId", event.getId());
            Navigation.findNavController(requireView())
                .navigate(R.id.action_homeFragment_to_eventDetailsFragment, args);
        });
        nearbyEventsRecyclerView.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        nearbyEventsRecyclerView.setAdapter(nearbyEventAdapter);
    }

    private void setupSearch() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
            return true;
        });
    }

    private void performSearch(String query) {
        // TODO: Implement search with new API
        Toast.makeText(requireContext(), "Search functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    private void loadFeaturedEvents() {
        // Fetch all events by calling getEvents(), which populates the LiveData observed below.
        eventRepository.getEvents();
        
        // Observe the events LiveData
        eventRepository.getEventsLiveData().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                featuredEventList.clear();
                // Filter featured events (you might want to add a 'featured' field to your Event class)
                List<Event> featured = new ArrayList<>();
                for (Event event : events) {
                    if (featured.size() < 5) { // Limit to 5 featured events
                        featured.add(event);
                    }
                }
                featuredEventList.addAll(featured);
                featuredEventAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadNearbyEvents() {
        // Observe the events LiveData, assuming getEvents() was called (e.g., by loadFeaturedEvents)
        eventRepository.getEventsLiveData().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                nearbyEventAdapter.updateEvents(events);
            } else {
                // Handle case where events are null or empty after fetch
                // nearbyEventAdapter.updateEvents(new ArrayList<>()); // Clear the list
            }
        });
    }

    private void observeApiErrors() {
        if (eventRepository != null && eventRepository.getErrorLiveData() != null) {
            eventRepository.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    Log.e("HomeFragment", "API Error: " + error);
                    // eventRepository.clearErrorLiveData(); // Consider adding if error is sticky
                }
            });
        }
    }

    private void setupNavigationDrawer() {
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(navigationView);
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already on home
                return true;
            } else if (id == R.id.nav_my_events) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_myEventsFragment);
            } else if (id == R.id.nav_create_event) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
            } else if (id == R.id.nav_favorites) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_favoritesFragment);
            } else if (id == R.id.nav_profile) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_profileFragment);
            } else if (id == R.id.nav_settings) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_homeFragment_to_settingsFragment);
            } else if (id == R.id.nav_logout) {
                handleLogout();
            }
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (!isAdded() || getView() == null) {
                return false;
            }

            int id = item.getItemId();
            NavController navController = Navigation.findNavController(getView());
            
            if (id == R.id.navigation_home) {
                return true; // Already on home
            } else if (id == R.id.navigation_category) {
                navController.navigate(R.id.action_homeFragment_to_categoryFragment);
                return true;
            } else if (id == R.id.navigation_create_event) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                navController.navigate(R.id.action_homeFragment_to_favoritesFragment);
                return true;
            } else if (id == R.id.navigation_profile) {
                navController.navigate(R.id.action_homeFragment_to_profileFragment);
                return true;
            }
            return false;
        });
    }

    private void handleLogout() {
        sessionManager.logout();
        
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
} 