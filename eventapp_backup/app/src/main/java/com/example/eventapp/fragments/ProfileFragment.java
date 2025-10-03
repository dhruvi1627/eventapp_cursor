package com.example.eventapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.eventapp.R;
import com.example.eventapp.activities.LoginActivity;
import com.example.eventapp.activities.CreateEventActivity;
import com.example.eventapp.data.UserRepository;
import com.example.eventapp.utils.SessionManager;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONObject;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {
    @Inject
    SessionManager sessionManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigation;
    private ImageButton menuButton;
    private Button logoutButton;
    private TextView toolbarTitle;
    private CircleImageView profileImage;
    private TextView nameText;
    private ImageButton editProfileButton;
    private View myEventsSection;
    private View bookedEventsSection;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize repositories
        userRepository = new UserRepository(requireContext());

        // Initialize views
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);
        bottomNavigation = requireActivity().findViewById(R.id.bottomNavigationView);
        menuButton = view.findViewById(R.id.menuButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        profileImage = view.findViewById(R.id.profileImage);
        nameText = view.findViewById(R.id.nameText);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        myEventsSection = view.findViewById(R.id.myEventsSection);
        bookedEventsSection = view.findViewById(R.id.bookedEventsSection);

        // Set toolbar title
        toolbarTitle.setText(R.string.nav_profile);

        // Set up navigation and click listeners
        setupNavigationDrawer();
        setupBottomNavigation();
        setupClickListeners();
        loadUserData();

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
                    .navigate(R.id.action_profileFragment_to_homeFragment);
            } else if (id == R.id.nav_my_events) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileFragment_to_myEventsFragment);
            } else if (id == R.id.nav_create_event) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
            } else if (id == R.id.nav_favorites) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileFragment_to_favoritesFragment);
            } else if (id == R.id.nav_settings) {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileFragment_to_settingsFragment);
            } else if (id == R.id.nav_logout) {
                handleLogout();
            }
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (!isAdded() || getView() == null) {
                return false;
            }

            int id = item.getItemId();
            NavController navController = Navigation.findNavController(getView());
            
            if (id == R.id.navigation_home) {
                navController.navigate(R.id.action_profileFragment_to_homeFragment);
                return true;
            } else if (id == R.id.navigation_category) {
                navController.navigateUp();
                navController.navigate(R.id.action_homeFragment_to_categoryFragment);
                return true;
            } else if (id == R.id.navigation_create_event) {
                startActivity(new Intent(requireActivity(), CreateEventActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                navController.navigate(R.id.action_profileFragment_to_favoritesFragment);
                return true;
            } else if (id == R.id.navigation_profile) {
                return true; // Already on profile
            }
            return false;
        });
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                .navigate(R.id.action_profileFragment_to_editProfileFragment);
        });

        myEventsSection.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                .navigate(R.id.action_profileFragment_to_myEventsFragment);
        });

        bookedEventsSection.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                .navigate(R.id.action_profileFragment_to_myEventsFragment);
        });
    }

    private void loadUserData() {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null) {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        // Use a background thread for database operations
        new Thread(() -> {
            final JSONObject profile = userRepository.getUserProfile(userEmail);
            
            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                try {
                    if (profile.has("name")) {
                        nameText.setText(profile.getString("name"));
                    }
                    
                    if (profile.has("profileImage")) {
                        String profileImagePath = profile.getString("profileImage");
                        // TODO: Load profile image using Glide or Picasso
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void handleLogout() {
        sessionManager.logout();
        
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
} 