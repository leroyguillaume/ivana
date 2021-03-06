import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faCog, faPencilAlt, faPlus, faTrash} from '@fortawesome/free-solid-svg-icons'
import {Page} from '../page'
import {Photo} from '../photo'
import {StateService} from '../state.service'
import {Router} from '@angular/router'
import {environment} from '../../environments/environment'
import {PhotoService} from '../photo.service'
import {NgbModal} from '@ng-bootstrap/ng-bootstrap'
import {AlbumSelectionModalComponent} from '../album-selection-modal/album-selection-modal.component'
import {AlbumService} from '../album.service'
import {Album} from '../album'
import {handleError} from '../util'
import {Permission} from '../permission'

@Component({
  selector: 'app-photo-grid',
  templateUrl: './photo-grid.component.html',
  styleUrls: ['./photo-grid.component.scss']
})
export class PhotoGridComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  trashIcon: IconDefinition = faTrash
  plusIcon: IconDefinition = faPlus
  editIcon: IconDefinition = faPencilAlt
  cogIcon: IconDefinition = faCog

  baseUrl: string = environment.baseUrl

  selectedPhotos: Set<string> = new Set()

  currentAlbum: Album

  @Input()
  page: Page<Photo>

  @Input()
  sharedView: boolean

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter()

  @Output()
  selectedPhotosDelete: EventEmitter<Set<string>> = new EventEmitter()

  @Output()
  albumDelete: EventEmitter<Album> = new EventEmitter()

  constructor(
    private photoService: PhotoService,
    private albumService: AlbumService,
    private stateService: StateService,
    private modalService: NgbModal,
    private router: Router
  ) {
  }

  get albumDeleteAllowed(): boolean {
    return this.currentAlbum?.permissions.indexOf(Permission.Delete) > -1
  }

  get albumUpdateAllowed(): boolean {
    return this.currentAlbum?.permissions.indexOf(Permission.Update) > -1
      || this.currentAlbum?.permissions.indexOf(Permission.UpdatePermissions) > -1
  }

  emitAlbumDelete(): void {
    this.albumDelete.emit(this.currentAlbum)
  }

  emitSelectedPhotosDelete(): void {
    this.selectedPhotosDelete.emit(this.selectedPhotos)
  }

  navigateToAlbumUpdatePage(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/album', this.currentAlbum.id, 'edit'])
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  ngOnInit(): void {
    this.stateService.currentAlbum.subscribe(album => this.currentAlbum = album)
  }

  openAlbumSelectionModal(): void {
    this.modalService.open(AlbumSelectionModalComponent).result.then((album: Album) => {
      this.albumService.update(album.id, album.name, Array.from(this.selectedPhotos))
        .subscribe(
          updatedAlbum => this.stateService.sendSuccessEvent(`Photos ajoutées à l'album ${updatedAlbum.name} !`),
          error => handleError(error, this.stateService, this.router)
        )
    })
  }

  openPhoto(id: string): void {
    this.stateService.startPhotoNavIndex = this.page.content.findIndex(photo => photo.id === id)
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(
      ['/photo', id],
      {
        queryParams: {
          album: this.currentAlbum?.id
        }
      }
    )
  }

  photoIsSelected(photo: Photo): boolean {
    return this.selectedPhotos.has(photo.id)
  }

  previousPage(): void {
    this.pageChange.emit(this.page.no - 1)
  }

  togglePhotoSelection(photo: Photo): void {
    if (this.photoIsSelected(photo)) {
      this.selectedPhotos.delete(photo.id)
    } else {
      this.selectedPhotos.add(photo.id)
    }
  }

}
