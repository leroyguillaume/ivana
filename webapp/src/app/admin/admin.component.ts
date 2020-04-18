import {Component, OnInit} from '@angular/core'
import {Page} from '../page'
import {UserService} from '../user.service'
import {StateService} from '../state.service'
import {finalize} from 'rxjs/operators'
import {ActivatedRoute, Router} from '@angular/router'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faPlus, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {User} from '../user'
import {fetchPageFromQueryParam, handleError} from '../util'

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
  plusIcon: IconDefinition = faPlus

  page: Page<User> = null
  loading: boolean = true

  constructor(
    private userService: UserService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
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
        error => handleError(error, this.stateService)
      )
  }

  nextPage(): void {
    this.fetchPage(this.page.no + 1)
  }

  ngOnInit(): void {
    fetchPageFromQueryParam(this.route, (no: number) => this.fetchPage(no))
  }

  previousPage(): void {
    this.fetchPage(this.page.no - 1)
  }

}
