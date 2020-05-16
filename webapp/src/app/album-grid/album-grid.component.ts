import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faCog, faImages, faPlus, faTrash} from '@fortawesome/free-solid-svg-icons'
import {environment} from '../../environments/environment'
import {Page} from '../page'
import {StateService} from '../state.service'
import {Router} from '@angular/router'
import {Album} from '../album'
import {AlbumService} from '../album.service'

@Component({
  selector: 'app-album-grid',
  templateUrl: './album-grid.component.html',
  styleUrls: ['./album-grid.component.css']
})
export class AlbumGridComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  trashIcon: IconDefinition = faTrash
  albumIcon: IconDefinition = faImages
  plusIcon: IconDefinition = faPlus
  cogIcon: IconDefinition = faCog

  baseUrl: string = environment.baseUrl

  selectedAlbums: Set<string> = new Set()

  @Input()
  page: Page<Album>

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter()

  @Output()
  selectedAlbumsDelete: EventEmitter<Set<string>> = new EventEmitter()

  @Output()
  albumCreate: EventEmitter<void> = new EventEmitter()

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    private router: Router
  ) {
  }

  albumIsSelected(album: Album): boolean {
    return this.selectedAlbums.has(album.id)
  }

  deleteSelectedAlbums(): void {
    this.selectedAlbumsDelete.emit(this.selectedAlbums)
  }

  emitAlbumCreate(): void {
    this.albumCreate.emit()
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  ngOnInit(): void {
    this.stateService.selectedAlbums.subscribe(selectedAlbums => this.selectedAlbums = selectedAlbums)
  }

  openAlbum(id: string): void {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/album', id])
  }

  previousPage(): void {
    this.pageChange.emit(this.page.no - 1)
  }

  toggleAlbumSelection(album: Album): void {
    if (this.albumIsSelected(album)) {
      this.selectedAlbums.delete(album.id)
    } else {
      this.selectedAlbums.add(album.id)
    }
  }

}
