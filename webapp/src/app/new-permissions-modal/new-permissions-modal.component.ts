import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner, faTimes} from '@fortawesome/free-solid-svg-icons'
import {FormControl, FormGroup} from '@angular/forms'
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap'
import {debounceTime, distinctUntilChanged, flatMap} from 'rxjs/operators'
import {UserService} from '../user.service'
import {User} from '../user'
import {Permission} from '../permission'
import {SubjectPermissions} from '../subject-permissions'
import {NgbTypeaheadSelectItemEvent} from '@ng-bootstrap/ng-bootstrap/typeahead/typeahead'
import {Observable, of} from 'rxjs'

@Component({
  selector: 'app-new-permissions-modal',
  templateUrl: './new-permissions-modal.component.html',
  styleUrls: ['./new-permissions-modal.component.css']
})
export class NewPermissionsModalComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  closeIcon: IconDefinition = faTimes

  selectedUser: User
  permsForm: FormGroup = new FormGroup({
    username: new FormControl(''),
    canRead: new FormControl(false),
    canUpdate: new FormControl(false),
    canDelete: new FormControl(false),
    canUpdatePermissions: new FormControl(false)
  })

  usersIdsBlacklist: Set<string> = new Set()

  suggest = (obs: Observable<string>) => obs.pipe(
    debounceTime(200),
    distinctUntilChanged(),
    flatMap(q => {
      if (q.trim() === '') {
        return of([])
      } else {
        return this.userService.suggest(q)
      }
    })
  )

  format = (user: User) => user.name

  constructor(
    private userService: UserService,
    public activeModal: NgbActiveModal
  ) {
  }

  ngOnInit(): void {
  }

  selectUser(event: NgbTypeaheadSelectItemEvent): void {
    this.selectedUser = event.item
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
    const subjPerms = new SubjectPermissions(this.selectedUser.id, this.selectedUser.name, perms)
    this.activeModal.close(subjPerms)
  }
}
