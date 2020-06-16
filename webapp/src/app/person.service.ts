import {Injectable} from '@angular/core'
import {environment} from '../environments/environment'
import {HttpClient} from '@angular/common/http'
import {Observable} from 'rxjs'
import {Person} from './person'
import {Page} from './page'

@Injectable({
  providedIn: 'root'
})
export class PersonService {
  private baseUrl: string = `${environment.baseUrl}/api/v1/person`

  constructor(
    private http: HttpClient
  ) {
  }

  create(lastName: string, firstName: string): Observable<Person> {
    const person = {lastName, firstName}
    return this.http.post<Person>(this.baseUrl, person, {withCredentials: true})
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, {withCredentials: true})
  }

  get(id: string): Observable<Person> {
    return this.http.get<Person>(`${this.baseUrl}/${id}`, {withCredentials: true})
  }

  getAll(page: number, size: number): Observable<Page<Person>> {
    return this.http.get<Page<Person>>(
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

  suggest(name: string, count: number = 5): Observable<Person[]> {
    return this.http.get<Person[]>(
      `${this.baseUrl}/suggest`,
      {
        withCredentials: true,
        params: {
          q: name,
          count: count.toString()
        }
      }
    )
  }

  update(id: string, lastName: string, firstName: string): Observable<Person> {
    const dto = {lastName, firstName}
    return this.http.put<Person>(`${this.baseUrl}/${id}`, dto, {withCredentials: true})
  }
}
