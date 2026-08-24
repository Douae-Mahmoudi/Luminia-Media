import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth';
import { Collection, CreateCollectionInput } from '../models/collection.models';

@Injectable({ providedIn: 'root' })
export class CollectionService {

  private readonly API = 'http://localhost:8083/api/collections';

  constructor(
    private http: HttpClient,
    private auth: AuthService,
  ) {}

  private get headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }

  create(input: CreateCollectionInput): Observable<Collection> {
    return this.http.post<Collection>(
      this.API, input, { headers: this.headers },
    ).pipe(catchError(() => of({} as Collection)));
  }

  getById(id: string): Observable<Collection> {
    return this.http.get<Collection>(
      `${this.API}/${id}`, { headers: this.headers },
    ).pipe(catchError(() => of({} as Collection)));
  }

  getByUserId(userId: string): Observable<Collection[]> {
    return this.http.get<Collection[]>(
      `${this.API}/user/${userId}`, { headers: this.headers },
    ).pipe(catchError(() => of([])));
  }

  getPublicByUserId(userId: string): Observable<Collection[]> {
    return this.http.get<Collection[]>(
      `${this.API}/user/${userId}/public`, { headers: this.headers },
    ).pipe(catchError(() => of([])));
  }

  getAllPublic(): Observable<Collection[]> {
    return this.http.get<Collection[]>(
      `${this.API}/public`, { headers: this.headers },
    ).pipe(catchError(() => of([])));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(
      `${this.API}/${id}`, { headers: this.headers },
    ).pipe(catchError(() => of(void 0)));
  }

  addMedia(collectionId: string, mediaId: string): Observable<Collection> {
    return this.http.post<Collection>(
      `${this.API}/${collectionId}/media/${mediaId}`, {}, { headers: this.headers },
    ).pipe(catchError(() => of({} as Collection)));
  }

  removeMedia(collectionId: string, mediaId: string): Observable<Collection> {
    return this.http.delete<Collection>(
      `${this.API}/${collectionId}/media/${mediaId}`, { headers: this.headers },
    ).pipe(catchError(() => of({} as Collection)));
  }
}