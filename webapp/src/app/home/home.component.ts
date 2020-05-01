import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Photo} from '../photo'
import {Page} from '../page'
import {PhotoService} from '../photo.service'
import {finalize} from 'rxjs/operators'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {ActivatedRoute, Router} from '@angular/router'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {fetchPageFromQueryParam, handleError} from '../util'
import {forkJoin} from 'rxjs'

export const PhotoPageSize: number = 12

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  page: Page<Photo>
  loading: boolean = true
  uploading: boolean = false

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private photoService: PhotoService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  deleteSelectedPhotos(selectedPhotos: Set<string>): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer ces ${selectedPhotos.size} photo(s) ?`)) {
      forkJoin(Array.from(selectedPhotos.values()).map(id => this.photoService.delete(id)))
        .subscribe(
          () => {
            this.stateService.sendSuccessEvent('Les photos ont été supprimées !')
            this.fetchPage(this.page.no)
          },
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  fetchPage(no: number): void {
    this.loading = true
    this.photoService.getAll(no, PhotoPageSize)
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
    fetchPageFromQueryParam(this.route, (no: number) => this.fetchPage(no))
    this.stateService.uploadingPhotos.subscribe(uploading => this.uploading = uploading)
    this.stateService.photosUploaded.subscribe(() => this.fetchPage(this.page.no))
    this.stateService.currentAlbum.next(null)
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
