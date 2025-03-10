package com.example.ticketsimulation.model.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

//Entity representing a ticket in the ticket simulation
//Each ticket has an ID, availability status, and an event category
@Entity
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Automatically generates unique identifier
    private Long id;
    private boolean isAvailable = true; //Availability status of the ticket

    private EventCategory eventCategory; // New field for Event Category

    public Ticket(EventCategory eventCategory) { // Constructor to set EventCategory
        this.eventCategory = eventCategory;
    }

    public Ticket() {} // Default constructor for JPA required to create ticket instances in the JPA


}

