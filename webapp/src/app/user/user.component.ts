import {Component, OnInit} from '@angular/core'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {Role, RoleLabels} from '../role'
import {UserService} from '../user.service'
import {finalize} from 'rxjs/operators'
import {StateService} from '../state.service'
import {handleError} from '../util'
import {Router} from '@angular/router'

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {
  leftArrowIcon: IconDefinition = faArrowLeft
  spinnerIcon: IconDefinition = faSpinner

  roleLabels: { role: Role, label: string }[] = RoleLabels
  loading: boolean = false

  userForm: FormGroup = new FormGroup({
    name: new FormControl(''),
    pwd: new FormControl(''),
    role: new FormControl(Role.User)
  })

  constructor(
    private userService: UserService,
    private stateService: StateService,
    private router: Router
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

  back(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/admin'], {
      queryParams: {
        tab: 'users'
      }
    })
  }

  ngOnInit(): void {
  }

  submit(): void {
    this.loading = true
    this.userService.create(this.name.value, this.pwd.value, this.role.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        () => this.stateService.sendSuccessEvent('Utilisateur créé !'),
        error => handleError(error, this.stateService, this.router)
      )
  }
}
