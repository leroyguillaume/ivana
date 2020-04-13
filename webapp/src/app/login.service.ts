import {Injectable} from '@angular/core'
import {Credentials} from './credentials'
import {HttpClient} from '@angular/common/http'
import {environment} from '../environments/environment'
import {Observable, of} from 'rxjs'
import {catchError, finalize, map, tap} from 'rxjs/operators'

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private firstCall: boolean = true
  private logged: boolean = false

  private baseUrl: string = environment.baseUrl

  constructor(
    private readonly http: HttpClient
  ) {
  }

  get isLogged(): Observable<boolean> {
    if (!this.logged && this.firstCall) {
      return this.http.get(`${this.baseUrl}/api/v1/login`, {withCredentials: true})
        .pipe(
          map(() => true),
          catchError(() => of(false)),
          tap(logged => this.logged = logged),
          finalize(() => this.firstCall = false)
        )
    }
    return of(this.logged)
  }

  login(username: string, password: string): Observable<any> {
    const creds = new Credentials(username, password)
    return this.http.post(`${this.baseUrl}/api/v1/login`, creds, {withCredentials: true})
      .pipe(tap(() => this.logged = true))
  }

  logout(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/logout`, {withCredentials: true})
      .pipe(tap(() => this.logged = false))
  }
}
