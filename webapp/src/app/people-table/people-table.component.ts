import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight, faPencilAlt, faTrash, faUserPlus} from '@fortawesome/free-solid-svg-icons'
import {Page} from '../page'
import {Person} from '../person'

@Component({
  selector: 'app-people-table',
  templateUrl: './people-table.component.html',
  styleUrls: ['./people-table.component.css']
})
export class PeopleTableComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight
  plusIcon: IconDefinition = faUserPlus
  trashIcon: IconDefinition = faTrash
  editIcon: IconDefinition = faPencilAlt

  @Input()
  page: Page<Person>

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter()

  @Output()
  personDelete: EventEmitter<Person> = new EventEmitter()

  constructor() {
  }

  emitPersonDelete(person: Person): void {
    this.personDelete.emit(person)
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  ngOnInit(): void {
  }

  previousPage(): void {
    this.pageChange.emit(this.page.no - 1)
  }
}
