import {Component, OnInit} from '@angular/core'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {LoginService} from '../login.service'
import {faSignInAlt, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {finalize} from 'rxjs/operators'
import {Router} from '@angular/router'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  loginIcon: IconDefinition = faSignInAlt

  loading: boolean = false
  error: string = null

  loginForm: FormGroup = new FormGroup({
    username: new FormControl(''),
    password: new FormControl('')
  })

  constructor(
    private loginService: LoginService,
    private router: Router
  ) {
  }

  get username(): AbstractControl {
    return this.loginForm.get('username')
  }

  get password(): AbstractControl {
    return this.loginForm.get('password')
  }

  login(): void {
    this.loading = true
    this.loginService.login(this.username.value, this.password.value)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        () => this.router.navigate(['home']),
        error => {
          if (error.status === 401) {
            this.error = 'Identifiant ou mot de passe invalide.'
          } else {
            console.error(error)
            this.error = 'Une erreur inconnue s\'est produite. Veuillez réessayer ultérieurement.'
          }
        }
      )
  }

  ngOnInit(): void {
  }
}
