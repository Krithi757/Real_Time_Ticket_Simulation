package com.example.ticketsimulation.model.entity;
import com.example.ticketsimulation.controller.CustomerRequestController;
import com.example.ticketsimulation.controller.TicketController;
import com.example.ticketsimulation.model.repository.TicketRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.*;
import java.util.logging.Formatter;
import java.util.stream.Collectors;


//Ticket pool to manage ticket availability, customer requests, and vendor actions
@Component
public class TicketPool {
    private Vendor vendor;
    private static final Logger LOGGER = Logger.getLogger("TicketSimulationLog");
    private volatile boolean stopProcessing = false; // Flag to stop processing ticket requests
    private final TicketRepository ticketRepository; // Repository for managing ticket persistence
    public static int ticketCount = 0; // Counter to keep track of total tickets in the pool
    private TicketController ticketController; // Controller to manage ticket-related frontend communication
    @Getter
    @Setter
    private int totalTickets; //Total tickets allowed in the pool
    static boolean isFull; //Flag capacity to indicate if the pool has reached its full capacity
    public final Lock lock = new ReentrantLock(); // Lock for synchronizing ticket pool actions
    public final Condition ticketsAvailable = lock.newCondition(); // Condition to signal availability of tickets
    private volatile boolean messageSent;
    public final Condition ticketsnotAvailable = lock.newCondition();

    // Comparator for prioritizing VIP customers in the queue
    private final Comparator<Customer> customerComparator = (c1, c2) -> {
        return c1.isVIP() && !c2.isVIP() ? -1 : (c2.isVIP() && !c1.isVIP() ? 1 : 0);
    };

    // Queue to manage customer requests based on priority (VIP status)
    private final PriorityBlockingQueue<Customer> customerQueue = new PriorityBlockingQueue<>(11, customerComparator);

