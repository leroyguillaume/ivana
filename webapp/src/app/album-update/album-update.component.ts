import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {ActivatedRoute, Router} from '@angular/router'
import {Album} from '../album'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {AlbumService} from '../album.service'
import {StateService} from '../state.service'
import {SubjectPermissions} from '../subject-permissions'
import {Page} from '../page'
import {SubjectPermissionsUpdateEvent} from '../subject-permissions-update-event'
import {Permission} from '../permission'
import {NgbNavChangeEvent} from '@ng-bootstrap/ng-bootstrap/nav/nav'

export const AlbumPermissionsPageSize = 10

enum Tab {
  Info = 'info',
  Permissions = 'perms'
}

@Component({
  selector: 'app-album-edition',
  templateUrl: './album-update.component.html',
  styleUrls: ['./album-update.component.scss']
})
export class AlbumUpdateComponent implements OnInit {
  arrowLeftIcon: IconDefinition = faArrowLeft
  spinnerIcon: IconDefinition = faSpinner

  loading: boolean = true
  updating: boolean = false

  album: Album
  permsPage: Page<SubjectPermissions>

  Tab: typeof Tab = Tab
  currentTab: Tab

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  get updateAllowed(): boolean {
    return this.album.permissions.indexOf(Permission.Update) > -1
  }

  get updatePermissionsAllowed(): boolean {
    return this.album.permissions.indexOf(Permission.UpdatePermissions) > -1
  }

  back(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/album', this.album.id])
  }

  fetchPermissionsPage(no: number): void {
    this.loading = true
    this.albumService.getPermissions(this.album.id, no, AlbumPermissionsPageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.permsPage = page
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
              page: page.no,
              tab: Tab.Permissions
            }
          })
        },
        error => handleError(error, this.stateService, this.router)
      )
  }

  fetchAlbum(id: string): void {
    this.loading = true
    this.albumService.get(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        album => {
          this.album = album
          this.route.queryParamMap.subscribe(params => {
            switch (params.get('tab')) {
              case Tab.Permissions:
                this.currentTab = Tab.Permissions
                this.fetchPermissionsPage(1)
                break
              case Tab.Info:
              default:
                this.currentTab = Tab.Info
                break
            }
          })
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
      case Tab.Info:
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: {
            page: this.permsPage?.no,
            tab: Tab.Info
          }
        })
        break
    }
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => this.fetchAlbum(params.get('id')))
  }

  updatePermissions(event: SubjectPermissionsUpdateEvent): void {
    this.updating = true
    this.albumService.updatePermissions(this.album.id, event.subjsPermsToAdd, event.subjsPermsToRemove)
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
