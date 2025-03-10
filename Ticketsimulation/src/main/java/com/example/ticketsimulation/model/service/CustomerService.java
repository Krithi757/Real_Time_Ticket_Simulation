package com.example.ticketsimulation.model.service;
import com.example.ticketsimulation.controller.TicketController;
import com.example.ticketsimulation.model.entity.*;
import com.example.ticketsimulation.model.entity.RealtimeCustomerRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
// The service class to handle customer-related operations such as starting threads for customer requests
@Service
public class CustomerService {
    private final TicketPool ticketpool; // Instance of the Ticketpool class to manage ticket requests
    private UserService userservice; // Instance of the Userservice class for user-related operations
    private TicketController ticketcontroller; // Instance of the TicketController to make connections to the frontend
    private final List<Thread> customerThreads = new ArrayList<>(); // List to store threads of customers

    // Constructor injection to provide the Ticketpool dependency
    @Autowired
    public CustomerService(TicketPool ticketpool) {
        this.ticketpool = ticketpool;
    }
    // Setter injection for the Ticketcontroller dependency
    @Autowired
    public void setTicketcontroller(TicketController ticketcontroller) {
        this.ticketcontroller = ticketcontroller;
    }

    // Setter injection for the Userservice dependency
    @Autowired
    public void setUserservice(UserService userservice) {
        this.userservice = userservice;
    }

    // Starts multiple customer threads, allowing each customer to request tickets
    public void startCustomers(int customerRet) {
        // Loop to create 5 customer threads
        for (int i = 1; i <= 5; i++) { //Instansiating
            Customer customer = new Customer(ticketpool, userservice, "Customer " + i, customerRet, ticketcontroller, false); //Creating an instance of the customer thread
            Thread thread = new Thread(customer); // Create a new thread for each customer
            customerThreads.add(thread); // Keep track of threads for potential management
            thread.start(); // Start the thread
        }

    }

    // Method to handle real-time customer requests directly, such as when a customer requests tickets in real-time via frontend
    public void handleRealTimeCustomerRequest(RealtimeCustomerRequest realTimeCustomerRequest) {
        // Create a new Customer object using data from the request
        Customer customer = new Customer(ticketpool, userservice, realTimeCustomerRequest.getUsername(),
                realTimeCustomerRequest.getTicketsToBuy(), ticketcontroller, true);

        //Set additional properties like password and the number of tickets to buy
        customer.setpassword(realTimeCustomerRequest.getPassword());
        customer.setrealtimerequest(realTimeCustomerRequest.getTicketsToBuy());
        customer.setUsername(realTimeCustomerRequest.getUsername());

        // Add the customer to the ticket pool for processing
        ticketpool.requestTicket(customer);

        // Call removeTicket to process the customer's ticket request
        ticketpool.removeTicket();

    }

}
