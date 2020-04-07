import {Component, HostListener, OnInit} from '@angular/core'
import {NavigablePhoto} from '../navigable-photo'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {PhotoService} from '../photo.service'
import {ActivatedRoute, Router} from '@angular/router'
import {finalize, flatMap} from 'rxjs/operators'
import {environment} from '../../environments/environment'

@Component({
  selector: 'app-photo',
  templateUrl: './photo.component.html',
  styleUrls: ['./photo.component.css']
})
export class PhotoComponent implements OnInit {
  spinnerIcon = faSpinner

  baseUrl = environment.baseUrl
  loading = true
  error = null

  photo: NavigablePhoto

  constructor(
    private photoService: PhotoService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

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

  private close() {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['home'])
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
