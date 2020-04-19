import {Component, HostListener, OnInit} from '@angular/core'
import {NavigablePhoto} from '../navigable-photo'
import {faArrowLeft, faPencilAlt, faRedo, faSpinner, faTimes, faTrash, faUndo} from '@fortawesome/free-solid-svg-icons'
import {PhotoService} from '../photo.service'
import {ActivatedRoute, Router} from '@angular/router'
import {finalize} from 'rxjs/operators'
import {environment} from '../../environments/environment'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {PhotoPageSize} from '../home/home.component'
import {handleError} from '../util'
import {RotationDirection} from '../rotation-direction'

@Component({
  selector: 'app-photo',
  templateUrl: './photo.component.html',
  styleUrls: ['./photo.component.scss']
})
export class PhotoComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  arrowLeftIcon: IconDefinition = faArrowLeft
  settingsIcon: IconDefinition = faPencilAlt
  closeIcon: IconDefinition = faTimes
  rotateClockwiseIcon: IconDefinition = faRedo
  rotateCounterclockwiseIcon: IconDefinition = faUndo
  trashIcon: IconDefinition = faTrash

  baseUrl: string = environment.baseUrl
  loading: boolean = true
  transforming: boolean = false
  error: string = null

  settingsPanelOpened: boolean = false

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

  delete(): void {
    if (window.confirm('Êtes-vous certain(e) de vouloir supprimer cette photo ?')) {
      this.photoService.delete(this.photo.id)
        .subscribe(
          () => {
            if (this.photo.next) {
              this.next()
            } else if (this.photo.previous) {
              this.previous()
            } else {
              this.close()
            }
          },
          error => handleError(error, this.stateService)
        )
    }
  }

  fetchPhoto(id: string): void {
    this.loading = true
    this.photoService.get(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        photo => this.photo = photo,
        error => handleError(error, this.stateService)
      )
  }

  @HostListener('window:keyup', ['$event'])
  keyUpEvent(event: KeyboardEvent): void {
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
    this.route.paramMap.subscribe(params => this.fetchPhoto(params.get('id')))
  }

  previous(): void {
    if (this.photo.previous) {
      this.stateService.currentPhotoNavOffset--
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['photo', this.photo.previous.id])
    }
  }

  rotate(dir: RotationDirection): void {
    this.transforming = true
    this.photoService.rotate(this.photo.id, dir)
      .pipe(finalize(() => this.transforming = false))
      .subscribe(
        () => this.fetchPhoto(this.photo.id),
        error => handleError(error, this.stateService)
      )
  }

  rotateClockwise(): void {
    this.rotate(RotationDirection.Clockwise)
  }

  rotateCounterclockwise(): void {
    this.rotate(RotationDirection.Counterclockwise)
  }

}
