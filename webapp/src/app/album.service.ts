import {Injectable} from '@angular/core'
import {HttpClient} from '@angular/common/http'
import {Observable} from 'rxjs'
import {Page} from './page'
import {environment} from '../environments/environment'
import {Album} from './album'
import {Photo} from './photo'
import {SubjectPermissions} from './subject-permissions'
import {SubjectPermissionsUpdate} from './subject-permissions-update'

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
    const dto = {name}
    return this.http.post<Album>(this.baseUrl, dto, {withCredentials: true})
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {withCredentials: true})
  }

  get(id: string): Observable<Album> {
    return this.http.get<Album>(`${this.baseUrl}/${id}`, {withCredentials: true})
  }

  getAll(page: number, size: number): Observable<Page<Album>> {
    return this.http.get<Page<Album>>(
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

  update(id: string, name: string, photosIdsToAdd: string[] = [], photosIdsToRemove: string[] = []): Observable<Album> {
    const dto = {
      name,
      photosToAdd: photosIdsToAdd,
      photosToRemove: photosIdsToRemove
    }
    return this.http.put<Album>(`${this.baseUrl}/${id}`, dto, {withCredentials: true})
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
}
