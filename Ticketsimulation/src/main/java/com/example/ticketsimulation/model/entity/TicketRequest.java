package com.example.ticketsimulation.model.entity;


//Represents ticket request with parameters for ticket allocation,customer retrieval,
// and capacity management within the ticketing system.
public class TicketRequest {
    private int totalTickets;
    private int ticketRelease;
    private int customerRetrieval;
    private int maxCapacity;

    // Getters and Setters
    // Total number of tickets to be added or managed in the pool
    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    // Number of tickets released by vendors per release cycle
    public int getTicketRelease() {
        return ticketRelease;
    }

    public void setTicketRelease(int ticketRelease) {
        this.ticketRelease = ticketRelease;
    }
    // Number of tickets customers will attempt to retrieve
    public int getCustomerRetrieval() {
        return customerRetrieval;
    }

    public void setCustomerRetrieval(int customerRetrieval) {
        this.customerRetrieval = customerRetrieval;
    }
    // Maximum capacity of tickets allowed in the system at any time
    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}

