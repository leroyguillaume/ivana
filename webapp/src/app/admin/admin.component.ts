import {Component, OnInit} from '@angular/core'
import {Page} from '../page'
import {UserService} from '../user.service'
import {StateService} from '../state.service'
import {finalize} from 'rxjs/operators'
import {ActivatedRoute, Router} from '@angular/router'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {User} from '../user'
import {handleError} from '../util'
import {NgbNavChangeEvent} from '@ng-bootstrap/ng-bootstrap/nav/nav'
import {Person} from '../person'
import {PersonService} from '../person.service'

export const UsersPageSize: number = 10
export const PeoplePageSize: number = 10

enum Tab {
  Users = 'users',
  People = 'people'
}

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  usersPage: Page<User> = null
  peoplePage: Page<Person> = null
  loading: boolean = true

  Tab: typeof Tab = Tab
  currentTab: Tab

  constructor(
    private userService: UserService,
    private personService: PersonService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
  }

  deletePerson(person: Person): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer la personne '${person.firstName} ${person.lastName}' ?`)) {
      this.loading = true
      this.personService.delete(person.id)
        .pipe(finalize(() => this.loading = false))
        .subscribe(
          () => {
            this.fetchPeoplePage(this.peoplePage.no)
            this.stateService.sendSuccessEvent(`La personne '${person.firstName} ${person.lastName}' a été supprimée !`)
          },
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  deleteUser(user: User): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer l'utilisateur '${user.name}' ?`)) {
      this.loading = true
      this.userService.delete(user.id)
        .pipe(finalize(() => this.loading = false))
        .subscribe(
          () => {
            this.fetchUsersPage(this.usersPage.no)
            this.stateService.sendSuccessEvent(`L'utilisateur '${user.name}' a été supprimé !`)
          },
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  fetchPeoplePage(no: number): void {
    this.loading = true
    this.personService.getAll(no, PeoplePageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.peoplePage = page
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
              page: page.no,
              tab: Tab.People
            }
          })
        },
        error => handleError(error, this.stateService, this.router)
      )
  }

  fetchUsersPage(no: number): void {
    this.loading = true
    this.userService.getAll(no, UsersPageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.usersPage = page
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
              page: page.no,
              tab: Tab.Users
            }
          })
        },
        error => handleError(error, this.stateService, this.router)
      )
  }

  loadTab(event: NgbNavChangeEvent): void {
    this.currentTab = event.nextId
    switch (event.nextId) {
      case Tab.People:
        if (!this.peoplePage) {
          this.fetchPeoplePage(1)
        }
        break
      case Tab.Users:
        if (!this.usersPage) {
          this.fetchUsersPage(1)
        }
        break
    }
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      switch (params.get('tab')) {
        case Tab.People:
          this.currentTab = Tab.People
          this.fetchPeoplePage(1)
          break
        case Tab.Users:
        default:
          this.currentTab = Tab.Users
          this.fetchUsersPage(1)
          break
      }
    })
  }

}
