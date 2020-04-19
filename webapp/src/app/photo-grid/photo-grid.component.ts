import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faTrash} from '@fortawesome/free-solid-svg-icons'
import {Page} from '../page'
import {Photo} from '../photo'
import {StateService} from '../state.service'
import {Router} from '@angular/router'
import {environment} from '../../environments/environment'
import {PhotoService} from '../photo.service'

@Component({
  selector: 'app-photo-grid',
  templateUrl: './photo-grid.component.html',
  styleUrls: ['./photo-grid.component.scss']
})
export class PhotoGridComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  trashIcon: IconDefinition = faTrash

  baseUrl: string = environment.baseUrl

  selectedPhotos: Set<string> = new Set()

  @Input()
  page: Page<Photo>

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter()

  @Output()
  selectedPhotosDelete: EventEmitter<Set<string>> = new EventEmitter()

  constructor(
    private photoService: PhotoService,
    private stateService: StateService,
    private router: Router
  ) {
  }

  deleteSelectedPhotos(): void {
    this.selectedPhotosDelete.emit(this.selectedPhotos)
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  ngOnInit(): void {
    this.stateService.selectedPhotos.subscribe(selectedPhotos => this.selectedPhotos = selectedPhotos)
  }

  openPhoto(id: string): void {
    this.stateService.startPhotoNavIndex = this.page.content.findIndex(photo => photo.id === id)
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/photo', id])
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
    this.stateService.selectedPhotos.next(this.selectedPhotos)
  }

}
