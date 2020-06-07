import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {ActivatedRoute, Router} from '@angular/router'
import {Photo} from '../photo'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {PhotoService} from '../photo.service'
import {StateService} from '../state.service'
import {SubjectPermissions} from '../subject-permissions'
import {Page} from '../page'
import {SubjectPermissionsUpdateEvent} from '../subject-permissions-update-event'
import {Permission} from '../permission'
import {NgbNavChangeEvent} from '@ng-bootstrap/ng-bootstrap/nav/nav'

export const PhotoPermissionsPageSize = 10

enum Tab {
  Info = 'info',
  Permissions = 'perms'
}

@Component({
  selector: 'app-photo-edition',
  templateUrl: './photo-update.component.html',
  styleUrls: ['./photo-update.component.scss']
})
export class PhotoUpdateComponent implements OnInit {
  arrowLeftIcon: IconDefinition = faArrowLeft
  spinnerIcon: IconDefinition = faSpinner

  loading: boolean = true
  updating: boolean = false

  photo: Photo
  permsPage: Page<SubjectPermissions>

  Tab: typeof Tab = Tab
  currentTab: Tab

  constructor(
    private photoService: PhotoService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  get updateAllowed(): boolean {
    return this.photo.permissions.indexOf(Permission.Update) > -1
  }

  get updatePermissionsAllowed(): boolean {
    return this.photo.permissions.indexOf(Permission.UpdatePermissions) > -1
  }

  back(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/photo', this.photo.id])
  }

  fetchPermissionsPage(no: number): void {
    this.loading = true
    this.photoService.getPermissions(this.photo.id, no, PhotoPermissionsPageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.permsPage = page
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

  fetchPhoto(id: string): void {
    this.loading = true
    this.photoService.get(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        photo => {
          this.photo = photo
          if (this.updateAllowed) {
            this.currentTab = Tab.Info
          } else {
            this.currentTab = Tab.Permissions
            this.fetchPermissionsPage(1)
          }
        },
        error => handleError(error, this.stateService, this.router)
      )
  }

  loadTab(event: NgbNavChangeEvent): void {
    this.currentTab = event.nextId
    switch (event.nextId) {
      case Tab.Permissions:
        if (!this.permsPage) {
          this.fetchPermissionsPage(1)
        }
        break
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => this.fetchPhoto(params.get('id')))
  }

  updatePermissions(event: SubjectPermissionsUpdateEvent): void {
    this.updating = true
    this.photoService.updatePermissions(this.photo.id, event.subjsPermsToAdd, event.subjsPermsToRemove)
      .pipe(finalize(() => this.updating = false))
      .subscribe(
        () => {
          this.stateService.sendSuccessEvent('Permissions mises à jour !')
          this.fetchPermissionsPage(this.permsPage.no)
        },
        error => handleError(error, this.stateService, this.router)
      )
  }
}
