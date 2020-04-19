import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {faArrowLeft, faArrowRight} from '@fortawesome/free-solid-svg-icons'
import {Page} from '../page'
import {Photo} from '../photo'
import {StateService} from '../state.service'
import {Router} from '@angular/router'
import {environment} from '../../environments/environment'

@Component({
  selector: 'app-photo-grid',
  templateUrl: './photo-grid.component.html',
  styleUrls: ['./photo-grid.component.css']
})
export class PhotoGridComponent implements OnInit {
  previousIcon: IconDefinition = faArrowLeft
  nextIcon: IconDefinition = faArrowRight

  baseUrl: string = environment.baseUrl

  @Input()
  page: Page<Photo>

  @Output()
  pageChange: EventEmitter<number> = new EventEmitter<number>()

  constructor(
    private stateService: StateService,
    private router: Router
  ) {
  }

  nextPage(): void {
    this.pageChange.emit(this.page.no + 1)
  }

  ngOnInit(): void {
  }

  openPhoto(id: string): void {
    this.stateService.startPhotoNavIndex = this.page.content.findIndex(photo => photo.id === id)
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/photo', id])
  }

  previousPage(): void {
    this.pageChange.emit(this.page.no - 1)
  }

}
