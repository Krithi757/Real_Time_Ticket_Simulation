package com.example.ticketsimulation.CLI;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.*;

// The CLI for simulating a ticket sales system
public class TicketCLI {

    // A list to hold log messages
    private static final List<String> logMessages = new ArrayList<>();
    // The path of the log file
    private static final String LOG_FILE = "ticket_simulation_logs.txt";  // Unified log file
    // A blocking queue to hold logs for thread-safe processing
    private static final BlockingQueue<String> logQueue = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.WARNING);
        String logFilePath = "ticket_cli.txt";
        Scanner scanner = new Scanner(System.in);

        try (FileWriter fileWriter = new FileWriter(logFilePath, true)) {
            int totalTickets = 0;
            // Collect configuration values from the user
            totalTickets = getIntInput(scanner, "Enter total tickets: ", totalTickets, false);
            int ticketRelease = getIntInput(scanner, "Enter tickets to release by vendor: ", totalTickets, true);
            int customerRetrieval = getIntInput(scanner, "Enter number of customer requests: ", totalTickets, true);
            int maxCapacity = getIntInput(scanner, "Enter maximum capacity of vendors: ", totalTickets, true);

            // Save the configuration to a file
            TicketConfig config = new TicketConfig(totalTickets, ticketRelease, customerRetrieval, maxCapacity);
            TicketConfig.saveToFile(config, "ticket_config.json");
            logMessages.add("Configuration saved to ticket_config.json.");
            fileWriter.write("Configuration saved to ticket_config.json.\n");

            // Read and send configuration data to start ticket process
            String jsonConfig = new String(Files.readAllBytes(Paths.get("ticket_config.json")));
            String startResponse = startTicketProcess(jsonConfig); // Pass JSON directly from file
            logMessages.add("Start Ticket Process Response:\n" + startResponse);
            fileWriter.write("Start Ticket Process Response:\n" + startResponse + "\n");

            // Print and save logs
            for (String logMessage : logMessages) {
                System.out.println(logMessage);
                fileWriter.write(logMessage + "\n");
            }

            // Create CountDownLatch for synchronization
            CountDownLatch vendorLatch = new CountDownLatch(1);   // Ensure vendor starts first
            CountDownLatch customerLatch = new CountDownLatch(1); // Ensure customer starts after vendor
            CountDownLatch ticketpoolLatch = new CountDownLatch(1); // Ensure ticketpool starts after customer

            // Start threads to monitor and display logs
            try {
                // Thread to monitor logs
                Thread logMonitorThread = new Thread(() -> monitorLogs());
                logMonitorThread.start();

                // Thread to display logs
                Thread logDisplayThread = new Thread(() -> displayLogs());
                logDisplayThread.start();

                // Vendor work thread
                Thread vendorWorkThread = new Thread(() -> {
                    try {
                        vendorWork(vendorLatch);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                vendorWorkThread.start();

                // Customer work thread (starts after Vendor)
                Thread customerWorkThread = new Thread(() -> {
                    try {
                        vendorWorkThread.join(); // Wait for Vendor thread to complete
                        customerWork(vendorLatch, customerLatch);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                customerWorkThread.start();

                // Ticketpool work thread (starts after Customer)
                Thread ticketpoolWorkThread = new Thread(() -> {
                    try {
                        customerWorkThread.join(); // Wait for Customer thread to complete
                        ticketpoolWork(customerLatch, ticketpoolLatch);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                ticketpoolWorkThread.start();


                String commandd;
                System.out.println("Type 'stop' to end the CLI.");
                do {
                    commandd = scanner.nextLine();
                } while (!commandd.equalsIgnoreCase("stop"));

                // Stop threads and clear log files
                logMonitorThread.interrupt();
                logDisplayThread.interrupt();
                vendorWorkThread.interrupt();
                customerWorkThread.interrupt();
                ticketpoolWorkThread.interrupt();
                clearLogFile(LOG_FILE);

                System.out.println("CLI stopped and log files cleared.");
            } catch (Exception e) {
                logMessages.add("Error: " + e.getMessage());
                System.out.println("Error: " + e.getMessage());
            }
        }
    }




    // Method to get integer input from the user
    private static int getIntInput(Scanner scanner, String message, int totalTickets, boolean validateagainstTotal) {
        int input = 0;
        boolean validInput = false;

        while (!validInput) {
            System.out.print(message);
            try {
                input = Integer.parseInt(scanner.next());
                if(input<=0){
                    System.out.println("Input cannot be less than or equal to zero. Please enter an integer greater than zero.");
                }else if(input>5000){
                    System.out.println("Input cannot be greater than 5000.");
                }
                else if(validateagainstTotal && input>totalTickets){
                    System.out.println("Input cannot be greater than total tickets");
                }else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer value.");
            }
        }
        return input;
    }


    // Start the ticket process by sending the JSON config to the backend
    private static String startTicketProcess(String jsonInputString) throws Exception {
        URL url = new URL("http://localhost:8080/ticket/start");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        // Write the configuration JSON to the request body
        connection.getOutputStream().write(jsonInputString.getBytes("UTF-8"));

        // Read the response from the backend
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner responseScanner = new Scanner(connection.getInputStream());
            StringBuilder responseBuilder = new StringBuilder();
            while (responseScanner.hasNextLine()) {
                responseBuilder.append(responseScanner.nextLine()).append("\n");
            }
            responseScanner.close();
            return responseBuilder.toString();
        } else {
            throw new Exception("Failed to start ticket process. Response Code: " + responseCode);
        }
    }

    // Monitor the log file for changes and add new lines to the log queue
    private static void monitorLogs() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            // Skip the initial content of the file
            while (reader.readLine() != null) {}

            // Continuously watch for changes in the log file
            while (!Thread.currentThread().isInterrupted()) {
                String line;
                if ((line = reader.readLine()) != null) {
                    logQueue.put(line); // Put the new log into the queue
                }
                if(Files.size(Paths.get(LOG_FILE)) == 1024*1024){
                    clearLogFile(LOG_FILE);
                    logQueue.put("Log file cleared as file exceeded 1MB");
                }
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("log monitoring interrupted.");
            } else {
                System.out.println("Stopping log monitoring: " + e.getMessage());
            }
        }
    }

    // Display logs from the queue in real time
    private static void displayLogs() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String log = logQueue.take(); // Block until a log is available
                System.out.println(log); // Display the log message
            }
        } catch (InterruptedException e) {
            System.out.println("Stopped displaying logs.");
        }
    }

    // Simulate the vendor work process and write log when completed
    private static void vendorWork(CountDownLatch vendorLatch) throws InterruptedException {
        // Simulate vendor log work
        vendorLatch.countDown(); // Signal Customer thread to start
    }

    // Simulate the customer work process and write log when completed
    private static void customerWork(CountDownLatch vendorLatch, CountDownLatch customerLatch) throws InterruptedException {
        vendorLatch.await(); // Wait for Vendor to finish
        // Simulate customer log work
        customerLatch.countDown(); // Signal Ticketpool thread to start
    }

    // Simulate the ticketpool work process and write log when completed
    private static void ticketpoolWork(CountDownLatch customerLatch, CountDownLatch ticketpoolLatch) throws InterruptedException {
        customerLatch.await(); // Wait for Customer to finish
        // Simulate ticketpool log work
    }

    // Method to clear the log file content
    private static void clearLogFile(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath, false); // Overwrite the log file
            writer.close();
        } catch (IOException e) {
            System.out.println("Error clearing log file: " + filePath);
        }
    }
}
