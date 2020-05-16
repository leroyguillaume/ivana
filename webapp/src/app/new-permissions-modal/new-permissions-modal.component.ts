import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner, faTimes} from '@fortawesome/free-solid-svg-icons'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {StateService} from '../state.service'
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap'
import {Router} from '@angular/router'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {UserService} from '../user.service'
import {User} from '../user'
import {Permission} from '../permission'
import {SubjectPermissions} from '../subject-permissions'

@Component({
  selector: 'app-new-permissions-modal',
  templateUrl: './new-permissions-modal.component.html',
  styleUrls: ['./new-permissions-modal.component.css']
})
export class NewPermissionsModalComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  closeIcon: IconDefinition = faTimes

  loading: boolean = true

  users: User[]
  permsForm: FormGroup = new FormGroup({
    userId: new FormControl(''),
    canRead: new FormControl(false),
    canUpdate: new FormControl(false),
    canDelete: new FormControl(false),
    canUpdatePermissions: new FormControl(false)
  })

  usersIdsBlacklist: Set<string> = new Set()

  constructor(
    private userService: UserService,
    private stateService: StateService,
    public activeModal: NgbActiveModal,
    private router: Router
  ) {
  }

  get userId(): AbstractControl {
    return this.permsForm.get('userId')
  }

  ngOnInit(): void {
    this.loading = true
    // TODO: it won't work when there are too many users
    this.userService.getAll(1, 100)
      .pipe(finalize(() => this.loading = false))
      .subscribe(
        page => this.users = page.content.filter(user => !this.usersIdsBlacklist.has(user.id)),
        error => handleError(error, this.stateService, this.router)
      )
  }

  submit(): void {
    const perms = []
    if (this.permsForm.get('canRead').value) {
      perms.push(Permission.Read)
    }
    if (this.permsForm.get('canUpdate').value) {
      perms.push(Permission.Update)
    }
    if (this.permsForm.get('canDelete').value) {
      perms.push(Permission.Delete)
    }
    if (this.permsForm.get('canUpdatePermissions').value) {
      perms.push(Permission.UpdatePermissions)
    }
    const subj = this.users.find(user => user.id === this.userId.value)
    const subjPerms = new SubjectPermissions(subj.id, subj.name, perms)
    this.activeModal.close(subjPerms)
  }
}
