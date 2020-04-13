import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Router} from '@angular/router'
import {faSignOutAlt, faSpinner, faUpload} from '@fortawesome/free-solid-svg-icons'
import {LoginService} from '../login.service'
import {StateService} from '../state.service'
import {IconDefinition} from '@fortawesome/fontawesome-common-types'

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

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private loginService: LoginService,
    private uploaderService: StateService,
    private router: Router
  ) {
  }

  get currentUrl(): string {
    return this.router.url
  }

  logout(): void {
    this.loginService.logout().subscribe(() => this.router.navigate(['login']))
  }

  ngOnInit(): void {
    this.uploaderService.uploadingPhotos.subscribe(uploading => this.uploading = uploading)
  }

  selectFiles(): void {
    this.filesInput.nativeElement.click()
  }

  upload(): void {
    const files = this.filesInput.nativeElement.files
    if (files.length > 0) {
      this.uploaderService.uploadPhotos(files)
    }
  }

}
