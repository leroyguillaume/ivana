import {Component, OnInit} from '@angular/core'
import {Page} from '../page'
import {UserService} from '../user.service'
import {StateService} from '../state.service'
import {finalize} from 'rxjs/operators'
import {ActivatedRoute, Router} from '@angular/router'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faSpinner, faTrash, faUserPlus} from '@fortawesome/free-solid-svg-icons'
import {User} from '../user'
import {fetchPageFromQueryParam, handleError} from '../util'
import {LoginService} from '../login.service'
import {Role, RoleLabels} from '../role'

export const UserPageSize: number = 10

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  plusIcon: IconDefinition = faUserPlus
  trashIcon: IconDefinition = faTrash

  Role: typeof Role = Role
  roleLabels: Map<Role, string> = new Map(
    RoleLabels
      .map(roleLabel => [roleLabel.role, roleLabel.label])
  )

  page: Page<User> = null
  loading: boolean = true
  currentUser: User

  constructor(
    private userService: UserService,
    private stateService: StateService,
    private loginService: LoginService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
  }

  delete(user: User): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer l'utilisateur '${user.name}' ?`)) {
      this.loading = true
      this.userService.delete(user.id)
        .pipe(finalize(() => this.loading = false))
        .subscribe(
          () => {
            this.fetchPage(this.page.no)
            this.stateService.sendSuccessEvent(`L'utilisateur '${user.name}' a été supprimé !`)
          },
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  fetchPage(no: number): void {
    this.loading = true
    this.userService.getAll(no, UserPageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.page = page
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
              page: page.no
            }
          })
        },
        error => handleError(error, this.stateService, this.router)
      )
  }

  nextPage(): void {
    this.fetchPage(this.page.no + 1)
  }

  ngOnInit(): void {
    fetchPageFromQueryParam(this.route, (no: number) => this.fetchPage(no))
    this.loginService.loggedUser().subscribe(user => this.currentUser = user)
  }

  previousPage(): void {
    this.fetchPage(this.page.no - 1)
  }

}
