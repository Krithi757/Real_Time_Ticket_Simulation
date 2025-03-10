import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-available-tickets', // Selector to use this component in HTML templates
  templateUrl: './Available-Tickets.component.html', // Path to the HTML template
  styleUrls: ['./MainPage.component.css'], // Path to the CSS file
  standalone: true,
})
export class AvailableTicketsComponent {
  @Input() availableTickets: number | null = null; // Input property to receive the number of available tickets from a parent component
  // Default value is null to handle cases where no value is passed
}
