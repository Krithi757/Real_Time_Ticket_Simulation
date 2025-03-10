import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CustomerRetrievalService {
  private customerRetrievalSource = new BehaviorSubject<number | null>(null);
  customerRetrieval$ = this.customerRetrievalSource.asObservable();

  setcustomerRetrieval(value: number): void {
    this.customerRetrievalSource.next(value);
  }
}
