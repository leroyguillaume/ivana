import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {ActivatedRoute, Router} from '@angular/router'
import {faSignOutAlt, faSpinner, faUpload} from '@fortawesome/free-solid-svg-icons'
import {LoginService} from '../login.service'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'
import {isAdmin, User} from '../user'

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  logoutIcon: IconDefinition = faSignOutAlt
  uploadIcon: IconDefinition = faUpload
  spinnerIcon: IconDefinition = faSpinner

  opened: boolean = false
  uploading: boolean = false

  currentUser: User

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private loginService: LoginService,
    private stateService: StateService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  get currentUrl(): string {
    return this.router.url
  }

  get currentUserIsAdmin(): boolean {
    return isAdmin(this.currentUser)
  }

  logout(): void {
    this.loginService.logout().subscribe(() => this.router.navigate(['login']))
  }

  ngOnInit(): void {
    this.stateService.uploadingPhotos.subscribe(uploading => this.uploading = uploading)
    this.loginService.loggedUser().subscribe(user => this.currentUser = user)
  }

  selectFiles(): void {
    this.filesInput.nativeElement.click()
  }

  upload(): void {
    const files = this.filesInput.nativeElement.files
    if (files.length > 0) {
      this.stateService.uploadPhotos(files)
    }
  }

}
