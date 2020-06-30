import {Component, HostListener, OnDestroy, OnInit} from '@angular/core'
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
import {AlbumSelectionModalComponent} from '../album-selection-modal/album-selection-modal.component'
import {Album} from '../album'
import {NgbModal} from '@ng-bootstrap/ng-bootstrap'
import {AlbumService} from '../album.service'
import {Permission} from '../permission'
import {Person} from '../person'
import {PersonSelectionModalComponent} from '../person-selection-modal/person-selection-modal.component'

@Component({
  selector: 'app-photo',
  templateUrl: './photo.component.html',
  styleUrls: ['./photo.component.scss']
})
export class PhotoComponent implements OnDestroy, OnInit {
  spinnerIcon: IconDefinition = faSpinner
  arrowLeftIcon: IconDefinition = faArrowLeft
  editIcon: IconDefinition = faPencilAlt
  closeIcon: IconDefinition = faTimes
  rotateClockwiseIcon: IconDefinition = faRedo
  rotateCounterclockwiseIcon: IconDefinition = faUndo
  trashIcon: IconDefinition = faTrash
  plusIcon: IconDefinition = faPlus

  baseUrl: string = environment.baseUrl
  loading: boolean = true
  loadingPeople: boolean = true

  settingsPanelOpened: boolean = false

  photo: NavigablePhoto
  people: Person[]
  currentAlbumId: string

  rotationDegrees: number = 0
  rotationDegreesOffset: number = 0
  rotationTimeoutId: number

  constructor(
    private photoService: PhotoService,
    private albumService: AlbumService,
    private stateService: StateService,
    private modalService: NgbModal,
    private route: ActivatedRoute,
    private router: Router,
  ) {
  }

  get deleteAllowed(): boolean {
    return this.photo.permissions.indexOf(Permission.Delete) > -1
  }

  get updateAllowed(): boolean {
    return this.photo.permissions.indexOf(Permission.Update) > -1
  }

  close(): void {
    const offset = this.stateService.startPhotoNavIndex > -1
      ? Math.floor((this.stateService.startPhotoNavIndex + this.stateService.currentPhotoNavOffset) / PhotoPageSize)
      : 0
    const route = this.currentAlbumId ? ['/album', this.currentAlbumId] : ['/home']
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

  fetchPeople(id: string): void {
    this.loadingPeople = true
    this.photoService.getPeople(id)
      .pipe(finalize(() => this.loadingPeople = false))
      .subscribe(
        people => this.people = people,
        error => handleError(error, this.stateService, this.router)
      )
  }

  fetchPhoto(id: string, albumId: string): void {
    this.loading = true
    this.photoService.get(id, albumId)
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
      this.router.navigate(
        ['photo', this.photo.next.id],
        {
          queryParams: {
            album: this.currentAlbumId
          }
        }
      )
    }
  }

  ngOnDestroy(): void {
    if (this.rotationTimeoutId) {
      clearTimeout(this.rotationTimeoutId)
      this.rotate()
    }
  }

  ngOnInit(): void {
    this.stateService.currentPhotoNavOffset = 0
    this.route.queryParamMap.subscribe(queryParams => {
      this.currentAlbumId = queryParams.get('album')
      this.route.paramMap.subscribe(params => {
        const id = params.get('id')
        this.fetchPhoto(id, this.currentAlbumId)
        this.fetchPeople(id)
      })
    })
  }

  openAlbumSelectionModal(): void {
    this.modalService.open(AlbumSelectionModalComponent).result.then((album: Album) => {
      this.albumService.update(album.id, album.name, [this.photo.id])
        .subscribe(
          updatedAlbum => this.stateService.sendSuccessEvent(`Photo ajoutée à l'album ${updatedAlbum.name} !`),
          error => handleError(error, this.stateService, this.router)
        )
    })
  }

  openPersonSelectionModal(): void {
    this.modalService.open(PersonSelectionModalComponent).result.then((person: Person) => {
      this.loadingPeople = true
      this.photoService.updatePeople(this.photo.id, [person.id])
        .pipe(finalize(() => this.loadingPeople = false))
        .subscribe(
          () => this.fetchPeople(this.photo.id),
          error => handleError(error, this.stateService, this.router)
        )
    })
  }

  previous(): void {
    if (this.photo.previous) {
      this.stateService.currentPhotoNavOffset--
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(
        ['photo', this.photo.previous.id],
        {
          queryParams: {
            album: this.currentAlbumId
          }
        }
      )
    }
  }

  removePerson(person: Person): void {
    if (window.confirm(`Êtes-vous certain(e) de vouloir supprimer ${person.firstName} ${person.lastName} de cette photo ?`)) {
      this.loadingPeople = true
      this.photoService.updatePeople(this.photo.id, [], [person.id])
        .pipe(finalize(() => this.loadingPeople = false))
        .subscribe(
          () => this.fetchPeople(this.photo.id),
          error => handleError(error, this.stateService, this.router)
        )
    }
  }

  rotate(): void {
    this.photoService.rotate(this.photo.id, this.rotationDegreesOffset).subscribe(
      () => this.rotationDegreesOffset = 0,
      error => console.error(error)
    )
  }

  rotateTimeout(): void {
    if (this.rotationTimeoutId) {
      clearTimeout(this.rotationTimeoutId)
    }
    this.rotationTimeoutId = setTimeout(() => {
      this.rotate()
      this.rotationTimeoutId = null
    }, 1000)
  }

  rotateClockwise(): void {
    this.rotationDegrees += 90.
    this.rotationDegreesOffset += 90.
    this.rotateTimeout()
  }

  rotateCounterclockwise(): void {
    this.rotationDegrees -= 90.
    this.rotationDegreesOffset -= 90.
    this.rotateTimeout()
  }

}
