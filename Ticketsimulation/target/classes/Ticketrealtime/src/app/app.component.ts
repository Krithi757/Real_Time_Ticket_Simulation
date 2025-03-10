import { Component } from '@angular/core';
import {MainPageComponent} from './Component/MainPage.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  standalone: true, // Declare AppComponent as standalone
  imports: [MainPageComponent], // Import your standalone LoginComponent
})
export class AppComponent {
    title: "Ticketrealtime" | undefined;
}
