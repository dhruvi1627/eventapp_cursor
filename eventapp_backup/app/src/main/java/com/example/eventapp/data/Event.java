package com.example.eventapp.data;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

public class Event {
    private static final String TAG = "Event";
    private String id;
    private String title;
    private String description;
    private String category;
    private String location;
    private Date date;
    private double price;
    private String imageUrl;
    private String organizer;
    private int capacity;
    private int availableTickets;
    private String status;

    public static final String[] VALID_CATEGORIES = {"social", "business", "sports", "education", "other"};
    public static final String[] VALID_STATUSES = {"upcoming", "ongoing", "completed", "cancelled"};

    public Event() {
        // Required empty constructor
        this.status = "upcoming"; // Default status
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public Date getDate() { return date; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getOrganizer() { return organizer; }
    public int getCapacity() { return capacity; }
    public int getAvailableTickets() { return availableTickets; }
    public String getStatus() { return status; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { 
        if (!isValidCategory(category)) {
            throw new IllegalArgumentException("Invalid category. Must be one of: social, business, sports, education, other");
        }
        this.category = category; 
    }
    public void setLocation(String location) { this.location = location; }
    public void setDate(Date date) { this.date = date; }
    public void setPrice(double price) { 
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price; 
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public void setCapacity(int capacity) { 
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        this.capacity = capacity; 
    }
    public void setAvailableTickets(int availableTickets) { this.availableTickets = availableTickets; }
    public void setStatus(String status) { 
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status. Must be one of: upcoming, ongoing, completed, cancelled");
        }
        this.status = status; 
    }

    private boolean isValidCategory(String category) {
        for (String validCategory : VALID_CATEGORIES) {
            if (validCategory.equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equals(status)) {
                return true;
            }
        }
        return false;
    }

    // JSON serialization
    public String toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("title", title);
            json.put("description", description);
            json.put("category", category);
            json.put("location", location);
            json.put("date", date != null ? date.getTime() : null);
            json.put("price", price);
            json.put("imageUrl", imageUrl);
            json.put("organizer", organizer);
            json.put("capacity", capacity);
            json.put("availableTickets", availableTickets);
            json.put("status", status);
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error converting event to JSON", e);
            return null;
        }
    }

    // JSON deserialization
    public static Event fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            Event event = new Event();
            event.setId(json.getString("id"));
            event.setTitle(json.getString("title"));
            event.setDescription(json.getString("description"));
            event.setCategory(json.getString("category"));
            event.setLocation(json.getString("location"));
            if (!json.isNull("date")) {
                event.setDate(new Date(json.getLong("date")));
            }
            event.setPrice(json.getDouble("price"));
            event.setImageUrl(json.optString("imageUrl"));
            event.setOrganizer(json.getString("organizer"));
            event.setCapacity(json.getInt("capacity"));
            event.setAvailableTickets(json.optInt("availableTickets", json.getInt("capacity")));
            event.setStatus(json.optString("status", "upcoming"));
            return event;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON to event", e);
            return null;
        }
    }
} 