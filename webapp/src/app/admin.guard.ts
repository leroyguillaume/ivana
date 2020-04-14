import {Injectable} from '@angular/core'
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router'
import {Observable} from 'rxjs'
import {map, tap} from 'rxjs/operators'
import {isAdmin} from './user'
import {LoginService} from './login.service'

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private loginService: LoginService,
    private router: Router
  ) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.loginService.loggedUser().pipe(
      map(user => isAdmin(user)),
      tap(userIsAdmin => {
        if (!userIsAdmin) {
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate(['home'])
        }
      })
    )
  }
}
