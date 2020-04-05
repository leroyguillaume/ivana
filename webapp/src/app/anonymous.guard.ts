import {Injectable} from '@angular/core'
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router'
import {Observable} from 'rxjs'
import {LoginService} from './login.service'
import {map, tap} from 'rxjs/operators'

@Injectable({
  providedIn: 'root'
})
export class AnonymousGuard implements CanActivate {
  constructor(
    private loginService: LoginService,
    private router: Router
  ) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.loginService.isLogged.pipe(
      map(isLogged => !isLogged),
      tap(isAnonymous => {
        if (!isAnonymous) {
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate(['home'])
        }
      })
    )
  }
}
