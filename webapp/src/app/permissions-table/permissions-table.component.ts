import {Component, Input, OnInit} from '@angular/core'
import {SubjectPermissions} from '../subject-permissions'
import {Permission} from '../permission'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faCheck, faTimes, faTrash} from '@fortawesome/free-solid-svg-icons'

@Component({
  selector: 'app-permissions-table',
  templateUrl: './permissions-table.component.html',
  styleUrls: ['./permissions-table.component.css']
})
export class PermissionsTableComponent implements OnInit {
  trashIcon: IconDefinition = faTrash
  checkIcon: IconDefinition = faCheck
  crossIcon: IconDefinition = faTimes

  @Input()
  subjsPerms: SubjectPermissions[]

  @Input()
  ownerId: string

  @Input()
  removable: boolean = false

  constructor() {
  }

  ngOnInit(): void {
  }

  removeSubjectPermissions(subjPerms: SubjectPermissions): void {
    this.subjsPerms.splice(this.subjsPerms.indexOf(subjPerms), 1)
  }

  subjectCanDelete(subjPerms: SubjectPermissions): boolean {
    return subjPerms.permissions.indexOf(Permission.Delete) > -1
  }

  subjectCanRead(subjPerms: SubjectPermissions): boolean {
    return subjPerms.permissions.indexOf(Permission.Read) > -1
  }

  subjectCanUpdate(subjPerms: SubjectPermissions): boolean {
    return subjPerms.permissions.indexOf(Permission.Update) > -1
  }

  subjectCanUpdatePermissions(subjPerms: SubjectPermissions): boolean {
    return subjPerms.permissions.indexOf(Permission.UpdatePermissions) > -1
  }
}
