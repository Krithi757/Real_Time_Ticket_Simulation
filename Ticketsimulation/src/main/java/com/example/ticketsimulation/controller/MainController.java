package com.example.ticketsimulation.controller;
import com.example.ticketsimulation.model.entity.TicketRequest;
import com.example.ticketsimulation.model.entity.TicketPool;
import com.example.ticketsimulation.model.service.CustomerService;
import com.example.ticketsimulation.model.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController // Marks this class as a REST controller to handle HTTP requests
@RequestMapping("/ticket") // Base URL for all methods in this controller
public class MainController {

    private final VendorService vendorService;
    private final CustomerService customerService;
    private final TicketPool ticketPool;

    @Autowired
    public MainController(VendorService vendorService, CustomerService customerService, TicketPool ticketPool) {
        this.vendorService = vendorService;
        this.customerService = customerService;
        this.ticketPool = ticketPool;
    }

    // HTTP POST endpoint to start ticket processing
    @PostMapping(value = "/start")
    public ResponseEntity<Map<String, String>> startTicketProcess(@RequestBody TicketRequest ticketRequest) throws InterruptedException { // Debugging log
        ticketPool.setTotalTickets(ticketRequest.getTotalTickets());  // Set total tickets in the pool

        // Start vendor process in a separate thread
        Thread vendorThread = new Thread(() -> {
            try {
                vendorService.startVendors(ticketRequest.getTicketRelease(), ticketRequest.getMaxCapacity());
            } catch (Exception e) {
                System.err.println("Error in vendor thread: " + e.getMessage());
            }
        });
        vendorThread.start();

        // Start customer process in a separate thread
        Thread customerThread = new Thread(() -> {
            try {
                customerService.startCustomers(ticketRequest.getCustomerRetrieval());
            } catch (Exception e) {
                System.err.println("Error in customer thread: " + e.getMessage());
            }
        });
        customerThread.start();
        // Return a success response
        return ResponseEntity.ok(createResponse("Ticket sales process initialized!"));
    }

    // WebSocket method to handle ticket updates
    @MessageMapping("/start-ticket")
    @SendTo("/topic/ticket-updates")
    public Map<String, String> processTicket(TicketRequest ticketRequest) throws InterruptedException {
        ticketPool.setTotalTickets(ticketRequest.getTotalTickets());

        // Start vendor process in a separate thread
        Thread vendorThread = new Thread(() -> {
            try {
                vendorService.startVendors(ticketRequest.getTicketRelease(), ticketRequest.getMaxCapacity());
            } catch (Exception e) {
                System.err.println("Error in vendor thread: " + e.getMessage());
            }
        });
        vendorThread.start();

        // Start customer process in a separate thread
        Thread customerThread = new Thread(() -> {
            try {
                customerService.startCustomers(ticketRequest.getCustomerRetrieval());
            } catch (Exception e) {
                System.err.println("Error in customer thread: " + e.getMessage());
            }
        });
        customerThread.start();
        return createResponse("Ticket sales process started!");
    }

    // Helper method to create a response map with a message
    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
