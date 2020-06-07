import {Component, Input, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner} from '@fortawesome/free-solid-svg-icons'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {Router} from '@angular/router'
import {finalize} from 'rxjs/operators'
import {Album} from '../album'
import {AlbumService} from '../album.service'
import {StateService} from '../state.service'
import {handleError} from '../util'

@Component({
  selector: 'app-album-update-form',
  templateUrl: './album-update-form.component.html',
  styleUrls: ['./album-update-form.component.css']
})
export class AlbumUpdateFormComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner

  updating: boolean = false

  albumForm: FormGroup

  @Input()
  album: Album

  constructor(
    private albumService: AlbumService,
    private stateService: StateService,
    private router: Router
  ) {
  }

  get name(): AbstractControl {
    return this.albumForm.get('name')
  }

  update(): void {
    this.updating = true
    this.albumService.update(this.album.id, this.name.value)
      .pipe(finalize(() => this.updating = false))
      .subscribe(
        () => this.stateService.sendSuccessEvent('L\'album mis à jour !'),
        error => handleError(error, this.stateService, this.router)
      )
  }

  ngOnInit(): void {
    this.albumForm = new FormGroup({
      name: new FormControl(this.album.name),
    })
  }
}
