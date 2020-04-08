import {Component, OnInit} from '@angular/core'
import {Photo} from '../photo'
import {Page} from '../page'
import {PhotoService} from '../photo.service'
import {finalize, map} from 'rxjs/operators'
import {faArrowLeft, faArrowRight, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {environment} from '../../environments/environment'
import {ActivatedRoute, Router} from '@angular/router'

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
  error = null
  loading = false

  constructor(
    private photoService: PhotoService,
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
