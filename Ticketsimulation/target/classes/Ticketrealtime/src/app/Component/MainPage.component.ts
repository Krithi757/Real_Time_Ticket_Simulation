import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Client, Message } from '@stomp/stompjs'; // STOMP client for WebSocket communication
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { takeUntil, filter, map, tap } from 'rxjs/operators';
import './MainPage.component.css';
import {ChartsComponent} from './Chart.component'; // Component to display charts
import {CustomerLoginComponent} from './Customer-Login.component'; // Component for customer login
import {TicketSalesFormComponent} from './Ticket-Sales-Form.component'; // Component for ticket sales form
import {AvailableTicketsComponent} from './Available-Tickets.component'; // Component for getting available tickets

@Component({
  selector: 'app-login-signup', // Component selector used in the HTML template
  templateUrl: './MainPage.component.html', // HTML template for this component
  styleUrls: ['./MainPage.component.css'], // Stylesheet for this component
  standalone: true, // Indicates that this component is a standalone component
  imports: [CommonModule, ReactiveFormsModule, ChartsComponent, CustomerLoginComponent, TicketSalesFormComponent, AvailableTicketsComponent], // Other modules and components imported for use within this component
})
export class MainPageComponent implements OnInit, OnDestroy {
  // BehaviorSubject to track WebSocket connection status
  connectionStatus$ = new BehaviorSubject<string>('Connecting...');
  generalUpdates: any[] = []; // Array to store general updates from the server
  customerUpdates: any[] = []; // Array to store customer-related updates
  purchaseSuccess$ = new Subject<string>(); // Subject to emit purchase success messages
  stompClient: Client | null = null; // STOMP client for WebSocket communication
  vendorSalesData: any[] = []; // Array to store vendor sales data
  customerPurchaseData: any[] = []; // Array to store customer purchase data
  avaialableTickets: number | null = null; // Number of available tickets null if not fetched yet



  private destroy$ = new Subject<void>(); // Subject to manage component destruction and cleanup
  // Lifecycle hook called when the component is initialized
  ngOnInit(): void {
    this.setupWebSocket(); // Initialize WebSocket connection
  }
  // Lifecycle hook called when the component is destroyed
  ngOnDestroy(): void {
    this.destroy$.next(); // Emit a signal to unsubscribe from observables
    this.destroy$.complete(); // Complete the destroy subject

    if (this.stompClient) {
      this.stompClient.deactivate(); // Deactivate WebSocket connection
    }
  }
  //Halts the WebSocket connection and pauses all updates.
  stopProcess() {
    console.log('Halting all processes...');

    // Stop WebSocket connection (halt real-time updates)
    if (this.stompClient?.connected) {
      this.stompClient.deactivate(); // Disconnect WebSocket without clearing data
      console.log('WebSocket connection deactivated.');
    }

    // Pause observable subscriptions to halt updates
    this.destroy$.next(); // Signals to stop processing updates
    console.log('Observable updates paused.');

    // Retain all data in their current state (no clearing)
    // The following ensures the data remains not entirely cleared
    console.log('Current state retained:', {
      generalUpdates: this.generalUpdates,
      vendorSalesData: this.vendorSalesData,
      customerPurchaseData: this.customerPurchaseData,
      availableTickets: this.avaialableTickets,
    });
  }
  //Configures and establishes the WebSocket connection using STOMP protocol
  setupWebSocket(): void {
    this.stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ticket-updates',
      onConnect: () => {
        this.connectionStatus$.next('Connected');
        console.log('WebSocket connected');
        // Subscribe to various WebSocket topics
        this.subscribeToTopic('/topic/ticket-updates', 'general');
        this.subscribeToTopic('/topic/ticket-updates', 'customer');
        this.subscribeToTopic('/topic/vendor-sales', 'vendor');
        this.subscribeToTopic('/topic/customer-purchases', 'customer');
        this.subscribeToTopic('/topic/available-tickets', 'available-tickets');

      },
      onWebSocketClose: () => {
        this.connectionStatus$.next('Disconnected'); // Update connection status
        console.log('WebSocket disconnected');
      },
      onStompError: (frame) => {
        this.connectionStatus$.next('Connection error'); // Update connection status on error
        console.error('WebSocket error:', frame);
      },
    });

    this.stompClient.activate(); // Activate the WebSocket connection
  }

//Subscribes to a WebSocket topic and processes incoming messages
  subscribeToTopic(topic: string, type: string): void {
    if (!this.stompClient) {
      console.log('STOMP client is not ready');
      return;
    }
    // Create an observable for the WebSocket messages
    const topicSubscription = new Observable<Message>((observer) => {
      const subscription = this.stompClient?.subscribe(topic, (message) => {
        observer.next(message);
      });
      return () => subscription?.unsubscribe(); // Unsubscribe when the observable completes
    });
    // Process the observable messages
    topicSubscription
      .pipe(
        takeUntil(this.destroy$), // Unsubscribe when the component is destroyed
        map((message) => JSON.parse(message.body)), // Parse the message body as JSON
        tap((update) => console.log(`Received update for ${type}:`, update)) // Log the received updates
      )
      .subscribe((update) => {
        if (topic === '/topic/ticket-updates') {
          if (type === 'general') {
            if(update.type!='customer'){ // Add to general updates if not customer-related
              this.generalUpdates.push(update);
            }
          } else if (update.type === 'customer') {
            this.customerUpdates.push(update); // Add to customer updates
            if (update.success === 'success') {
              alert(update.register); // Show success alert
              this.purchaseSuccess$.next(update.message);  // Set the success message
            } else if (update.success === 'false') {
              update.message = 'Attempting to buy tickets';
              this.purchaseSuccess$.next(update.message);  // Set the failure message
            }          }
        } else if (topic === '/topic/vendor-sales') {
          this.vendorSalesData = Object.keys(update).map((key) => ({
            name: key,
            vendorSales: update[key],
          })); // Process vendor sales data
        } else if (topic === '/topic/customer-purchases') {
          this.customerPurchaseData = Object.keys(update).map((key) => ({
            name: key,
            customerPurchases: update[key],
          })); // Process customer purchase data
        }else if(topic === '/topic/available-tickets') {
          this.avaialableTickets = update.availableTickets ?? 0; // Update available tickets

        }
      });
  }
}
