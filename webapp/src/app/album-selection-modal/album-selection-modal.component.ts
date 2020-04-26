import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner, faTimes} from '@fortawesome/free-solid-svg-icons'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {AlbumService} from '../album.service'
import {StateService} from '../state.service'
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {Album} from '../album'
import {Router} from '@angular/router'

@Component({
  selector: 'app-album-selection-modal',
  templateUrl: './album-selection-modal.component.html',
  styleUrls: ['./album-selection-modal.component.css']
})
export class AlbumSelectionModalComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  closeIcon: IconDefinition = faTimes

  loading: boolean = true

  albums: Album[]
  albumForm: FormGroup = new FormGroup({
    albumId: new FormControl('')
  })

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    public activeModal: NgbActiveModal,
    private router: Router
  ) {
  }

  get albumId(): AbstractControl {
    return this.albumForm.get('albumId')
  }

  ngOnInit(): void {
    this.loading = true
    this.albumService.getAll(1, 100)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => this.albums = page.content,
        error => handleError(error, this.stateService, this.router)
      )
  }

  submit(): void {
    this.activeModal.close(this.albums.find(album => album.id === this.albumId.value))
  }
}
