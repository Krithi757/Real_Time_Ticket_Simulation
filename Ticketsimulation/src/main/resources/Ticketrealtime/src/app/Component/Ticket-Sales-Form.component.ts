import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { CustomerRetrievalService } from './CustomerRetrieval.service';

@Component({
  selector: 'app-ticket-sales-form', // The selector used to include this component in templates
  templateUrl: './Ticket-Sales-Form.component.html', // Path to the HTML template for this component
  styleUrls: ['./MainPage.component.css'], // Path to the CSS file for this component
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Modules required by this component
})
export class TicketSalesFormComponent implements OnInit, OnDestroy {
  @Input() stompClient: Client | null = null; // Input property to receive a WebSocket client instance

  ticketForm: FormGroup; // Reactive form for ticket purchase
  ticketResponseMessage$ = new Subject<string>(); // Observable to store and emit ticket sales response messages
  private destroy$ = new Subject<void>(); // Subject to manage component destruction and clean-up

  constructor(private fb: FormBuilder, private customerRetrievalService: CustomerRetrievalService) {
    this.ticketForm = this.fb.group({
      totalTickets: ['', [Validators.required, Validators.min(1)],], // Field for total tickets with required and minimum value validation
      ticketRelease: ['', [Validators.required, Validators.min(1)],], // Field for ticket release with validation
      customerRetrieval: ['', [Validators.required, Validators.min(1)],], // Field for customer retrieval with validation
      maxCapacity: ['', [Validators.required, Validators.min(1)],], // Field for max capacity with validation
    },
      {
        validators: [this.validateTicketValues()],
        // Custom validation to ensure ticket values don't exceed constraints
    }
    );
  }

  ngOnInit(): void {
    // Lifecycle hook for initialization logic (if needed)
  }

  ngOnDestroy(): void { // Lifecycle hook for clean-up when the component is destroyed
    this.destroy$.next();
    this.destroy$.complete(); // Signal completion for observables to avoid memory leaks
  }

  handleTicketFormSubmit(): void {
    // Handle form submission for starting the ticket sales process
    if (this.ticketForm.invalid) {
      return; // Exit if the form is invalid
    }

    const ticketData = this.ticketForm.value; // Retrieve form values to send as ticket data
    const customerRetrieval=ticketData.customerRetrieval; // Extract max capacity to set in the MaxcapacityService
    this.customerRetrievalService.setcustomerRetrieval(customerRetrieval);

    if (this.stompClient?.connected) {
      // Check if the WebSocket connection is active
      this.stompClient.publish({
        // WebSocket destination endpoint for ticket sales
        destination: '/app/start-ticket',
        body: JSON.stringify(ticketData),
      });
      // Emit success message
      this.ticketResponseMessage$.next('Ticket sales process started via WebSocket!');
    } else {
      // Emit error message if WebSocket is disconnected
      this.ticketResponseMessage$.next('WebSocket is not connected.');
    }
  }
  private validateTicketValues(){
    // Custom validator to ensure ticket-related constraints
    return (form: FormGroup) => {
      const totalTickets=form.get('totalTickets')?.value;
      const ticketRelease=form.get('ticketRelease')?.value;
      const customerRetrieval=form.get('customerRetrieval')?.value;
      const maxCapacity=form.get('maxCapacity')?.value;
      // Check if any of the ticket values exceed the total tickets
      if((ticketRelease>totalTickets)||(customerRetrieval>totalTickets)||(maxCapacity>totalTickets)){
        return {exceedsTotalTickets: true};  // Return error if constraints are violated
      }
      return null; // Return null if validation passes
    };
  }
}
