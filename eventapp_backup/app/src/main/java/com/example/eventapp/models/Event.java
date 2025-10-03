package com.example.eventapp.models;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Event {
    @SerializedName("_id")
    private String id;
    
    private String title;
    private String description;
    @SerializedName("date")
    private String dateStr;
    private transient Date dateObj;
    private String location;
    private User organizer;
    private List<User> participants;
    private int capacity;
    private double price;
    private int availableTickets;
    private String category;
    private String status;
    
    @SerializedName("imageBase64")
    private String imageBase64;
    
    @SerializedName("createdAt")
    private Date createdAt;
    
    @SerializedName("updatedAt")
    private Date updatedAt;

    @SerializedName("imageUrl")
    private String imageUrl;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    // Constructor for creating new events
    public Event(String title, String description, Date date, String location, 
                int capacity, double price, String category) {
        this.title = title;
        this.description = description;
        this.dateObj = date;
        this.dateStr = DATE_FORMAT.format(date);
        this.location = location;
        this.capacity = capacity;
        this.price = price;
        this.category = category;
        this.status = "upcoming";
        this.availableTickets = capacity;
    }

    // Constructor for existing events
    public Event(String id, String title, String description, String date, String location, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateStr = date;
        try {
            this.dateObj = DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            this.dateObj = new Date(); // Fallback to current date if parsing fails
        }
        this.location = location;
        this.imageUrl = imageUrl;
        this.status = "upcoming";
        this.availableTickets = capacity;
    }

    // Default constructor for Gson
    public Event() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() {
        if (dateObj == null && dateStr != null) {
            try {
                dateObj = DATE_FORMAT.parse(dateStr);
            } catch (ParseException e) {
                dateObj = new Date(); // Fallback to current date if parsing fails
            }
        }
        return dateObj;
    }

    public void setDate(Date date) {
        this.dateObj = date;
        this.dateStr = DATE_FORMAT.format(date);
    }

    public void setDate(String dateStr) {
        this.dateStr = dateStr;
        try {
            this.dateObj = DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            this.dateObj = new Date(); // Fallback to current date if parsing fails
        }
    }

    public String getDateString() {
        return dateStr;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }

    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getAvailableTickets() { return availableTickets; }
    public void setAvailableTickets(int availableTickets) { this.availableTickets = availableTickets; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
} 