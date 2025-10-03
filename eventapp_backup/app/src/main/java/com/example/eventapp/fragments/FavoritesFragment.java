package com.example.eventapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.example.eventapp.R;
import com.example.eventapp.adapters.EventAdapter;
import com.example.eventapp.models.Event;
import com.example.eventapp.data.FavoritesRepository;
import com.example.eventapp.utils.SessionManager;
import com.example.eventapp.activities.LoginActivity;
import com.example.eventapp.activities.CreateEventActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment {
    @Inject
    SessionManager sessionManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private ImageButton menuButton;
    private TextView toolbarTitle;
    private RecyclerView favoritesRecyclerView;
    private EventAdapter eventAdapter;
    private FavoritesRepository favoritesRepository;
    private View emptyStateContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Initialize repositories and managers
        favoritesRepository = new FavoritesRepository(requireContext());

        // Initialize views
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);
        bottomNavigation = requireActivity().findViewById(R.id.bottomNavigationView);
        menuButton = view.findViewById(R.id.menuButton);
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        // Set toolbar title
        toolbarTitle.setText(R.string.nav_favorites);

        // Set up navigation and recycler view
        setupNavigationDrawer();
        setupBottomNavigation();
        setupRecyclerView();
        loadFavorites();

        return view;
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
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_favoritesFragment_to_categoryFragment);
            } else if (id == R.id.nav_my_events) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_favoritesFragment_to_myEventsFragment);
            } else if (id == R.id.nav_profile) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_favoritesFragment_to_profileFragment);
            } else if (id == R.id.nav_settings) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_favoritesFragment_to_settingsFragment);
            }
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_favorites);
        
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
                navController.navigate(R.id.action_favoritesFragment_to_categoryFragment);
                return true;
            } else if (id == R.id.navigation_create_event) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                return true; // Already on favorites
            } else if (id == R.id.navigation_profile) {
                navController.navigate(R.id.action_favoritesFragment_to_profileFragment);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(event -> {
            Bundle args = new Bundle();
            args.putString("eventId", event.getId());
            Navigation.findNavController(requireView())
                .navigate(R.id.action_favoritesFragment_to_eventDetailsFragment, args);
        });
        favoritesRecyclerView.setAdapter(eventAdapter);
    }

    private void loadFavorites() {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null) {
            showEmptyState();
            return;
        }

        // Use a background thread for database operations
        new Thread(() -> {
            final List<Event> favorites = favoritesRepository.getFavorites(userEmail);
            
            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                if (favorites.isEmpty()) {
                    showEmptyState();
                } else {
                    eventAdapter.updateEvents(favorites);
                    showContent();
                }
            });
        }).start();
    }

    private void showEmptyState() {
        emptyStateContainer.setVisibility(View.VISIBLE);
        favoritesRecyclerView.setVisibility(View.GONE);
    }

    private void showContent() {
        emptyStateContainer.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void handleLogout() {
        sessionManager.logout();
        
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
} 