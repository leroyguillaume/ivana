import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {Page} from '../page'
import {Photo} from '../photo'
import {StateService} from '../state.service'
import {ActivatedRoute, Router} from '@angular/router'
import {fetchPageFromQueryParam, handleError} from '../util'
import {finalize, flatMap, map} from 'rxjs/operators'
import {PhotoPageSize} from '../home/home.component'
import {AlbumService} from '../album.service'
import {Album} from '../album'

@Component({
  selector: 'app-album',
  templateUrl: './album.component.html',
  styleUrls: ['./album.component.css']
})
export class AlbumComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  album: Album
  page: Page<Photo>
  loading: boolean = true

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  deleteAlbum(): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer cet album ?`)) {
      this.albumService.delete(this.album.id).subscribe(
        () => {
          this.stateService.success.next('L\'album a été supprimé !')
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate(['/album'])
        },
        error => handleError(error, this.stateService, this.router)
      )
    }
  }

  fetchPage(no: number): void {
    this.loading = true
    this.albumService.getAllPhotos(this.album.id, no, PhotoPageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.page = page
          this.stateService.currentPhotosPage = page
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
    this.route.paramMap
      .pipe(map(params => params.get('id')), flatMap(id => this.albumService.get(id)))
      .subscribe(album => {
        this.album = album
        this.stateService.currentAlbum.next(album)
        fetchPageFromQueryParam(this.route, (no: number) => this.fetchPage(no))
      })
  }

  removeSelectedPhotosFromAlbum(selectedPhotos: Set<string>): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer ces ${selectedPhotos.size} photo(s) de cet album ?`)) {
      this.albumService.update(this.album.id, this.album.name, [], Array.from(selectedPhotos)).subscribe(
        () => {
          this.stateService.success.next('Les photos ont été supprimées de cet album !')
          this.fetchPage(this.page.no)
        },
        error => handleError(error, this.stateService, this.router)
      )
    }
  }
}
