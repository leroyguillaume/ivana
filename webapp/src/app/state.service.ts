import {EventEmitter, Injectable} from '@angular/core'
import {BehaviorSubject} from 'rxjs'
import {PhotoService} from './photo.service'
import {finalize} from 'rxjs/operators'
import {Photo} from './photo'
import {Page} from './page'
import {Album} from './album'

@Injectable({
  providedIn: 'root'
})
export class StateService {
  success: BehaviorSubject<string> = new BehaviorSubject(null)
  error: BehaviorSubject<string> = new BehaviorSubject(null)

  uploadingPhotos: BehaviorSubject<boolean> = new BehaviorSubject(false)
  photosUploaded: EventEmitter<void> = new EventEmitter()
  selectedAlbums: BehaviorSubject<Set<string>> = new BehaviorSubject<Set<string>>(new Set())

  currentAlbum: Album
  currentPhotosPage: Page<Photo>

  startPhotoNavIndex: number = -1
  currentPhotoNavOffset: number = 0

  constructor(
    private photoService: PhotoService
  ) {
  }

  uploadPhotos(files: FileList): void {
    this.uploadingPhotos.next(true)
    this.photoService.upload(files)
      .pipe(
        finalize(() => {
          this.uploadingPhotos.next(false)
          this.photosUploaded.emit()
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
