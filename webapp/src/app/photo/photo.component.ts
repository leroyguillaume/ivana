import {Component, HostListener, OnInit} from '@angular/core'
import {NavigablePhoto} from '../navigable-photo'
import {faArrowLeft, faPencilAlt, faPlus, faRedo, faSpinner, faTimes, faTrash, faUndo} from '@fortawesome/free-solid-svg-icons'
import {PhotoService} from '../photo.service'
import {ActivatedRoute, Router} from '@angular/router'
import {finalize} from 'rxjs/operators'
import {environment} from '../../environments/environment'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {PhotoPageSize} from '../home/home.component'
import {handleError} from '../util'
import {RotationDirection} from '../rotation-direction'
import {AlbumSelectionModalComponent} from '../album-selection-modal/album-selection-modal.component'
import {Album} from '../album'
import {NgbModal} from '@ng-bootstrap/ng-bootstrap'
import {AlbumService} from '../album.service'

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
  plusIcon: IconDefinition = faPlus

  baseUrl: string = environment.baseUrl
  loading: boolean = true
  error: string = null

  settingsPanelOpened: boolean = false

  photo: NavigablePhoto
  currentAlbum: Album

  rotationDegrees: number = 0

  constructor(
    private photoService: PhotoService,
    private albumService: AlbumService,
    private stateService: StateService,
    private modalService: NgbModal,
    private route: ActivatedRoute,
    private router: Router,
  ) {
  }

  close(): void {
    const offset = this.stateService.startPhotoNavIndex > -1
      ? Math.floor((this.stateService.startPhotoNavIndex + this.stateService.currentPhotoNavOffset) / PhotoPageSize)
      : 0
    const route = this.currentAlbum ? ['/album', this.currentAlbum.id] : ['/home']
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(route, {
      queryParams: {
        page: (this.stateService.currentPhotosPage?.no || 1) + offset
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
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  fetchPhoto(id: string): void {
    this.loading = true
    this.photoService.get(id)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        photo => this.photo = photo,
        error => handleError(error, this.stateService, this.router)
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
    this.stateService.currentAlbum.subscribe(album => this.currentAlbum = album)
  }

  openAlbumSelectionModal(): void {
    this.modalService.open(AlbumSelectionModalComponent).result.then((album: Album) => {
      this.albumService.update(album.id, album.name, [this.photo.id])
        .subscribe(
          updatedAlbum => this.stateService.success.next(`Photo ajoutée à l'album ${updatedAlbum.name} !`),
          error => handleError(error, this.stateService, this.router)
        )
    })
  }

  previous(): void {
    if (this.photo.previous) {
      this.stateService.currentPhotoNavOffset--
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['photo', this.photo.previous.id])
    }
  }

  rotate(direction: RotationDirection): void {
    this.photoService.rotate(this.photo.id, direction).subscribe(
      () => {
      },
      error => console.error(error)
    )
  }

  rotateClockwise(): void {
    this.rotate(RotationDirection.Clockwise)
    this.rotationDegrees += 90.
  }

  rotateCounterclockwise(): void {
    this.rotate(RotationDirection.Counterclockwise)
    this.rotationDegrees -= 90.
  }

}
