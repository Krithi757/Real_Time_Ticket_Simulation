package com.example.ticketsimulation.model.entity;
import lombok.Setter;

//Enum representing different event categories in the ticket simulation
//Each category has a name, date, and price, and allows modification of the price
public enum EventCategory {
    MOVIE_THEATER("Movie Theater", "2024-11-01", "3500"),
    HOTEL_BOOKING("Hotel Booking", "2024-11-02", "5000"),
    GAME_SHOW("Game Show Booking", "2024-11-03", "4500"),
    COOKING_EVENT("Cooking Event", "2024-11-04", "3000"),
    FASHION_EVENT("Fashion Event", "2024-11-05", "4000");

    private final String eventName; //Name of the event
    private final String date; //Date of the event
    @Setter
    private String price; //Price of event

    //Constructor to initialize each event category with specific details
    EventCategory(String eventName, String date, String price) {
        this.eventName = eventName;
        this.date = date;
        this.price = price;
    }

    //Getter for event name
    public String getEventName() {
        return eventName;
    }

    //Getter for event date
    public String getDate() {
        return date;
    }

    //Getter for event price
    public String getPrice() {
        return price;
    }
}

