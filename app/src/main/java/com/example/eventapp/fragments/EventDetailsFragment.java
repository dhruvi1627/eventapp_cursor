package com.example.eventapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eventapp.R;
import com.example.eventapp.models.Event;
import com.example.eventapp.repository.EventRepository;
import com.bumptech.glide.Glide;

import android.util.Log; // Added for logging
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
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView organizerTextView;
    private TextView categoryTextView;
    private TextView priceTextView;
    private TextView ticketsTextView;
    private TextView participantsTextView;
    private Button joinEventButton;
    private View eventDetailsCardView; // To control visibility of the card

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
        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorTextView);
        organizerTextView = view.findViewById(R.id.organizerTextView);
        categoryTextView = view.findViewById(R.id.categoryTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        ticketsTextView = view.findViewById(R.id.ticketsTextView);
        participantsTextView = view.findViewById(R.id.participantsTextView);
        joinEventButton = view.findViewById(R.id.joinEventButton);
        eventDetailsCardView = view.findViewById(R.id.eventDetailsCardView); // Initialize CardView

        // Load event details
        loadEventDetails();

        joinEventButton.setOnClickListener(v -> {
            if (eventId != null) {
                joinEventButton.setEnabled(false);
                joinEventButton.setText(getString(R.string.action_joining_event));

                eventRepository.joinEvent(eventId).observe(getViewLifecycleOwner(), response -> {
                    joinEventButton.setEnabled(true);
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(getContext(), getString(R.string.message_join_event_success), Toast.LENGTH_SHORT).show();
                        joinEventButton.setText(getString(R.string.action_joined_event));
                        if (response.getData() != null) {
                            updateUI(response.getData()); // Update UI with new participant count, tickets etc.
                        }
                    } else {
                        joinEventButton.setText(getString(R.string.action_join_event));
                        String message = getString(R.string.message_join_event_failed);
                        if (response != null && response.getMessage() != null) {
                            message = response.getMessage();
                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return view;
    }

    private void loadEventDetails() {
        if (eventId != null) {
            progressBar.setVisibility(View.VISIBLE);
            errorTextView.setVisibility(View.GONE);
            eventDetailsCardView.setVisibility(View.GONE); // Hide content initially

            eventRepository.getEventById(eventId).observe(getViewLifecycleOwner(), event -> {
                progressBar.setVisibility(View.GONE);
                if (event != null) {
                    eventDetailsCardView.setVisibility(View.VISIBLE);
                    updateUI(event);
                } else {
                    errorTextView.setText(getString(R.string.message_event_load_failed_not_found));
                    errorTextView.setVisibility(View.VISIBLE);
                    eventDetailsCardView.setVisibility(View.GONE);
                }
            });

            // Observe errors
            eventRepository.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
                if (error != null && !error.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setText(getString(R.string.message_event_load_error, error));
                    errorTextView.setVisibility(View.VISIBLE);
                    eventDetailsCardView.setVisibility(View.GONE);
                    // Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show(); // Removed as per instruction
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

        // Populate new TextViews
        if (event.getOrganizer() != null && event.getOrganizer().getName() != null) {
            organizerTextView.setText(getString(R.string.label_organizer, event.getOrganizer().getName()));
        } else {
            organizerTextView.setText(getString(R.string.label_organizer_not_available));
        }

        if (event.getCategory() != null && !event.getCategory().isEmpty()) {
            categoryTextView.setText(getString(R.string.label_category, event.getCategory()));
        } else {
            categoryTextView.setText(getString(R.string.label_category_not_specified));
        }

        priceTextView.setText(getString(R.string.label_price) + String.format(Locale.getDefault(), "$%.2f", event.getPrice()));

        ticketsTextView.setText(getString(R.string.label_tickets_available_capacity, event.getAvailableTickets(), event.getCapacity()));

        if (event.getParticipants() != null && !event.getParticipants().isEmpty()) {
            participantsTextView.setText(getString(R.string.label_participants_count, event.getParticipants().size()));
        } else {
            participantsTextView.setText(getString(R.string.label_participants_zero));
        }


        if (event.getImageBase64() != null && !event.getImageBase64().isEmpty() && getContext() != null) {
            String base64Image = event.getImageBase64();
            String pureBase64Image = base64Image;

            // Check if the string contains the Base64 prefix
            if (base64Image.contains(",")) {
                // Split the string at the comma and take the second part (the actual Base64)
                pureBase64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            try {
                byte[] decodedString = android.util.Base64.decode(pureBase64Image, android.util.Base64.DEFAULT);
                Glide.with(getContext())
                    .load(decodedString)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image) // This error drawable should show if decoding fails or bytes are not an image
                    .into(eventImageView);
            } catch (IllegalArgumentException e) {
                // Log error or handle case where pureBase64Image is not valid Base64
                Log.e("EventDetailsFragment", "Failed to decode Base64 string", e);
                Glide.with(getContext())
                    .load(R.drawable.error_image) // Show error drawable explicitly on decode failure
                    .into(eventImageView);
            }
        } else {
            // Load placeholder if no image
            Glide.with(getContext())
                .load(R.drawable.placeholder_image)
                .into(eventImageView);
        }
    }
}