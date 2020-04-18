import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Photo} from '../photo'
import {Page} from '../page'
import {PhotoService} from '../photo.service'
import {finalize} from 'rxjs/operators'
import {faArrowLeft, faArrowRight, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {environment} from '../../environments/environment'
import {ActivatedRoute, Router} from '@angular/router'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {fetchPageFromQueryParam, handleError} from '../util'

export const PhotoPageSize: number = 12

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  baseUrl: string = environment.baseUrl

  spinnerIcon: IconDefinition = faSpinner
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight

  page: Page<Photo> = null
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

  fetchPage(no: number): void {
    this.loading = true
    this.photoService.getAll(no, PhotoPageSize)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => {
          this.page = page
          this.stateService.currentHomePage = page
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
    this.stateService.uploadingPhotos.subscribe(uploading => this.uploading = uploading)
    this.stateService.photosUploaded.subscribe(() => this.fetchPage(this.page.no))
  }

  openPhoto(id: string): void {
    this.stateService.startPhotoNavIndex = this.page.content.findIndex(photo => photo.id === id)
    this.router.navigate(['/photo', id])
  }

  previousPage(): void {
    this.fetchPage(this.page.no - 1)
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
