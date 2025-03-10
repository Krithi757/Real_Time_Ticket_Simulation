package com.example.ticketsimulation.model.service;

import com.example.ticketsimulation.controller.TicketController;
import com.example.ticketsimulation.model.entity.EventCategory;
import com.example.ticketsimulation.model.entity.TicketPool;
import com.example.ticketsimulation.model.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Service layer for managing vendor threads that release tickets into the Ticketpool
@Service
public class VendorService {
    private final TicketPool ticketpool;
    private UserService userservice;  // Make User service a setter dependency for user-related services
    private TicketController ticketcontroller;  // Make TicketController a setter dependency for ticket updates
    private final List<Thread> vendorThreads = new ArrayList<>(); // List to keep track of vendor threads

    @Autowired
    public VendorService(TicketPool ticketpool) {
        this.ticketpool = ticketpool;
    }

    // Setter injection for Userservice
    @Autowired
    public void setUserservice(UserService userservice) {
        this.userservice = userservice;
    }

    // Setter injection for TicketController
    @Autowired
    public void setTicketcontroller(TicketController ticketcontroller) {
        this.ticketcontroller = ticketcontroller;
    }

    // Starts vendor threads to release tickets
    public void startVendors(int ticketRelease, int maxCap) {
        EventCategory[] eventCategories = EventCategory.values(); // Available event categories
        Random random = new Random();

        for (int i = 1; i <= 5; i++) {
            int randomIndex = random.nextInt(eventCategories.length); // Select a random event category
            Vendor vendor = new Vendor(ticketpool, userservice, "Vendor " + i, ticketRelease, maxCap, eventCategories[randomIndex], ticketcontroller);
            Thread thread = new Thread(vendor); // Create a new thread for each vendor
            vendorThreads.add(thread); // Add the thread to the list for tracking
            thread.start(); // Start the thread

            try {
                Thread.sleep(100); // Add delay between starting each vendor thread
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
