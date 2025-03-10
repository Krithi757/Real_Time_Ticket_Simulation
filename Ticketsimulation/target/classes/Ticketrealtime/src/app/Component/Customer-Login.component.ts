import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import {Subject, Subscription} from 'rxjs';
import { Client } from '@stomp/stompjs';
import { CommonModule } from '@angular/common';
import { CustomerRetrievalService } from './CustomerRetrieval.service';

@Component({
  selector: 'app-customer-login', // The selector used to include this component in templates
  templateUrl: './Customer-Login.component.html', // Path to the HTML template for this component
  styleUrls: ['./MainPage.component.css'], // Path to the CSS file for this component
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Modules required by this component
})
export class CustomerLoginComponent implements OnInit, OnDestroy {
  @Input() stompClient: Client | null = null; // Input property to receive a WebSocket client instance
  @Input() purchaseSuccess$: Subject<string> | null=null; // Input property to handle purchase success messages

  customerLoginForm: FormGroup; // Reactive form for customer login and ticket purchase
  customerResponseMessage$ = new Subject<string>(); // Observable to store and emit customer response messages

  private destroy$ = new Subject<void>(); // Subject to manage component destruction and clean-up
  private subscription: Subscription | null = null; // Subscription for observing the maximum ticket capacity
  maxCapacity: number | null = null; // Variable to store the maximum ticket capacity for validation

  // FormBuilder service for creating reactive forms and service to fetch and update maximum ticket capacity
  constructor(private fb: FormBuilder, private customerRetrievalService: CustomerRetrievalService) {
    // Initialize the reactive form with controls for username, password, and tickets to buy
    this.customerLoginForm = this.fb.group({
      username: ['', Validators.required], // Username is required
      password: ['', [Validators.required, Validators.minLength(8)],], // Password is required with a minimum length of 8
      ticketsToBuy: ['', [Validators.required, Validators.min(1)],], // Tickets to buy is required with a minimum of 1

    });
  }

  ngOnInit(): void {
    // Subscribe to the maxCapacity$ observable to dynamically set validators for ticket quantity
    this.subscription = this.customerRetrievalService.customerRetrieval$.subscribe((value) => {
      this.maxCapacity = value; // Update the maximum capacity value
      // Update the validators for the ticketsToBuy control
      this.customerLoginForm.get('ticketsToBuy')?.setValidators([
        Validators.required,
        Validators.min(1),
        Validators.max(this.maxCapacity || Infinity),
      ]);
      this.customerLoginForm.get('ticketsToBuy')?.updateValueAndValidity(); // Update the control's validation state
    });
  }

  ngOnDestroy(): void {
    // Unsubscribe from the maxCapacity$ observable to prevent memory leaks
    this.subscription?.unsubscribe();
    // Signal the destroy$ subject to clean up any subscriptions linked to it
    this.destroy$.next();
    this.destroy$.complete();
  }

  handleCustomerLoginSubmit(): void {
    // Ensure the form is valid before processing
    if (this.customerLoginForm.invalid) {
      return;
    }

    const customerData = this.customerLoginForm.value; // Get form data

    // Check if the WebSocket connection is active
    if (this.stompClient?.connected) {
      // Publish the customer data to the WebSocket server
      this.stompClient.publish({
        destination: '/app/realtime-customer-purchase', // WebSocket destination for customer purchase requests
        body: JSON.stringify(customerData), // Serialize form data to JSON
      });
      this.customerResponseMessage$.next('Customer purchase request sent via WebSocket!');
    } else {
      // Emit an error message if the WebSocket connection is not active
      this.customerResponseMessage$.next('WebSocket is not connected.');
    }
  }
}
