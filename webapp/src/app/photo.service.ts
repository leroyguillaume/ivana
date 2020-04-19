import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Photo} from './photo'
import {Page} from './page'
import {Observable} from 'rxjs'
import {environment} from '../environments/environment'
import {NavigablePhoto} from './navigable-photo'
import {PhotoUploadResults} from './photo-upload-results'
import {RotationDirection} from './rotation-direction'

@Injectable({
  providedIn: 'root'
})
export class PhotoService {
  private baseUrl: string = `${environment.baseUrl}/api/v1/photo`

  constructor(
    private http: HttpClient
  ) {
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {withCredentials: true})
  }

  get(id: string): Observable<NavigablePhoto> {
    return this.http.get<NavigablePhoto>(
      `${this.baseUrl}/${id}`,
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

  rotate(id: string, dir: RotationDirection): Observable<void> {
    const body = {
      type: 'rotation',
      direction: dir
    }
    return this.http.put<void>(`${this.baseUrl}/${id}/transform`, body, {withCredentials: true})
  }

  upload(files: FileList): Observable<PhotoUploadResults> {
    const data = new FormData()
    for (let i = 0; i < files.length; ++i) {
      data.append('files', files.item(i))
    }
    return this.http.post<PhotoUploadResults>(this.baseUrl, data, {withCredentials: true})
  }
}
