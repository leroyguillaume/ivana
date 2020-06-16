import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faTrash, faUserPlus} from '@fortawesome/free-solid-svg-icons'
import {Role, RoleLabels} from '../role'
import {User} from '../user'
import {LoginService} from '../login.service'
import {Page} from '../page'

@Component({
  selector: 'app-users-table',
  templateUrl: './users-table.component.html',
  styleUrls: ['./users-table.component.css']
})
export class UsersTableComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  plusIcon: IconDefinition = faUserPlus
  trashIcon: IconDefinition = faTrash

  Role: typeof Role = Role
  roleLabels: Map<Role, string> = new Map(
    RoleLabels
      .map(roleLabel => [roleLabel.role, roleLabel.label])
  )

  currentUser: User

  @Input()
  page: Page<User>

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter()

  @Output()
  userDelete: EventEmitter<User> = new EventEmitter()

  constructor(
    private loginService: LoginService
  ) {
  }

  emitUserDelete(user: User): void {
    this.userDelete.emit(user)
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  ngOnInit(): void {
    this.loginService.loggedUser().subscribe(user => this.currentUser = user)
  }

  previousPage(): void {
    this.pageChange.emit(this.page.no - 1)
  }

}
