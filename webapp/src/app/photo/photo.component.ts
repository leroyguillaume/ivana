import {Component, HostListener, OnInit} from '@angular/core'
import {NavigablePhoto} from '../navigable-photo'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {PhotoService} from '../photo.service'
import {ActivatedRoute, Router} from '@angular/router'
import {finalize, flatMap} from 'rxjs/operators'
import {environment} from '../../environments/environment'
import {RoutingService} from '../routing.service'

@Component({
  selector: 'app-photo',
  templateUrl: './photo.component.html',
  styleUrls: ['./photo.component.scss']
})
export class PhotoComponent implements OnInit {
  spinnerIcon = faSpinner
  leftIcon = faArrowLeft

  baseUrl = environment.baseUrl
  loading = true
  error = null

  constructor(
    private photoService: PhotoService,
    private routingService: RoutingService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
  }

  photo: NavigablePhoto

  @HostListener('window:keyup', ['$event'])
  keyEvent(event: KeyboardEvent) {
    switch (event.key) {
      case 'ArrowLeft':
        this.previous()
        break
      case 'ArrowRight':
        this.next()
        break
      case 'Escape':
        this.close()
        break
    }
  }

  close() {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigateByUrl(this.routingService.previousUrl)
  }

  private next() {
    if (this.photo.next) {
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['photo', this.photo.next.id])
    }
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(flatMap(params => this.photoService.get(params.get('id')).pipe(finalize(() => this.loading = false))))
      .subscribe(
        photo => this.photo = photo,
        error => {
          console.error(error)
          this.error = 'Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.'
        }
      )
  }

  private previous() {
    if (this.photo.previous) {
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['photo', this.photo.previous.id])
    }
  }

}
