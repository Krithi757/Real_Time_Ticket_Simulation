package com.example.ticketsimulation.controller;
import com.example.ticketsimulation.model.entity.RealtimeCustomerRequest;
import com.example.ticketsimulation.model.entity.TicketPool;
import com.example.ticketsimulation.model.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
// Marks this class as a REST controller, capable of handling HTTP requests and WebSocket messages
@RestController
public class CustomerRequestController {

    private final TicketPool ticketPool; // Represents the pool of tickets available for purchase
    private final CustomerService customerService; // Service layer to handle customer-related business logic
    private final TicketController ticketController; // Controller to manage WebSocket updates
    private static String mes=null; // Holds the latest message to be sent as feedback
    private static boolean value=false;
    private static String registerr="not registered"; // Default message for registration status
    // Constructor-based dependency injection for Ticketpool, Customerservice, and TicketController
    @Autowired
    public CustomerRequestController(TicketPool ticketPool, CustomerService customerService, TicketController ticketController) {
        this.ticketPool = ticketPool;
        this.customerService = customerService;
        this.ticketController=ticketController;
    }
    // Static method to update the message variable
    public static void sendMessage(String message){
        mes=message; // Update the success flag
    }
    // Static method to update the success flag
    public static void isSuccess(boolean val){
        value=val;
    }
    // Static method to update the registration status
    public static void register(String register){
        registerr=register; // Update the registration status message
    }

    // WebSocket method to handle real-time customer request
    //WebSocket endpoint to handle real-time customer purchase requests
    // The method listens for messages sent to `/realtime-customer-purchase and sends the response to `/topic/ticket-updates`.
    @MessageMapping("/realtime-customer-purchase")
    @SendTo("/topic/ticket-updates")
    public Map<String, String> processRealTimeCustomerRequest(RealtimeCustomerRequest customerRequest) {
        // Process the customer request in a similar manner as vendor/customer threads
        // Directly update the ticket pool with customer-related info

        // Assuming Customer service processes the ticket buying action
        customerService.handleRealTimeCustomerRequest(customerRequest);
        // Return a response containing feedback and status
        return Map.of(
                "type", "customer",
                "success","success",
                "message", mes,
                "register", registerr
        );

    }
}