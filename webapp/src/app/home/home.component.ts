import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Photo} from '../photo'
import {Page} from '../page'
import {PhotoService} from '../photo.service'
import {finalize, map} from 'rxjs/operators'
import {faArrowLeft, faArrowRight, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {environment} from '../../environments/environment'
import {ActivatedRoute, Router} from '@angular/router'
import {UploaderService} from '../uploader.service'

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  readonly size = 12

  baseUrl = environment.baseUrl

  spinnerIcon = faSpinner
  previousIcon = faArrowLeft
  nextIcon = faArrowRight

  page = new Page<Photo>([], 0, 0, 0)
  success = null
  error = null
  loading = false
  uploading = false

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private photoService: PhotoService,
    private uploaderService: UploaderService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  nextPage(): void {
    this.fetchPage(this.page.no + 1)
  }

  previousPage(): void {
    this.fetchPage(this.page.no - 1)
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
    this.uploaderService.uploading.subscribe(uploading => this.uploading = uploading)
    this.uploaderService.error.subscribe(error => this.error = error)
    this.uploaderService.success.subscribe(success => this.success = success)
    this.uploaderService.filesUploaded.subscribe(() => this.fetchPage(this.page.no))
  }

  selectFiles() {
    this.filesInput.nativeElement.click()
  }

  upload() {
    const files = this.filesInput.nativeElement.files
    if (files.length > 0) {
      this.uploaderService.upload(files)
    }
  }

  private fetchPage(no: number) {
    this.loading = true
    this.photoService.getAll(no, this.size)
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
        error => {
          console.error(error)
          this.error = 'Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.'
        }
      )
  }

}
