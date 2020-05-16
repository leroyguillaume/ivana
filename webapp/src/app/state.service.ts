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
  uploadingPhotos: BehaviorSubject<boolean> = new BehaviorSubject(false)
  photosUploaded: EventEmitter<void> = new EventEmitter()
  selectedAlbums: BehaviorSubject<Set<string>> = new BehaviorSubject(new Set())

  currentAlbum: BehaviorSubject<Album> = new BehaviorSubject(null)
  currentPhotosPage: Page<Photo>

  startPhotoNavIndex: number = -1
  currentPhotoNavOffset: number = 0

  private success: BehaviorSubject<string> = new BehaviorSubject(null)
  private error: BehaviorSubject<string> = new BehaviorSubject(null)
  private successTimeoutId: number
  private errorTimeoutId: number

  constructor(
    private photoService: PhotoService
  ) {
  }

  clearError(): void {
    clearTimeout(this.errorTimeoutId)
    this.error.next(null)
  }

  clearSuccess(): void {
    clearTimeout(this.successTimeoutId)
    this.success.next(null)
  }

  sendErrorEvent(error: string): void {
    this.errorTimeoutId = this.displayMessage(error, this.error, this.errorTimeoutId)
  }

  sendSuccessEvent(success: string): void {
    this.successTimeoutId = this.displayMessage(success, this.success, this.successTimeoutId)
  }

  subscribeMessageEvent(
    successHandler: (error: string) => void,
    errorHandler: (error: string) => void
  ): void {
    this.error.subscribe(errorHandler)
    this.success.subscribe(successHandler)
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
            this.sendSuccessEvent('Toutes les photos ont été importées !')
          } else {
            if (totalSuccess > 0) {
              this.sendSuccessEvent(`${totalSuccess} photos ont été importées !`)
            }
            console.error(failures)
            this.sendErrorEvent(`${totalFailure} photos n'ont pas pu être importées. Veuillez réessayer ultérieurement.`)
          }
        },
        error => {
          console.error(error)
          this.sendErrorEvent('Une erreur inattendue s\'est produite. Veuillez réessayer ultérieurement.')
        }
      )
  }

  private displayMessage(msg: string, subject: BehaviorSubject<string>, timeoutId: number): number {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    subject.next(msg)
    return setTimeout(() => subject.next(null), 3000)
  }
}
