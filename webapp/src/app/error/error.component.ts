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
    this.stateService.clearError()
  }

  closeSuccess(): void {
    this.stateService.clearSuccess()
  }

  ngOnInit(): void {
    this.stateService.subscribeMessageEvent(
      success => this.success = success,
      error => this.error = error
    )
  }

}
