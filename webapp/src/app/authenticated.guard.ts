import {Injectable} from '@angular/core'
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router'
import {Observable} from 'rxjs'
import {LoginService} from './login.service'
import {map, tap} from 'rxjs/operators'

@Injectable({
  providedIn: 'root'
})
export class AuthenticatedGuard implements CanActivate {
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
      map(user => user !== null),
      tap(isLogged => {
        if (!isLogged) {
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate(['login'])
        }
      }))
  }

}
