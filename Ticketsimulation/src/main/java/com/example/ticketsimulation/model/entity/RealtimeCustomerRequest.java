package com.example.ticketsimulation.model.entity;

public class RealtimeCustomerRequest {

    private String username;
    private String password;
    private int ticketsToBuy;
    private String message;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTicketsToBuy() {
        return ticketsToBuy;
    }

    public void setTicketsToBuy(int ticketsToBuy) {
        this.ticketsToBuy = ticketsToBuy;
    }

    public void setMessage(String message) {
        this.message=message;
    }

    public String getMessage() {
        return message;
    }
}
