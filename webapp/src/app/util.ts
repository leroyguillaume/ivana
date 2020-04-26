import {map} from 'rxjs/operators'
import {ActivatedRoute, Router} from '@angular/router'
import {StateService} from './state.service'
import {HttpErrorResponse} from '@angular/common/http'
import {ApiError} from './api-error'

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

export function handleError(error: HttpErrorResponse, stateService: StateService, router: Router): void {
  console.error(error)
  const dto: ApiError = error.error
  switch (dto.code) {
    case 'album_already_contains_photos':
      stateService.error.next('L\'album sélectionné contient déjà une ou plusieurs de ces photos.')
      break
    case 'forbidden':
      // noinspection JSIgnoredPromiseFromCall
      router.navigate(['/forbidden'])
      break
    default:
      stateService.error.next('Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.')
      break
  }
}
