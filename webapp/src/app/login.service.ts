import {Injectable} from '@angular/core'
import {Credentials} from './credentials'
import {HttpClient} from '@angular/common/http'
import {environment} from '../environments/environment'
import {Observable, of} from 'rxjs'
import {catchError, flatMap, tap} from 'rxjs/operators'
import {User} from './user'
import {UserService} from './user.service'

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  private currentUser: User = null

  private baseUrl: string = `${environment.baseUrl}/api/v1`

  constructor(
    private userService: UserService,
    private http: HttpClient
  ) {
  }

  loggedUser(): Observable<User> {
    if (!this.currentUser) {
      return this.userService.me().pipe(
        catchError(() => of(null)),
        tap(user => this.currentUser = user),
      )
    }
    return of(this.currentUser)
  }

  login(username: string, password: string): Observable<User> {
    const creds = new Credentials(username, password)
    return this.http.post<void>(`${this.baseUrl}/login`, creds, {withCredentials: true})
      .pipe(flatMap(() => this.userService.me()))
  }

  logout(): Observable<void> {
    return this.http.get<void>(`${this.baseUrl}/logout`, {withCredentials: true})
      .pipe(tap(() => this.currentUser = null))
  }
}
