import {Component, ElementRef, OnInit, ViewChild} from '@angular/core'
import {Router} from '@angular/router'
import {faSignOutAlt, faSpinner, faUpload} from '@fortawesome/free-solid-svg-icons'
import {LoginService} from '../login.service'
import {UploaderService} from '../uploader.service'

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  logoutIcon = faSignOutAlt
  uploadIcon = faUpload
  spinnerIcon = faSpinner

  opened = false
  uploading = false

  @ViewChild('files')
  filesInput: ElementRef

  constructor(
    private loginService: LoginService,
    private uploaderService: UploaderService,
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
    this.uploaderService.uploading.subscribe(uploading => this.uploading = uploading)
  }

  selectFiles() {
    this.filesInput.nativeElement.click()
  }

  upload() {
    const files = this.filesInput.nativeElement.files
    if (files.length > 0) {
      this.uploaderService.upload(files)
    }
  }

}
