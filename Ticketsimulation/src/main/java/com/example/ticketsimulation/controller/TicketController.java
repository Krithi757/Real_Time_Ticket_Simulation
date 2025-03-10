package com.example.ticketsimulation.controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
// Marks this class as a Spring controller that handles WebSocket messaging
@Controller
public class TicketController {

    private final SimpMessagingTemplate messagingTemplate; // Used to send messages to WebSocket subscribers
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private String lastMessage; // To store the last message sent
    private Map<String, Integer> vendorSalesData = new HashMap<>();
    private Map<String, Integer> customerPurchaseData = new HashMap<>();


    public TicketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    // Adds a message to the queue to be processed and sent to clients
    public void addToQueue(String message) {
        messageQueue.add(message); // Add the message to the queue
    }
    // Adds a message to the queue and immediately processes it
    public void sendUpdate(String message) {
        addToQueue(message); // Add message to queue once
        processQueue(); // Immediately process the queue

    }
    // Processes messages in the queue, ensuring that messages are sent in the order they are received
    private void processQueue() {
        // Process messages until the queue is empty
        while (!messageQueue.isEmpty()) {
            String message = messageQueue.poll(); // Retrieve and remove the head of the queue
            if (message != null && !message.equals(lastMessage)) {
                messagingTemplate.convertAndSend("/topic/ticket-updates", createResponse(message));
                lastMessage = message; // Store the last message sent
            }
        }
    }
    // Updates the vendor sales data and sends the updated data to the WebSocket subscribers
    public void sendVendorTicketData(String vendorName, int ticketsAdded) {
        // Update the vendor sales data
        vendorSalesData.put(vendorName, vendorSalesData.getOrDefault(vendorName, 0) + ticketsAdded);
        messagingTemplate.convertAndSend("/topic/vendor-sales", vendorSalesData);
    }
    // Updates the customer purchase data and sends the updated data to the WebSocket subscribers
    public void updateCustomerPurchaseData(String customerName, int ticketsBought) {
        customerPurchaseData.put(customerName, customerPurchaseData.getOrDefault(customerName, 0)+ticketsBought); // Update purchase data
        messagingTemplate.convertAndSend("/topic/customer-purchases", customerPurchaseData);
    }
    // Sends the available tickets count to WebSocket subscribers
    public void sendAvailableTicketsUpdate(int availableTickets) {
        Map<String, Integer> response = new HashMap<>();
        response.put("availableTickets", availableTickets);
        messagingTemplate.convertAndSend("/topic/available-tickets", response);
    }
    // Creates a map with the message, which will be sent to WebSocket clients
    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    // Scheduled method to send updates every 5 seconds (adjust timing as needed)
    @Scheduled(fixedRate = 5000)
    public void sendPeriodicUpdates() {
        if (lastMessage != null && messageQueue.isEmpty()) { // Add to queue only if there’s no pending message
            addToQueue(lastMessage);
            processQueue(); // Process the queue to send the message
        }
    }
}
