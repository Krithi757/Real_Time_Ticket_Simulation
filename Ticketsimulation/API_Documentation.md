# Ticket Simulation API Documentation

## Introduction
The Ticket Simulation API provides endpoints to manage and simulate ticket-related activities. It allows users to start
ticket simulations, handle real-time customer ticket purchases, and get updates on ticket availability, vendor sales,
and customer purchases via WebSocket connections.

## Authentication
The Ticket Simulation API uses username and password-based authentication implemented with Spring Security.
While the authentication mechanism is not directly associated with API headers like Authorization, users are required to
log in through the application interface or other predefined methods.

- **User Credentials**: Unique usernames and passwords are generated for vendors and customers. Passwords are securely hashed using the BCrypt algorithm before storage.
- **Role-Based Access**:
  - Vendors have access to endpoints under /vendor/**.
  - Customers have access to endpoints under /customer/**.

## Endpoints

### 1. **Start Ticket Simulation**
#### **URL:** `http://localhost:8080/ticket/start`
#### **HTTP Method:** POST
#### **Description:**
Initializes the ticket simulation process by configuring ticket-related parameters and starting vendor and customer threads.
#### **Request Parameters:**
**Body:**
```json
{
    "totalTickets": 100,
    "ticketRelease": 50,
    "customerRetrieval": 10,
    "maxCapacity": 20
}
 ```

##### **Success Response**
If ticket simulation is initialized successfully.
- **Status Code:** `200 OK`
- **Body:**

```json
{
  "message": "Ticket sales process initialized!"
}
```

##### **Error Response**
If request is sent without running the backend
- **Status Code:** `Could not send request`
- **Body:**

```json
{
  "message":"Could not send request"
}
```

##### **Error Response**
If there is a syntax error in providing the request
- **Status Code:** `400 Bad Request`
- **Body:**

```json
{
  "timestamp": 1732629807903,
  "status": 400,
  "error": "Bad Request"
}
```
___

### 2. **Customer Ticket Purchase**
#### **URL (subscribing topic):** `/topic/realtime-customer-purchase`
#### **HTTP Method:** WebSocket (`ws://localhost:8080/ticket-updates`)
#### **Description:**
Processes a real-time customer request to purchase tickets.

#### **Request Parameters:**
- **Body:**

```json
{
  "username": "goe",
  "password": 984924289832,
  "ticketsToBuy": 3
}
```
#### **Responses:**

##### **Success Response**
If customer registered/ logged in and purchase request sent successfully.
- **Status Code:** `200 OK`
- **Body:**

```json
{
  "type": "customer",
  "success":"success",
  "message": "Yay! you just bought 3 tickets for: {Cooking Event on 2024-11-04 3000}.Total tickets left for all events: 8. You got loyalty points: 1" ,
  "register": "goe registered successfully with username: goe and password: 984924289832"
}
```

##### **Failure Response**
If customer registered/ logged in and purchase request sent successfully but couldn't buy tickets.
- **Status Code:** `200 OK`
- **Body:**

```json
{
  "type": "customer",
  "success":"success",
  "message": "Could not buy requested tickets" ,
  "register": "goe registered successfully with username: goe and password: 984924289832"
}
```
##### **Error Response**
If the input parameters are provided but the WebSocket connection is established.
- **Status Code:** `WebSocket disconnected`
- **Body:**

```
{
  Could not connect to ws: //localhost:8080/ticket-updates
  10:41: 04
  Error: connect ECONNREFUSED 127.0.01:8080 
}
```
___

### 3. **Customer Purchase Update**
#### **URL (subscribing topic):** `/topic/customer-purchases`
#### **HTTP Method:** WebSocket (`ws://localhost:8080/ticket-updates`)
#### **Description:**
Broadcasts updates about tickets purchased by customers in real time.
##### **Success Response**
Customer name is mapped to the number of tickets that they have purchased once the ticket simulation starts.
- **Status Code:** `200 OK`
- **Body:**

```json
{
  "Customer 1": 9,
  "Customer 4": 4,
  "Customer 5": 9,
  "goe": 3
}
```

##### **Error Response**
If there is a syntax error in providing the request
- **Status Code:** `400 Bad Request`
- **Body:**

```json
{
  "timestamp": 1732629807903,
  "status": 400,
  "error": "Bad Request"
}
```
##### **Error Response**
If the input parameters are provided but the WebSocket connection is established.
- **Status Code:** `WebSocket disconnected`
- **Body:**

```
{
  Could not connect to ws: //localhost:8080/ticket-updates
  10:41: 04
  Error: connect ECONNREFUSED 127.0.01:8080 
}
```
___
### 4. **Vendor Ticket Update**
#### **URL (subscribing topic):** `/topic/vendor-sales`
#### **HTTP Method:** WebSocket (`ws://localhost:8080/ticket-updates`)
#### **Description:**
Broadcasts updates about tickets added by vendors in real time.

#### **Request Parameters:** None

#### **Responses:**

##### **Success Response**
Vendor name is mapped to the number of tickets that have added once the ticket simulation starts
- **Status Code:** `200 OK`
- **Body:**

```json
{
  "Vendor1": 10,
  "Vendor2": 15,
  "Vendor5": 13, 
  "Vendor3": 3, 
  "Vendor4": 0
}
```

##### **Error Response**
If the input parameters are provided but teh WebSocket connection is established.
- **Status Code:** `WebSocket disconnected`
- **Body:**

```
{
  Could not connect to ws: //localhost:8080/ticket-updates
  10:41: 04
  Error: connect ECONNREFUSED 127.0.01:8080 
}
```
___

### 5. **Available Tickets Update**
#### **URL (subscribing topic):** `/topic/available-tickets`
#### **HTTP Method:** WebSocket (`ws://localhost:8080/ticket-updates`)
#### **Description:**
Broadcasts the total available tickets in real time.

#### **Request Parameters:** None

#### **Responses:**

##### **Success Response**
Available tickets in the ticket pool are mapped in real time.
- **Status Code:** `200 OK`
- **Body:**

```json
{
  "availableTickets": 15
}
``` 
##### **Error Response**
If the input parameters are provided but teh WebSocket connection is established.
- **Status Code:** `WebSocket disconnected`
- **Body:**

```
{
  Could not connect to ws: //localhost:8080/ticket-updates
  10:41: 04
  Error: connect ECONNREFUSED 127.0.01:8080 
}
```
___

## Error Handling
- **400 Bad Request** - This error occurs when the server cannot understand or process the request because of invalid syntax, malformed data, or missing required parameters.
- **Could not send request** - This is not a standard WebSocket error code but rather a generic message from tools like Postman or a WebSocket client. It usually means the client failed to send the WebSocket message due to a server-side or client-side issue.
- **WebSocket disconnected** – This error indicates that the WebSocket connection between the client and server has been closed unexpectedly.

When facing above errors, make sure to:
•	Check the WebSocket URL, headers, and protocols during the handshake.
•	Confirm that the server is running and accepting connections.
•	Check for missing input parameters or syntax errors when inputting parameters.
•	Verify that the network allows WebSocket traffic (e.g., no firewalls or proxy issues).
___

## Practical Examples
Examples of interacting with APIs is shown in the code snippets by demonstrating success responses and errors responses for each API request.
___ 



