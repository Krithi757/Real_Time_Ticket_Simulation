# Real Time Ticketing System

## Introduction

This project is real-time ticketing system that supports concurrent ticket releases and purchases. It utilizes multi-threading,
synchronization and Reentrant locks to handle concurrency and to prevent race conditions while ensuring data integrity. The
system offers features such as ticket simulation, input validation, error handling, real-time updates, start\stop commands
and a customer login through the frontend. Bonus features includes a visual chart, VIP customer implementation, usage of locks and SQLite database
to store ticketing information.

The frontend is built with Angular, while the backend uses Spring Boot and SQLite as the database. A command line version (CLI)
implemented in core Java is also included for backend simulation and printing logs.

---

## Features

### Core Features

- **Concurrent Ticketing**: Supports multiple vendors and customers purchasing tickets in real-time.
- **Input Validation**: Input parameters are validated and prompts users again if not valid.
- **Error Handling**: Prints meaningful error messages to the user about what went wrong.
- **Logging**: Prints log messages on CLI by use File logging.
- **Configuration**: Configures input parameters and serializes them using gson and saves them to a text file.
- **Authentication**: Automatically generates secure usernames and passwords for vendors and customers where passwords are hashed and stored securely using BCrypt for robust security using spring security.

### Bonus Features

- **Advanced Synchronization**: Uses Reentrant locks to ensure ticket acquisition and to obtain more control over synchronization.
- **Priority Customers**: System assumes that if a customer makes over 5 purchases customer becomes a VIP customer. Uses
  Comparable interface to prioritize Vip customers over normal customers.
- **Persistence**: Saves ticketing, user data to a SQLite database that implements basic CRUD operations through Spring Data JPA.
- **Visual Chart**: Uses chart.js in angular to implement a line chart that displays ticketing metrics in real-time in a visual way.

---

## Technology Stack

- **Backend**: Spring Boot
- **CLI Implementation**: Core Java (Uses FileHandler for Logging)
- **Database**: SQLite
- **Frontend**: Angular (RxJS, @Input/@Output for communication)

---

## Setup Instructions

### Prerequisites

Ensure the following are installed in your system

- **Node.js**: Version 14 or above
- **Angular CLI**: Latest version (`npm install -g @angular/cli`)
- **Java**: Version 17 or above
- **SQLite**: For database storage
- **StompJs**: For websocket communication (`npm install @stomp/stompsjs`)
- **Chart.js**: For real-time charting (`npm install chart.js`)

### Backend Setup

1. Navigate to the Ticketsimulation directory in your local machine.
2. Open the directory in an IDE (eg. Intellij or Eclipse) that supports spring boot projects.
3. Ensure all dependencies are downloaded by allowing the IDE to build the project.
4. Locate the TicketsimulationApplication.java file.
5. Run the application by right-clicking the file and selecting Run or using the IDE's run functionality.
6. Verify the backend is running by visiting http://localhost:8080 in your browser.

### Command Line Interface (CLI) Setup

1. Navigate to the Ticketsimulation directory in your local machine.
2. Navigate to TicketCLI file under view package inside the Ticketsimulation project.
3. Run the application by right-clicking the file and selecting Run or using the IDE's run functionality.

### Frontend Setup

1. Navigate to the Ticketsimulation directory in your local machine.
2. Navigate to the Ticketrealtime directory under resources directory inside the Ticketsimulation project.
   ```bash
   cd Ticketrealtime
   ```
3. Install dependencies
   ```bash
   npm install @stomp/stompsjs
   npm install chart.js
   ```
4. Start the angular application
   ```bash
   ng serve
   ```
5. Access the application at http://localhost:4200.

---

## Usage

### Command Line Interface (CLI) Usage

1. Run the backend application to enable the CLI.
2. Enter the following parameters when prompted:
    - **Total Tickets**: Total number of tickets that can be sold in teh system.
    - **Ticket Release Rate**: Maximum number of tickets that can be released by a vendor at a time.
    - **Customer Retrieval Rate**: Maximum number of tickets that can be bought by a customer at a time.
    - **Maximum Capacity**: Maximum number of tickets that can be held temporarily by teh ticket pool.
3. If any input is not valid, an error message will be displayed and the system will prompt teh user to enter valid inputs.
4. Once all the inputs are valid the simulation will start automatically.
5. All logs from the customer, vendor, ticketpool classes are written to a central text file and will be displayed in chronological order.
6. Type stop to terminate all CLI processes.
7. The processes will be stopped gracefully and all data will be saved.

### Frontend usage

1. Open the application in your browser at http://localhost:4200.
2. Navigate to the ticket simulation form and input the following.
    - **Total Tickets**: Total number of tickets that can be sold in teh system.
    - **Ticket Release Rate**: Maximum number of tickets that can be released by a vendor at a time.
    - **Customer Retrieval Rate**: Maximum number of tickets that can be bought by a customer at a time.
    - **Maximum Capacity**: Maximum number of tickets that can be held temporarily by teh ticket pool.
3. If any input is not valid, an error message will be displayed and the system will prompt teh user to enter valid inputs.
4. Click Submit to start the simulation.
5. Navigate to the customer login form in the same page and input teh following.
    - **Username**: Enter customer's username.
    - **Password**: Enter customer password.
    - **Number of tickets to buy**: Enter number of tickets to be bought.
6. Click Register/ Login to be successfully registered or logged in.
7. On success registration or logging in, the system will display an alert message saying that the customer has registered/ logged in.
8. On successful purchase, the ticket details will be displayed right below the login form.
9. If purchase failed, the message failure message will be printed.
10. The updates container dynamically displays all logs related to ticket sales, including any real time purchases sent using the customer login form.
11. Use the 'stop all processes' to stop all processes and to disconnect the websocket connection.

---

### Assumptions

The CLI system has been designed as a client that seamlessly connects to the backend, leveraging REST API as the
communication bridge. This approach was chosen because the backend is implemented using Spring Boot, while the CLI
is built using core Java without relying on Spring Boot frameworks or annotations. By using REST API, the CLI and
backend remain modular, ensuring a clean separation of concerns. This design allows the CLI to act as a lightweight
client that interacts efficiently with the backend through well-defined REST API calls, maintaining flexibility and
scalability in the system

By using REST API, the backend becomes a centralized service that can handle requests from both the GUI and CLI.

---

### Additional Notes

1. Even if the simulation is run from backend CLI, real-time updates will appear in the frontend's updates container, as both CLI and GUI
   are connected via REST apis, thus making it optional for the user to simulate using the frontend or the CLI.
2. Advanced synchronization using Reentrant lock ensures thread safety during concurrent ticket operations.
3. CLI logs are consolidated in a single file, with the CLI monitoring and displaying the latest entries for real-time insights.
4. Ticket, vendor and customer data are stored in an SQLite database, ensuring durability and enabling basic CRUD operations.
5. The modular design enables easy expansion, supporting new features like advanced analytics, multi-event ticketing or enhanced user roles with minimal disruption.

---

## Contributor

Krithiga Thachanamoorthy

## Contributions

- Developed the entire system including backend (Spring Boot),CLI , frontend (Angular) and database (SQLite).
- Designed and implemented features such as ticket simulation, VIP prioritization, real-time updates, logging, and chart visualizations.
- Authored documentation and conducted testing to ensure system reliability.
