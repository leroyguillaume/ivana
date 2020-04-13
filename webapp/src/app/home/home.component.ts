import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Photo} from '../photo'
import {Page} from '../page'
import {PhotoService} from '../photo.service'
import {finalize, map} from 'rxjs/operators'
import {faArrowLeft, faArrowRight, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {environment} from '../../environments/environment'
import {ActivatedRoute, Router} from '@angular/router'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'

export const PageSize: number = 12

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
  success: string = null
  error: string = null
  loading: boolean = false
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
    this.photoService.getAll(no, PageSize)
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
        error => {
          console.error(error)
          this.error = 'Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.'
        }
      )
  }

  nextPage(): void {
    this.fetchPage(this.page.no + 1)
  }

  ngOnInit(): void {
    this.route.queryParamMap
      .pipe(map(params => params.get('page')))
      .subscribe(page => {
        const no = Number(page)
        if (isNaN(no) || no < 1) {
          this.fetchPage(1)
        } else {
          this.fetchPage(no)
        }
      })
    this.stateService.uploadingPhotos.subscribe(uploading => this.uploading = uploading)
    this.stateService.error.subscribe(error => this.error = error)
    this.stateService.success.subscribe(success => this.success = success)
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
