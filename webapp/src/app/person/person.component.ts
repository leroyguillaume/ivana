import {Component, OnInit} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faSpinner} from '@fortawesome/free-solid-svg-icons'
import {AbstractControl, FormControl, FormGroup} from '@angular/forms'
import {StateService} from '../state.service'
import {ActivatedRoute, Router} from '@angular/router'
import {finalize} from 'rxjs/operators'
import {handleError} from '../util'
import {PersonService} from '../person.service'
import {Person} from '../person'

@Component({
  selector: 'app-person',
  templateUrl: './person.component.html',
  styleUrls: ['./person.component.css']
})
export class PersonComponent implements OnInit {
  leftArrowIcon: IconDefinition = faArrowLeft
  spinnerIcon: IconDefinition = faSpinner

  loading: boolean = false

  personForm: FormGroup = new FormGroup({
    lastName: new FormControl(''),
    firstName: new FormControl(''),
  })

  person: Person

  constructor(
    private personService: PersonService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  get firstName(): AbstractControl {
    return this.personForm.get('firstName')
  }

  get lastName(): AbstractControl {
    return this.personForm.get('lastName')
  }

  back(): void {
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/admin'], {
      queryParams: {
        tab: 'people'
      }
    })
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id')
      if (id) {
        this.personService.get(id).subscribe(
          person => {
            this.person = person
            this.lastName.setValue(person.lastName)
            this.firstName.setValue(person.firstName)
          },
          error => {
            console.error(error)
            this.back()
          }
        )
      }
    })
  }

  submit(): void {
    this.loading = true
    if (this.person) {
      this.personService.update(this.person.id, this.lastName.value, this.firstName.value)
        .pipe(finalize(() => this.loading = false))
        .subscribe(
          () => this.stateService.sendSuccessEvent('Personne mise à jour !'),
          error => handleError(error, this.stateService, this.router)
        )
    } else {
      this.personService.create(this.lastName.value, this.firstName.value)
        .pipe(finalize(() => this.loading = false))
        .subscribe(
          person => {
            this.stateService.sendSuccessEvent('Personne créée !')
            this.person = person
          },
          error => handleError(error, this.stateService, this.router)
        )
    }
  }
}