    // Static block to set up logging to a file
    static {
        try {
            FileHandler fileHandler = new FileHandler("ticket_simulation_logs.txt", true);
            fileHandler.setFormatter(new Formatter(){
                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + System.lineSeparator(); // Only log the message
                }
            });
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to set up file handler for ticket simulation logs: " + e.getMessage());
        }
    }

    //Constructor to initialize Ticket pool with required repository for ticket data
    public TicketPool(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        this.vendor=vendor;
    }



    //Autowired method to inject TicketController dependency
    @Autowired
    public void setTicketcontroller(TicketController ticketController) {
        this.ticketController = ticketController;
    }


    //Logs and sends a ticket-related update to the CLI and frontend
    void sendTicketUpdate(String message) {
        LOGGER.info(message);
        ticketController.sendUpdate(message);  // Send the message to the frontend
    }

    //Adds a customer to the request queue and signals ticket availability
    public void requestTicket(Customer customer) {
        lock.lock();
        try {
            customerQueue.add(customer);
            ticketsAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    //Retrieves the number of available tickets in the pool
    public int getAvailableTickets() {
        lock.lock();
        try {
            return (int) ticketRepository.countByIsAvailableTrue();
        } finally {
            lock.unlock();
        }
    }

    //Adds new tickets to the pool based on the specified quantity and event category
    public void addTicket(int numberOfTickets, EventCategory eventCategory) {
        lock.lock(); // Lock to ensure thread safety while modifying the ticket pool
        try {
            // Loop to add the specified number of tickets, checking the pool's total capacity
            for (int i = 0; i < numberOfTickets; i++) {
                if (ticketCount >= totalTickets) {
                    break; // Stop if the pool reaches its total ticket count
                }
                Ticket ticket = new Ticket(); // Create a new ticket
                ticket.setAvailable(true); // Set the ticket as available
                ticket.setEventCategory(eventCategory); // Set the event category for the ticket
                ticketRepository.save(ticket); // Save the ticket to the database
            }
            ticketsAvailable.signalAll(); // Notify all waiting threads that new tickets are available
        } finally {
            lock.unlock(); // Release the lock
        }
    }



    //Processes ticket requests in the queue, giving priority to VIP customers and allows customers to buy tickets in the pool
    public void removeTicket() {
        lock.lock(); // Locks ticket pool for thread safety
        try {
            while (!customerQueue.isEmpty() && !stopProcessing) {
                Customer customer = customerQueue.poll(); // Get the next customer from the queue
                if (customer != null) {
                    customer.createAccount(); // Create an account if it doesn't exist

                    int ticketsToBuy = customer.getRequestedTickets(); // Get the number of tickets the customer wants to buy

                    if ((ticketCount == getTotalTickets()) && (getAvailableTickets() == 0)) {
                        sendTicketUpdate("All tickets finished and reaching end");
                        isFull = true;
                        stopProcessing = true;
                        break; // Stop processing if no tickets are available and total ticket count is reached
                    }

                    while (getAvailableTickets() == 0 && !stopProcessing) {
                        if (!messageSent) {
                            sendTicketUpdate("No tickets available, waiting for new tickets...");
                            messageSent = true; // Set the flag to prevent repeated messages
                        }
                        ticketsAvailable.await(); // Wait until tickets are added
                    }
                    ticketsnotAvailable.signalAll();

                    // Reset the flag once tickets become available
                    messageSent = false;

                    if (stopProcessing) {
                        break;
                    }

                    // Find available tickets from the repository
                    List<Ticket> availableTickets = ticketRepository.findByIsAvailableTrue();

                    if (availableTickets.size() >= ticketsToBuy) {
                        Map<EventCategory, Long> ticketsBoughtMap = new HashMap<>();

                        // Process customer purchase and group purchased tickets by event category
                        for (int i = 0; i < ticketsToBuy; i++) {
                            Ticket ticket = availableTickets.get(i);
                            ticket.setAvailable(false); // Mark the ticket as sold
                            ticketRepository.save(ticket); // Save the ticket

                            // Increment ticket count for the event category
                            EventCategory eventCategory = ticket.getEventCategory();
                            ticketsBoughtMap.put(eventCategory, ticketsBoughtMap.getOrDefault(eventCategory, 0L) + 1);
                        }
                        customer.incrementLoyaltyPoints();
                        ticketController.updateCustomerPurchaseData(customer.getName(), ticketsToBuy); // Update frontend with purchase data

                        StringBuilder purchaseSummary = new StringBuilder(customer.getName() + " bought " + ticketsToBuy + " tickets for: {");

// Append event details horizontally
                        ticketsBoughtMap.forEach((eventCategory, count) -> {
                            purchaseSummary.append(" [Event: ").append(eventCategory.getEventName())
                                    .append(", Date: ").append(eventCategory.getDate())
                                    .append(", Tickets Bought: ").append(count).append("],");
                        });

// Remove the trailing comma and close the brace
                        if (purchaseSummary.charAt(purchaseSummary.length() - 1) == ',') {
                            purchaseSummary.deleteCharAt(purchaseSummary.length() - 1);
                        }

                        purchaseSummary.append("}. Total tickets left for all events: ")
                                .append(getAvailableTickets())
                                .append(". Loyalty Points: ").append(customer.getLoyaltyPoints());

                        String message = purchaseSummary.toString();
                        sendTicketUpdate(message);
                        ticketController.sendAvailableTicketsUpdate(getAvailableTickets()); // Update available tickets on frontend

                        if (customer.isRealtime()) {
                            String realtimeMessage = "Yay! you just bought " + ticketsToBuy + " tickets for the following events: " + ticketsBoughtMap.keySet().stream()
                                    .map(eventCategory -> eventCategory.getEventName() + " on " + eventCategory.getDate())
                                    .collect(Collectors.joining(", "));
                            CustomerRequestController.sendMessage(realtimeMessage);
                            CustomerRequestController.isSuccess(true);
                        }
                    } else {
                        if (customer.isRealtime()) {
                            String mess = "Could not buy the requested tickets";
                            CustomerRequestController.sendMessage(mess);
                            CustomerRequestController.isSuccess(true);
                        }
                        ticketsnotAvailable.signalAll();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle thread interruption
            sendTicketUpdate("Thread interrupted while waiting for tickets.");
        } finally {
            lock.unlock();
        }
    }



}


