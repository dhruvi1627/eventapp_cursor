package com.example.eventapp.models;

import java.util.Date;

public class Ticket {
    private String id;
    private String eventId;
    private String userId;
    private String ticketNumber;
    private Date purchaseDate;
    private double price;
    private String pdfUrl;

    public Ticket() {
        // Required empty constructor for Firestore
    }

    public Ticket(String id, String eventId, String userId, String ticketNumber,
                 Date purchaseDate, double price, String pdfUrl) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.ticketNumber = ticketNumber;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.pdfUrl = pdfUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
} 