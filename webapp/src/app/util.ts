import {map} from 'rxjs/operators'
import {ActivatedRoute} from '@angular/router'
import {StateService} from './state.service'

export function fetchPageFromQueryParam(route: ActivatedRoute, fetchPage: (no: number) => void): void {
  route.queryParamMap
    .pipe(map(params => params.get('page')))
    .subscribe(page => {
      const no = Number(page)
      if (isNaN(no) || no < 1) {
        fetchPage(1)
      } else {
        fetchPage(no)
      }
    })
}

export function handleError(error, stateService: StateService): void {
  console.error(error)
  stateService.error.next('Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.')
}
