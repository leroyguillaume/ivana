import {Component, OnInit} from '@angular/core'
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner, faTimes} from '@fortawesome/free-solid-svg-icons'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {AlbumService} from '../album.service'
import {StateService} from '../state.service'
import {Router} from '@angular/router'

@Component({
  selector: 'app-album-modal',
  templateUrl: './album-modal.component.html',
  styleUrls: ['./album-modal.component.scss']
})
export class AlbumModalComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  closeIcon: IconDefinition = faTimes

  loading: boolean = false

  albumForm: FormGroup = new FormGroup({
    name: new FormControl('')
  })

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    public activeModal: NgbActiveModal,
    private router: Router
  ) {
  }

  get name(): AbstractControl {
    return this.albumForm.get('name')
  }

  ngOnInit(): void {
  }

  submit(): void {
    this.loading = true
    this.albumService.create(this.name.value)
      .pipe(finalize(() => {
        this.loading = false
        this.activeModal.dismiss()
      }))
      .subscribe(
        () => this.stateService.success.next('Album créé !'),
        error => handleError(error, this.stateService, this.router)
      )
  }

}
