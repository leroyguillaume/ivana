import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {SubjectPermissions} from '../subject-permissions'
import {Page} from '../page'
import {Permission} from '../permission'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faPlus, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {SubjectPermissionsUpdate} from '../subject-permissions-update'
import {OwnableEntity} from '../ownable-entity'
import {NgbModal} from '@ng-bootstrap/ng-bootstrap'
import {NewPermissionsModalComponent} from '../new-permissions-modal/new-permissions-modal.component'
import {SubjectPermissionsUpdateEvent} from '../subject-permissions-update-event'

@Component({
  selector: 'app-permission',
  templateUrl: './permission.component.html',
  styleUrls: ['./permission.component.css']
})
export class PermissionComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  plusIcon: IconDefinition = faPlus
  spinnerIcon: IconDefinition = faSpinner

  newSubjsPerms: SubjectPermissions[] = []

  @Input()
  entity: OwnableEntity

  @Input()
  page: Page<SubjectPermissions>

  @Input()
  updating: boolean = false

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter()

  @Output()
  permissionsUpdate: EventEmitter<SubjectPermissionsUpdateEvent> = new EventEmitter()

  constructor(
    private modalService: NgbModal
  ) {
  }

  emitPermissionsUpdate(): void {
    const subjsPermsToAdd = this.newSubjsPerms.map(subjPerms => new SubjectPermissionsUpdate(subjPerms.subjectId, subjPerms.permissions))
    const subjsPermsToRemove = this.newSubjsPerms.map(subjPerms =>
      new SubjectPermissionsUpdate(
        subjPerms.subjectId,
        Object.values(Permission)
          .map(perm => perm as Permission)
          .filter(perm => subjPerms.permissions.indexOf(perm) === -1)
      )
    )
    this.permissionsUpdate.emit(new SubjectPermissionsUpdateEvent(subjsPermsToAdd, subjsPermsToRemove))
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  openNewPermissionsModal(): void {
    const modalRef = this.modalService.open(NewPermissionsModalComponent)
    const newPermsSubjsIds = this.newSubjsPerms.map(subjPerms => subjPerms.subjectId)
    modalRef.componentInstance.usersIdsBlacklist = new Set(Array.from([this.entity.ownerId].concat(newPermsSubjsIds)))
    modalRef.result.then(
      (subjPerms: SubjectPermissions) => this.newSubjsPerms.push(subjPerms),
      () => {
      }
    )
  }

  ngOnInit(): void {
  }

  previousPage(): void {
    this.pageChange.emit(this.page.no - 1)
  }
}
