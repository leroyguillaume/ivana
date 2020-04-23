import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Observable} from 'rxjs'
import {Page} from './page'
import {User} from './user'
import {environment} from '../environments/environment'
import {Album} from './album'

@Injectable({
  providedIn: 'root'
})
export class AlbumService {
  private baseUrl: string = `${environment.baseUrl}/api/v1/album`

  constructor(
    private http: HttpClient
  ) {
  }

  create(name: string): Observable<Album> {
    const album = {name}
    return this.http.post<Album>(this.baseUrl, album, {withCredentials: true})
  }

  getAll(page: number, size: number): Observable<Page<Album>> {
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
}
