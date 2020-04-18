import {Component, HostListener, OnInit} from '@angular/core'
import {NavigablePhoto} from '../navigable-photo'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {PhotoService} from '../photo.service'
import {ActivatedRoute, Router} from '@angular/router'
import {finalize, flatMap} from 'rxjs/operators'
import {environment} from '../../environments/environment'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {PhotoPageSize} from '../home/home.component'
import {handleError} from '../util'

@Component({
  selector: 'app-photo',
  templateUrl: './photo.component.html',
  styleUrls: ['./photo.component.scss']
})
export class PhotoComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  arrowLeftIcon: IconDefinition = faArrowLeft

  baseUrl: string = environment.baseUrl
  loading: boolean = true
  error: string = null

  photo: NavigablePhoto

  constructor(
    private photoService: PhotoService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
  }

  close(): void {
    const offset = this.stateService.startPhotoNavIndex > -1
      ? Math.floor((this.stateService.startPhotoNavIndex + this.stateService.currentPhotoNavOffset) / PhotoPageSize)
      : 0
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['home'], {
      queryParams: {
        page: (this.stateService.currentHomePage?.no || 1) + offset
      }
    })
  }

  @HostListener('window:keyup', ['$event'])
  keyEvent(event: KeyboardEvent): void {
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

  next(): void {
    if (this.photo.next) {
      this.stateService.currentPhotoNavOffset++
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['photo', this.photo.next.id])
    }
  }

  ngOnInit(): void {
    this.stateService.currentPhotoNavOffset = 0
    this.route.paramMap
      .pipe(flatMap(params => this.photoService.get(params.get('id')).pipe(finalize(() => this.loading = false))))
      .subscribe(
        photo => this.photo = photo,
        error => handleError(error, this.stateService)
      )
  }

  previous(): void {
    if (this.photo.previous) {
      this.stateService.currentPhotoNavOffset--
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['photo', this.photo.previous.id])
    }
  }

}
