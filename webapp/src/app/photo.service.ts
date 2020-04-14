import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Photo} from './photo'
import {Page} from './page'
import {Observable} from 'rxjs'
import {environment} from '../environments/environment'
import {NavigablePhoto} from './navigable-photo'
import {PhotoUploadResults} from './photo-upload-results'

@Injectable({
  providedIn: 'root'
})
export class PhotoService {
  private baseUrl: string = `${environment.baseUrl}/api/v1/photo`

  constructor(
    private http: HttpClient
  ) {
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

  upload(files: FileList): Observable<PhotoUploadResults> {
    const data = new FormData()
    for (let i = 0; i < files.length; ++i) {
      data.append('files', files.item(i))
    }
    return this.http.post<PhotoUploadResults>(this.baseUrl, data, {withCredentials: true})
  }
}
