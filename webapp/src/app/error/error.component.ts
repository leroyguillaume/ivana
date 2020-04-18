import {Component, OnInit} from '@angular/core'
import {StateService} from '../state.service'

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.css']
})
export class ErrorComponent implements OnInit {
  error: string = null
  success: string = null

  constructor(
    private stateService: StateService
  ) {
  }

  closeError(): void {
    this.stateService.error.next(null)
  }

  closeSuccess(): void {
    this.stateService.success.next(null)
  }

  ngOnInit(): void {
    this.stateService.error.subscribe(error => this.error = error)
    this.stateService.success.subscribe(success => this.success = success)
  }

}
