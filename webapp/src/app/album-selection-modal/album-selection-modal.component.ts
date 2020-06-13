import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner, faTimes} from '@fortawesome/free-solid-svg-icons'
import {AlbumService} from '../album.service'
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap'
import {debounceTime, distinctUntilChanged, flatMap} from 'rxjs/operators'
import {Album} from '../album'
import {Observable, of} from 'rxjs'
import {NgbTypeaheadSelectItemEvent} from '@ng-bootstrap/ng-bootstrap/typeahead/typeahead'
import {Permission} from '../permission'

@Component({
  selector: 'app-album-selection-modal',
  templateUrl: './album-selection-modal.component.html',
  styleUrls: ['./album-selection-modal.component.css']
})
export class AlbumSelectionModalComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  closeIcon: IconDefinition = faTimes

  selectedAlbum: Album

  suggest = (obs: Observable<string>) => obs.pipe(
    debounceTime(200),
    distinctUntilChanged(),
    flatMap(q => {
      if (q.trim() === '') {
        return of([])
      } else {
        return this.albumService.suggest(q, Permission.Update)
      }
    })
  )

  format = (album: Album) => album.name

  constructor(
    private albumService: AlbumService,
    public activeModal: NgbActiveModal
  ) {
  }

  ngOnInit(): void {
  }

  selectAlbum(event: NgbTypeaheadSelectItemEvent): void {
    this.selectedAlbum = event.item
  }

  submit(): void {
    this.activeModal.close(this.selectedAlbum)
  }
}
