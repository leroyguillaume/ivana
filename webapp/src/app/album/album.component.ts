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
  uploading: boolean = false

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  deleteSelectedPhotos(selectedPhotos: Set<string>): void {

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
        error => handleError(error, this.stateService)
      )
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(map(params => params.get('id')), flatMap(id => this.albumService.get(id)))
      .subscribe(album => {
        this.album = album
        fetchPageFromQueryParam(this.route, (no: number) => this.fetchPage(no))
      })
    this.stateService.uploadingPhotos.subscribe(uploading => this.uploading = uploading)
    this.stateService.photosUploaded.subscribe(() => this.fetchPage(this.page.no))
  }

  selectFiles(): void {
    this.filesInput.nativeElement.click()
  }

  upload(): void {
    const files = this.filesInput.nativeElement.files
    if (files.length > 0) {
      this.stateService.uploadPhotos(files)
    }
  }
}
