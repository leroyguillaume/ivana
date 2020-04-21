import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Observable} from 'rxjs'
import {Page} from './page'
import {User} from './user'
import {environment} from '../environments/environment'
import {Role} from './role'

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl: string = `${environment.baseUrl}/api/v1/user`

  constructor(
    private http: HttpClient
  ) {
  }

  create(name: string, pwd: string, role: Role): Observable<User> {
    const user = {name, pwd, role}
    return this.http.post<User>(this.baseUrl, user, {withCredentials: true})
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {withCredentials: true})
  }

  getAll(page: number, size: number): Observable<Page<User>> {
    return this.http.get<Page<User>>(
      this.baseUrl,
      {
        withCredentials: true,
        params: {
          page: page.toString(),
          size: size.toString()
        }
      }
    )
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/me`, {withCredentials: true})
  }

  updatePassword(newPwd: string): Observable<void> {
    const body = {newPwd}
    return this.http.put<void>(`${this.baseUrl}/password`, body, {withCredentials: true})
  }
}
