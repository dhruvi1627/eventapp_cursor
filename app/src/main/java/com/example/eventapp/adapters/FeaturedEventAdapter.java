package com.example.eventapp.adapters;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.example.eventapp.models.Event;

import java.util.List;
import java.util.Date;

public class FeaturedEventAdapter extends RecyclerView.Adapter<FeaturedEventAdapter.FeaturedEventViewHolder> {
    private final List<Event> events;
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public FeaturedEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public FeaturedEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_event, parent, false);

        // CRITICAL FIX: Ensure the view has match_parent dimensions for ViewPager2
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return new FeaturedEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedEventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class FeaturedEventViewHolder extends RecyclerView.ViewHolder {
        private final ImageView eventImage;
        private final TextView eventTitle;
        private final TextView eventDate;
        private final TextView eventLocation;
        private final TextView eventPrice;

        FeaturedEventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.featured_event_image);
            eventTitle = itemView.findViewById(R.id.featured_event_title);
            eventDate = itemView.findViewById(R.id.featured_event_date);
            eventLocation = itemView.findViewById(R.id.featured_event_location);
            eventPrice = itemView.findViewById(R.id.featured_event_price);
        }

        void bind(Event event, OnEventClickListener listener, SimpleDateFormat dateFormat) {
            if (event == null) return;

            if (eventTitle != null) {
                eventTitle.setText(event.getTitle());
            }
            
            if (eventLocation != null) {
                eventLocation.setText(event.getLocation());
            }
            
            if (eventDate != null && event.getDate() != null) {
                eventDate.setText(dateFormat.format(event.getDate()));
            }
            
            if (eventPrice != null) {
                eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
            }

            // Load image if available
            if (eventImage != null) {
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
                } else {
                    eventImage.setImageResource(R.drawable.event_placeholder);
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        }
    }
}