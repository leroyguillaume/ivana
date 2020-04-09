import {EventEmitter, Injectable} from '@angular/core'
import {BehaviorSubject} from 'rxjs'
import {PhotoService} from './photo.service'
import {finalize} from 'rxjs/operators'

@Injectable({
  providedIn: 'root'
})
export class UploaderService {
  uploading = new BehaviorSubject(false)
  success = new BehaviorSubject(null)
  error = new BehaviorSubject(null)
  filesUploaded = new EventEmitter()

  constructor(
    private photoService: PhotoService
  ) {
  }

  upload(files: FileList) {
    this.uploading.next(true)
    this.photoService.upload(files)
      .pipe(
        finalize(() => {
          this.uploading.next(false)
          this.filesUploaded.emit()
        })
      )
      .subscribe(
        uploadResults => {
          const failures = uploadResults.results.filter(result => result.type === 'failure')
          const totalSuccess = uploadResults.results.filter(result => result.type === 'success').length
          const totalFailure = failures.length
          if (totalFailure === 0) {
            this.success.next('Toutes les photos ont été importées !')
          } else {
            if (totalSuccess > 0) {
              this.success.next(`${totalSuccess} photos ont été importées !`)
            }
            console.error(failures)
            this.error.next(`${totalFailure} photos n'ont pas pu être importées. Veuillez réessayer ultérieurement.`)
          }
        },
        error => {
          console.error(error)
          this.error.next('Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.')
        }
      )
  }
}
