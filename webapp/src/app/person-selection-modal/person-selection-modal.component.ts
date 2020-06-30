import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faSpinner, faTimes} from '@fortawesome/free-solid-svg-icons'
import {Observable, of} from 'rxjs'
import {debounceTime, distinctUntilChanged, flatMap} from 'rxjs/operators'
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap'
import {NgbTypeaheadSelectItemEvent} from '@ng-bootstrap/ng-bootstrap/typeahead/typeahead'
import {Person} from '../person'
import {PersonService} from '../person.service'

@Component({
  selector: 'app-person-selection-modal',
  templateUrl: './person-selection-modal.component.html',
  styleUrls: ['./person-selection-modal.component.css']
})
export class PersonSelectionModalComponent implements OnInit {
  spinnerIcon: IconDefinition = faSpinner
  closeIcon: IconDefinition = faTimes

  selectedPerson: Person

  suggest = (obs: Observable<string>) => obs.pipe(
    debounceTime(200),
    distinctUntilChanged(),
    flatMap(q => {
      if (q.trim() === '') {
        return of([])
      } else {
        return this.personService.suggest(q)
      }
    })
  )

  format = (person: Person) => `${person.firstName} ${person.lastName}`

  constructor(
    private personService: PersonService,
    public activeModal: NgbActiveModal
  ) {
  }

  ngOnInit(): void {
  }

  selectPerson(event: NgbTypeaheadSelectItemEvent): void {
    this.selectedPerson = event.item
  }

  submit(): void {
    this.activeModal.close(this.selectedPerson)
  }
}
