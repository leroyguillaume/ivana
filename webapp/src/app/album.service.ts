import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Observable} from 'rxjs'
import {Page} from './page'
import {User} from './user'
import {environment} from '../environments/environment'
import {Album} from './album'
import {Photo} from './photo'

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

  get(id: string): Observable<Album> {
    return this.http.get<Album>(`${this.baseUrl}/${id}`, {withCredentials: true})
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

  getAllPhotos(albumId: string, page: number, size: number): Observable<Page<Photo>> {
    return this.http.get<Page<Photo>>(
      `${this.baseUrl}/${albumId}/content`,
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
