import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Photo} from './photo'
import {Page} from './page'
import {Observable} from 'rxjs'
import {environment} from '../environments/environment'
import {NavigablePhoto} from './navigable-photo'

@Injectable({
  providedIn: 'root'
})
export class PhotoService {
  private baseUrl = environment.baseUrl

  constructor(
    private http: HttpClient
  ) {
  }

  get(id: string): Observable<NavigablePhoto> {
    return this.http.get<NavigablePhoto>(
      `${this.baseUrl}/api/v1/photo/${id}`,
      {
        withCredentials: true,
        params: {
          navigable: 'true'
        }
      }
    )
  }

  getAll(page: number, size: number): Observable<Page<Photo>> {
    return this.http.get<Page<Photo>>(
      `${this.baseUrl}/api/v1/photo`,
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
