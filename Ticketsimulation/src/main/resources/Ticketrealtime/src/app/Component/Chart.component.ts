import { Component, Input, OnInit, OnChanges, SimpleChanges, ElementRef, ViewChild } from '@angular/core';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-charts', //Selector for embedding the component
  templateUrl: './Chart.component.html', //Path to the HTML template for the component
  styleUrls: ['./MainPage.component.css'], //Path to the CSS file
  standalone: true
})
export class ChartsComponent implements OnInit, OnChanges {
  @Input() vendorSalesData: any[] = []; //Input property for vendor sales data
  @Input() customerPurchaseData: any[] = []; //Input property for customer purchase data
  @ViewChild('lineChart') lineChartRef!: ElementRef<HTMLCanvasElement>; // Reference to the canvas element used for rendering the chart
  chart: Chart | null = null; // Holds the Chart.js instance; initialized as null

  ngOnInit(): void {
    this.updateChart(); // Initial render
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Detect changes to input properties and update the chart accordingly
    if (changes['vendorSalesData'] || changes['customerPurchaseData']) {
      this.updateChart();
      // Update the chart if vendor or customer data changes
    }
  }

  updateChart(): void {
    // Updates the chart with new data or creates it if it doesn't exist
    // Array to hold chart labels
    const labels: string[] = [];
    // Array for vendor sales data
    const vendorSales: number[] = [];
    //Array for customer purchase data
    const customerPurchases: number[] = [];
    // Map to consolidate vendor sales and customer purchase data by name
    const dataMap = new Map();
    // Populate the map with vendor sales data
    this.vendorSalesData.forEach((item) => {
      dataMap.set(item.name, { name: item.name, vendorSales: item.vendorSales, customerPurchases: 0 }); // Default customer purchases to 0 for names only in vendor data
    });
    // Populate the map with customer purchase data or update existing entries
    this.customerPurchaseData.forEach((item) => {
      if (dataMap.has(item.name)) {  // If the name already exists, update customer purchase value
        dataMap.get(item.name).customerPurchases = item.customerPurchases;
      } else {
        // If the name is new, add it to the map
        // Default vendor sales to 0 for names only in customer data
        dataMap.set(item.name, { name: item.name, vendorSales: 0, customerPurchases: item.customerPurchases });
      }
    });
    // Extract consolidated data for labels, vendor sales, and customer purchases
    dataMap.forEach((value) => {
      labels.push(value.name);
      vendorSales.push(value.vendorSales);
      customerPurchases.push(value.customerPurchases);
    });
    // Update the chart if it already exists; otherwise, create a new one
    if (this.chart) {
      this.chart.data.labels = labels; // Update chart labels
      this.chart.data.datasets[0].data = vendorSales; // Update vendor sales dataset
      this.chart.data.datasets[1].data = customerPurchases; // Update customer purchases dataset
      this.chart.update(); // Re-render the updated chart
    } else {
      // Create a new chart if it doesn't already exist
      this.createChart(labels, vendorSales, customerPurchases);
    }
  }

  createChart(labels: string[], vendorSales: number[], customerPurchases: number[]): void {
    const ctx = this.lineChartRef.nativeElement.getContext('2d');
    if (!ctx) return;

    this.chart = new Chart(ctx, {
      type: 'line', // Chart type is a line chart
      data: {
        labels, // X-axis labels
        datasets: [
          {
            label: 'Vendor Sales', // Label for vendor sales dataset
            data: vendorSales, // Data points for vendor sales
            borderColor: '#8884d8',
            backgroundColor: 'rgba(136, 132, 216, 0.2)', // Background fill for vendor sales
            tension: 0.4, // Smoothness of the line
            fill: true, // Enable area fill under the line
          },
          {
            label: 'Customer Purchases',
            data: customerPurchases,
            borderColor: '#82ca9d',
            backgroundColor: 'rgba(130, 202, 157, 0.2)',
            tension: 0.4,
            fill: true,
          },
        ],
      },
      options: {
        responsive: true, // Makes the chart responsive to container size
        plugins: {
          legend: {
            position: 'top', // Position the legend at the top
          },
        },
        scales: {
          x: {
            title: {
              display: true, // Display title for X-axis
              text: 'Name', // Title text for X-axis
            },
          },
          y: {
            title: {
              display: true, // Display title for Y-axis
              text: 'Values', // Title text for Y-axis
            },
            beginAtZero: true, // Start the Y-axis at zero
          },
        },
      },
    });
  }
}
