package com.example.ticketsimulation.model.entity;
import com.example.ticketsimulation.controller.CustomerRequestController;
import com.example.ticketsimulation.controller.TicketController;
import com.example.ticketsimulation.model.service.UserService;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.*;

//Implements Runnable to allow each customer to run in a separate thread, and Comparable to prioritize VIP customers
public class Customer implements Runnable, Comparable<com.example.ticketsimulation.model.entity.Customer> {
    private static final Logger LOGGER = Logger.getLogger("TicketSimulationLog"); //Logger to write logs to a central file to print in CLI
    private final TicketPool ticketpool;
    private final TicketController ticketcontroller;
    private int realtimeTicketsToBuy = -1;

    @Getter
    private String name; //Customer name
    private final int customerRet; //Number of requests that can be made by a customer
    @Getter
    @Setter
    private boolean isRealtime;
    @Getter
    private int loyaltyPoints; //Specifies loyalty points to prioritize VIP customers
    private static FileHandler fileHandler;
    private boolean isVIP; //Flag to check if customer is VIP
    private String username;
    private String password;
    private RealtimeCustomerRequest realtimeCustomerRequest;

    private final UserService userService; //Service for managing user accounts
    private boolean isLoggedIn = false; //Flag to track login status

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 8;

    // Static block to configure the Logger for customer logs
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

    //Constructor to initialize Customer with its details and dependencies
    public Customer(TicketPool ticketpool, UserService userService, String name, int customerRet, TicketController ticketcontroller, boolean isRealtime) {
        this.ticketpool = ticketpool;
        this.userService = userService;
        this.name = name;
        this.customerRet = customerRet;
        this.ticketcontroller = ticketcontroller;
        this.loyaltyPoints = 0;
        this.isVIP = false;
        this.password = generatePassword();
        this.isRealtime = isRealtime;


    }

    //Function to check if customer is VP
    public boolean isVIP() {
        return isVIP;
    }

    public void setpassword(String password){
        this.password=password;
    }




    public void setrealtimerequest(int ticketrequest){
        realtimeTicketsToBuy = ticketrequest;
    }


    //sendTicketUpdate updates the print messages to the logger and the websocket
    private void sendTicketUpdate(String message) {
        LOGGER.info(message);
        ticketcontroller.addToQueue(message); // Add message to the queue
        ticketcontroller.sendUpdate(message);  // Send the message to the frontend
    }


    //Increases loyalty points with each purchase
    public void incrementLoyaltyPoints() {
        loyaltyPoints++;
        if (loyaltyPoints >= 5) {
            if (loyaltyPoints == 5) { //if loyalty points is 5 or greater than 5 customer is a VIP customer
                upgradeToVIP();
            } else {
                isVIP = true;
            }
        }
    }




    //When customer first becomes a VIP customer prints "customer became a VIP customer"
    private void upgradeToVIP() {
        isVIP = true;
        sendTicketUpdate(name + " became a VIP customer.");
    }
    public void setUsername(String username) {
        this.username = username;
    }

    //Creates or logs in the customer account through the user service
    public void createAccount() {
        if (!isLoggedIn) {// Only register or log in if not already logged in
            try {
                if (username != null && !username.isEmpty() && userService.isUserRegistered(username)) {
                    userService.login(username, password);
                    isLoggedIn = true;
                    sendTicketUpdate(name + " logged in with username: " + username);
                    if(isRealtime) {
                        CustomerRequestController.register(name + " logged in with username: " + username);
                    }
                } else {
                    if(isRealtime){
                        username = userService.registerNewUser(username, password, User.Role.CUSTOMER, true);
                    }else{
                        username = userService.registerNewUser(username, password, User.Role.CUSTOMER, false);
                    }
                    isLoggedIn = true;
                    sendTicketUpdate(name + " registered successfully with username: " + username + " and password: " + password);
                    if(isRealtime){
                        CustomerRequestController.register(name + " successfully registered with username: " + username);
                    }
                }
                sendTicketUpdate(name + " account status: " + (isLoggedIn ? "Logged in" : "Failed to log in"));
            } catch (Exception loginException) {
                sendTicketUpdate("Login failed for user: " + username);
            }
        }
    }


    //Run method to continuously request tickets while the customer is active
    @Override
    public void run() {
        int releaseDelay=1000/customerRet;
        for (int i = 0; i < customerRet; i++) {
            createAccount(); // Account setup at the start
            ticketpool.requestTicket(this); // Make a request for tickets
            try {
                Thread.sleep(releaseDelay); // Delay between requests
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle interruption
                return;
            }
            ticketpool.removeTicket(); // Process the request in the ticket pool
        }
    }


    //Generate random unique password
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    //Gets the number of requested tickets by each customer
    public int getRequestedTickets() {
        return realtimeTicketsToBuy > 0 ? realtimeTicketsToBuy : (int) (Math.random() * customerRet) + 1;
        //return (int) (Math.random() * customerRet) + 1;
    }



    //Compares customers based on VIP priority or non-VIP priority
    @Override
    public int compareTo(com.example.ticketsimulation.model.entity.Customer other) {
        return Boolean.compare(other.isVIP, this.isVIP);
    }
}

