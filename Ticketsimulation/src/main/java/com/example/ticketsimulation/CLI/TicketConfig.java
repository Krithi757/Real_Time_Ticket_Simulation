package com.example.ticketsimulation.CLI;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TicketConfig {

    private int totalTickets; // Total number of tickets available
    private int ticketRelease; // Number of tickets to be released at a time
    private int customerRetrieval; // Number of tickets a customer can retrieve
    private int maxCapacity; // Maximum capacity of the event

    // Constructor to initialize the ticket configuration
    public TicketConfig(int totalTickets, int ticketRelease, int customerRetrieval, int maxCapacity) {
        this.totalTickets = totalTickets;
        this.ticketRelease = ticketRelease;
        this.customerRetrieval = customerRetrieval;
        this.maxCapacity = maxCapacity;
    }
    // Getters and setters

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getTicketRelease() {
        return ticketRelease;
    }

    public void setTicketRelease(int ticketRelease) {
        this.ticketRelease = ticketRelease;
    }

    public int getCustomerRetrieval() {
        return customerRetrieval;
    }

    public void setCustomerRetrieval(int customerRetrieval) {
        this.customerRetrieval = customerRetrieval;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    // Method to save the configuration to a file (this will overwrite the file)
    public static void saveToFile(TicketConfig config, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new Gson();
            gson.toJson(config, writer);  // This will overwrite the file
        }
    }


    // Method to load the configuration from a file
    public static TicketConfig loadFromFile(String filename) throws IOException {
        try (FileReader reader = new FileReader(filename)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, TicketConfig.class);
        }
    }

    // Override the toString method to provide a custom string representation of the Ticketconfig object
    @Override
    public String toString() {
        return "TicketConfiguration{" +
                "totalTickets=" + totalTickets +
                ", ticketRelease=" + ticketRelease +
                ", customerRetrieval=" + customerRetrieval +
                ", maxCapacity=" + maxCapacity +
                '}';
    }
}
