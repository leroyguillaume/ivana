import {Component, OnInit} from '@angular/core'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {Role} from '../role'
import {UserService} from '../user.service'
import {finalize} from 'rxjs/operators'
import {StateService} from '../state.service'
import {handleError} from '../util'

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {
  leftArrowIcon: IconDefinition = faArrowLeft
  spinnerIcon: IconDefinition = faSpinner

  roles: { role: Role, label: string }[] = [
    {
      role: Role.User,
      label: 'Simple utilisateur'
    },
    {
      role: Role.Admin,
      label: 'Administrateur'
    }
  ]
  loading: boolean = false

  userForm: FormGroup = new FormGroup({
    name: new FormControl(''),
    pwd: new FormControl(''),
    role: new FormControl(Role.User)
  })

  constructor(
    private userService: UserService,
    private stateService: StateService
  ) {
  }

  get pwd(): AbstractControl {
    return this.userForm.get('pwd')
  }

  get name(): AbstractControl {
    return this.userForm.get('name')
  }

  get role(): AbstractControl {
    return this.userForm.get('role')
  }

  ngOnInit(): void {
  }

  submit(): void {
    this.loading = true
    this.userService.create(this.name.value, this.pwd.value, this.role.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        () => this.stateService.success.next('Utilisateur créé !'),
        error => handleError(error, this.stateService)
      )
  }
}