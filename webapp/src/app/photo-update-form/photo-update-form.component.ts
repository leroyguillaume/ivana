import {Component, Input, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {StateService} from '../state.service'
import {Router} from '@angular/router'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {Photo} from '../photo'
import {PhotoService} from '../photo.service'

@Component({
  selector: 'app-photo-update-form',
  templateUrl: './photo-update-form.component.html',
  styleUrls: ['./photo-update-form.component.css']
})
export class PhotoUpdateFormComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  updating: boolean = false

  photoForm: FormGroup

  @Input()
  photo: Photo

  constructor(
    private photoService: PhotoService,
    private stateService: StateService,
    private router: Router
  ) {
  }

  get shootingDate(): AbstractControl {
    return this.photoForm.get('shootingDate')
  }

  update(): void {
    this.updating = true
    this.photoService.update(this.photo.id, this.shootingDate.value)
      .pipe(finalize(() => this.updating = false))
      .subscribe(
        () => this.stateService.sendSuccessEvent('Photo mise à jour !'),
        error => handleError(error, this.stateService, this.router)
      )
  }

  ngOnInit(): void {
    this.photoForm = new FormGroup({
      shootingDate: new FormControl(this.photo.shootingDate),
    })
  }
}
