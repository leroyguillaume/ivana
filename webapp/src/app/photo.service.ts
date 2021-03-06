import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Photo} from './photo'
import {Page} from './page'
import {Observable} from 'rxjs'
import {environment} from '../environments/environment'
import {NavigablePhoto} from './navigable-photo'
import {PhotoUploadResults} from './photo-upload-results'
import {SubjectPermissions} from './subject-permissions'
import {SubjectPermissionsUpdate} from './subject-permissions-update'
import {Person} from './person'

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

  get(id: string, albumId: string = null): Observable<NavigablePhoto> {
    let params = {
      navigable: 'true'
    }
    if (albumId != null) {
      params = {
        ...params,
        ...{
          album: albumId
        }
      }
    }
    return this.http.get<NavigablePhoto>(
      `${this.baseUrl}/${id}`,
      {
        withCredentials: true,
        params
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

  getPeople(id: string): Observable<Person[]> {
    return this.http.get<Person[]>(`${this.baseUrl}/${id}/people`, {withCredentials: true})
  }

  getPermissions(id: string, page: number, size: number): Observable<Page<SubjectPermissions>> {
    return this.http.get<Page<SubjectPermissions>>(
      `${this.baseUrl}/${id}/permissions`,
      {
        withCredentials: true,
        params: {
          page: page.toString(),
          size: size.toString()
        }
      }
    )
  }

  getShared(page: number, size: number): Observable<Page<Photo>> {
    return this.http.get<Page<Photo>>(
      `${this.baseUrl}/shared`,
      {
        withCredentials: true,
        params: {
          page: page.toString(),
          size: size.toString()
        }
      }
    )
  }

  rotate(id: string, degrees: number): Observable<void> {
    const dto = {
      type: 'rotation',
      degrees
    }
    return this.http.put<void>(`${this.baseUrl}/${id}/transform`, dto, {withCredentials: true})
  }

  update(id: string, shootingDate: Date): Observable<Photo> {
    const dto = {
      shootingDate
    }
    return this.http.put<Photo>(`${this.baseUrl}/${id}`, dto, {withCredentials: true})
  }

  updatePeople(id: string, peopleToAdd: string[], peopleToRemove: string[] = []): Observable<void> {
    const dto = {
      peopleToAdd,
      peopleToRemove
    }
    return this.http.put<void>(`${this.baseUrl}/${id}/people`, dto, {withCredentials: true})
  }

  updatePermissions(
    id: string,
    permissionsToAdd: SubjectPermissionsUpdate[],
    permissionsToRemove: SubjectPermissionsUpdate[]
  ): Observable<void> {
    const dto = {
      permissionsToAdd,
      permissionsToRemove
    }
    return this.http.put<void>(`${this.baseUrl}/${id}/permissions`, dto, {withCredentials: true})
  }

  upload(files: FileList): Observable<PhotoUploadResults> {
    const data = new FormData()
    for (let i = 0; i < files.length; ++i) {
      data.append('files', files.item(i))
    }
    return this.http.post<PhotoUploadResults>(this.baseUrl, data, {withCredentials: true})
  }
}
