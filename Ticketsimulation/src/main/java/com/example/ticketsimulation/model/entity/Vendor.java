package com.example.ticketsimulation.model.entity;
import com.example.ticketsimulation.controller.TicketController;
import com.example.ticketsimulation.model.service.UserService;
import lombok.Getter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.*;

//Represents a vendor in the ticket simulation who can add tickets, log in, and perform other actions
public class Vendor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("TicketSimulationLog"); //Logger to write logs to a central file to print in CLI
    private static volatile boolean stopAllVendors = false; //Flag to stop all vendors if ticket limit reached
    private final TicketPool ticketpool; // Instance of the Ticketpool class to add tickets
    @Getter
    private final String name; //Vendor name
    private String username; //Vendor username
    private final String password; //Vendor password
    private final int ticketRelease; //Specifies the number o tickets that can be released bye each vendor
    private final int maxCap; // Maximum capacity for ticket pool
    private final EventCategory eventCategory; // The event category associated with the tickets
    private final UserService userService; // Service for user operations such as login, registration
    private final Random random = new Random(); // Random object for generating random values
    private boolean isLoggedIn = false; //Tracks vendor login status
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; //Characters for random password
    private static final int PASSWORD_LENGTH = 8; //Declares password length as 8
    private final TicketController ticketController;
    private boolean salesStarted = false; // Indicates whether ticket sales have started
    private volatile boolean maxCapLogged; // Tracks whether max capacity has been logged

    // Static block to configure the Logger for vendor logs


    static {
        try {
            FileHandler fileHandler = new FileHandler("ticket_simulation_logs.txt", true);
            //fileHandler.setFormatter(new SimpleFormatter());
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

    //Vendor constructor with details and dependencies
    public Vendor(TicketPool ticketpool, UserService userService,
                  String name, int ticketRelease, int maxCap, EventCategory eventCategory, TicketController ticketController) {
        this.ticketpool = ticketpool;
        this.userService = userService;
        this.name = name;
        this.ticketRelease = ticketRelease;
        this.maxCap = maxCap;
        this.eventCategory = eventCategory;
        this.ticketController = ticketController;
        this.password = generatePassword();// Generate random password for the vendor
    }

    //sendTicketUpdate updates the print messages to the logger and the websocket
    private void sendTicketUpdate(String message) {
        LOGGER.info(message); // Log the message to the file
        ticketController.addToQueue(message); // Add the message to the ticket controller queue
        ticketController.sendUpdate(message); // Send the update to WebSocket for real-time updates
    }

    //Creates a random unique password
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH); // Select a random character for the password
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString(); // Return the generated password
    }


    //Run method to continuously add tickets to the ticket pool till total ticket count reached
    @Override
    public void run() {
        int releaseDelay=1000/ticketRelease;
        while (!stopAllVendors) {
            for (int i = 0; i < ticketRelease; i++) {
                int ticketToBeAdded = random.nextInt(1, ticketRelease + 1); //Generate a random number of tickets to be added between 1 and ticket release
                ticketpool.lock.lock(); //Ticket pool from vendor class is also locked as there are multiple concurrent actions that happen before addTicket that needs to be synchronized
                try {
                    if (TicketPool.ticketCount >= ticketpool.getTotalTickets()) {
                            if (!stopAllVendors) {
                                stopAllVendors = true; //flag becomes true if total ticket count is reached
                                sendTicketUpdate("Vendors stopping. Total ticket count reached.");
                            }
                        break;
                    }

                    int availSpace = maxCap - ticketpool.getAvailableTickets(); //Calculate available space for tickets
                    int ticketToAdd = Math.min(ticketToBeAdded, availSpace);
                    while ((availSpace == 0) && (salesStarted) && (!stopAllVendors)) {
                        if(!maxCapLogged){
                            maxCapLogged = true;
                            sendTicketUpdate("Max capacity reached. Waiting for customers to start purchasing....");
                        }
                        ticketpool.ticketsnotAvailable.await();
                    }
                    ticketpool.ticketsAvailable.signalAll();
                    maxCapLogged = false;
                    if (ticketToAdd > 0) { // Add tickets if only available space is greater than zero
                        createAccount(); // Ensure vendor is logged in
                        int remainingTickets = ticketpool.getTotalTickets() - TicketPool.ticketCount;

                        if (ticketToAdd > remainingTickets) {
                            ticketToAdd = remainingTickets; // Adjust ticketToAdd to the remaining capacity
                        }

                        if (ticketToAdd > 0) {
                            ticketpool.addTicket(ticketToAdd, eventCategory); // Add tickets
                            ticketController.sendVendorTicketData(name, ticketToAdd);
                            salesStarted = true;
                            TicketPool.ticketCount += ticketToAdd;
                            sendTicketUpdate(name + " added " + ticketToAdd + " tickets: {Event: "
                                    + eventCategory.getEventName() + ", Date: "
                                    + eventCategory.getDate() + ", Price: "
                                    + eventCategory.getPrice() + ". Total tickets available: " + ticketpool.getAvailableTickets()
                                    + " Total ticket count: " + TicketPool.ticketCount + "}. ");
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    ticketpool.lock.unlock(); //Unlock ticket pool
                }

                //Occasionally change ticket price to make it a real time application
                if (random.nextInt(ticketpool.getTotalTickets()*80) < 13) {
                    createAccount(); // Ensure vendor is logged in before changing price
                    randomTicketPriceChange(); // Change ticket price randomly
                }

                try {
                    Thread.sleep(releaseDelay); //Pause before next action
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interruption
                }
            }
        }
    }


    //Creates or logs in before doing any vendor activity
    public void createAccount() {
        if (!isLoggedIn) {
            try {
                // If the vendor is already registered, log them in
                if (userService.isUserRegistered(username)) {
                    userService.login(username, password);
                    isLoggedIn = true;
                    sendTicketUpdate(name + " logged in.");
                } else if(!userService.isUserRegistered(username)){ //If the vendor is not registered, create a new account
                    username = userService.registerNewUser(username, password, User.Role.VENDOR, false);
                    isLoggedIn = true;
                    sendTicketUpdate(name + " registered successfully with username: " + username + " and password: " + password);
                }
                sendTicketUpdate(name + " account status: " + (isLoggedIn ? "Logged in" : "Failed to log in"));
            } catch (Exception e) {
                sendTicketUpdate("Account creation failed for vendor: " + name + ". Error: " + e.getMessage());
            }
        }
    }

    //Randomly changes the price for tickets for the event category
    public void randomTicketPriceChange() {
        String newPrice = String.valueOf(random.nextInt(5000, 7000)); // Generate a random price between 5000 and 7000
        eventCategory.setPrice(newPrice); // Set the new price for the event category
        sendTicketUpdate(name + " changed price. Price for {" + eventCategory.getEventName() + "} is " + newPrice);
    }
}
