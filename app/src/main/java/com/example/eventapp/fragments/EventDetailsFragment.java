package com.example.eventapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.repository.EventRepository;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EventDetailsFragment extends Fragment {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView dateTextView;
    private TextView locationTextView;
    private ImageView eventImageView;
    @Inject
    EventRepository eventRepository;
    private String eventId;
    private SimpleDateFormat dateFormat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = EventDetailsFragmentArgs.fromBundle(getArguments()).getEventId();
        }
        // Initialize date formatter
        dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        // Initialize views
        titleTextView = view.findViewById(R.id.titleTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        eventImageView = view.findViewById(R.id.eventImageView);

        // Load event details
        loadEventDetails();

        return view;
    }

    private void loadEventDetails() {
        if (eventId != null) {
            eventRepository.getEventById(eventId).observe(getViewLifecycleOwner(), event -> {
                if (event != null) {
                    updateUI(event);
                } else {
                    Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                }
            });

            // Observe errors
            eventRepository.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updateUI(Event event) {
        titleTextView.setText(event.getTitle());
        descriptionTextView.setText(event.getDescription());
        // Format the date
        Date eventDate = event.getDate();
        if (eventDate != null) {
            dateTextView.setText(dateFormat.format(eventDate));
        } else {
            dateTextView.setText("Date not available");
        }
        locationTextView.setText(event.getLocation());

        if (event.getImageBase64() != null && !event.getImageBase64().isEmpty() && getContext() != null) {
            // Convert base64 to bitmap
            byte[] decodedString = android.util.Base64.decode(event.getImageBase64(), android.util.Base64.DEFAULT);
            Glide.with(getContext())
                .load(decodedString)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(eventImageView);
        } else {
            // Load placeholder if no image
            Glide.with(getContext())
                .load(R.drawable.placeholder_image)
                .into(eventImageView);
        }
    }
}