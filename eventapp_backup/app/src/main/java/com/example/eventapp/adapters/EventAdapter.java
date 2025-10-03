package com.example.eventapp.adapters;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.events = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void updateEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ImageView eventImage;
        private final TextView eventTitle;
        private final TextView eventDescription;
        private final TextView eventLocation;
        private final TextView eventDate;
        private final TextView eventPrice;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventPrice = itemView.findViewById(R.id.eventPrice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });
        }

        void bind(Event event) {
            eventTitle.setText(event.getTitle());
            eventDescription.setText(event.getDescription());
            eventLocation.setText(event.getLocation());
            
            // Format and set date
            if (event.getDate() != null) {
                eventDate.setText(dateFormat.format(event.getDate()));
            } else {
                eventDate.setText(event.getDateString()); // Fallback to string date
            }
            
            // Set price if view exists
            if (eventPrice != null) {
                if (event.getPrice() > 0) {
                    eventPrice.setVisibility(View.VISIBLE);
                    eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
                } else {
                    eventPrice.setVisibility(View.GONE);
                }
            }

            // Handle image loading
            if (event.getImageBase64() != null && !event.getImageBase64().isEmpty()) {
                // Handle base64 image
                String base64Image = event.getImageBase64();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }
                
                try {
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Glide.with(itemView.getContext())
                            .load(decodedString)
                            .placeholder(R.drawable.event_placeholder)
                            .error(R.drawable.event_placeholder)
                            .centerCrop()
                            .into(eventImage);
                } catch (IllegalArgumentException e) {
                    eventImage.setImageResource(R.drawable.event_placeholder);
                }
            } else if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                // Handle URL image
                Glide.with(itemView.getContext())
                        .load(event.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.event_placeholder)
                        .error(R.drawable.event_placeholder)
                        .into(eventImage);
            } else {
                eventImage.setImageResource(R.drawable.event_placeholder);
            }
        }
    }
} 