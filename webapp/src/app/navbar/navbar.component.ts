import {Component, OnInit} from '@angular/core'
import {Router} from '@angular/router'
import {faSignOutAlt} from '@fortawesome/free-solid-svg-icons'
import {LoginService} from '../login.service'

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  logoutIcon = faSignOutAlt
  opened = false

  constructor(
    private loginService: LoginService,
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
  }

}
