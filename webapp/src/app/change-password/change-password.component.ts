import {Component, OnInit} from '@angular/core'
import {AbstractControl, FormControl, FormGroup, ValidatorFn} from '@angular/forms'
import {UserService} from '../user.service'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {Router} from '@angular/router'

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  loading: boolean = false

  pwdForm: FormGroup = new FormGroup({
    pwd: new FormControl(''),
    pwd2: new FormControl('', this.checkPwd2())
  })

  constructor(
    private userService: UserService,
    private stateService: StateService,
    private router: Router
  ) {
  }

  get pwd(): AbstractControl {
    return this.pwdForm.get('pwd')
  }

  get pwd2(): AbstractControl {
    return this.pwdForm.get('pwd2')
  }

  checkPwd2(): ValidatorFn {
    return control => control.value === control.parent?.get('pwd').value ? null : {notSame: true}
  }

  ngOnInit(): void {
  }

  submit(): void {
    this.loading = true
    this.userService.updatePassword(this.pwd.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        () => this.stateService.sendSuccessEvent('Mot de passe mis à jour !'),
        error => handleError(error, this.stateService, this.router)
      )
  }
}
