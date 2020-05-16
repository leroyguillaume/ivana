import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {Page} from '../page'
import {StateService} from '../state.service'
import {ActivatedRoute, Router} from '@angular/router'
import {fetchPageFromQueryParam, handleError} from '../util'
import {finalize} from 'rxjs/operators'
import {Album} from '../album'
import {AlbumService} from '../album.service'
import {NgbModal} from '@ng-bootstrap/ng-bootstrap'
import {AlbumModalComponent} from '../album-modal/album-modal.component'
import {forkJoin} from 'rxjs'

export const AlbumPageSize = 12

@Component({
  selector: 'app-album-list',
  templateUrl: './album-list.component.html',
  styleUrls: ['./album-list.component.css']
})
export class AlbumListComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  page: Page<Album>
  loading: boolean = true
  creating: boolean = false

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    private modalService: NgbModal,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  deleteSelectedAlbums(selectedAlbums: Set<string>): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer ces ${selectedAlbums.size} album(s) ?`)) {
      forkJoin(Array.from(selectedAlbums.values()).map(id => this.albumService.delete(id)))
        .subscribe(
          () => {
            this.stateService.sendSuccessEvent('Les albums ont été supprimés !')
            this.fetchPage(this.page.no)
          },
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  fetchPage(no: number): void {
    this.loading = true
    this.albumService.getAll(no, AlbumPageSize)
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

  ngOnInit(): void {
    fetchPageFromQueryParam(this.route, (no: number) => this.fetchPage(no))
  }

  openAlbumModal(): void {
    this.modalService.open(AlbumModalComponent).result.then(
      () => this.fetchPage(this.page.no),
      () => {
      }
    )
  }
}
