 
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';
import { MediaResponse, ExternalMediaResponse, MediaType } from '../models/media.models';
 
@Injectable({ providedIn: 'root' })
export class MediaService {
  private readonly API = 'http://localhost:8082/api/media';
  private readonly EXT = 'http://localhost:8082/api/external';
 
  constructor(private http: HttpClient) {}
 
 
  getAvailable(): Observable<MediaResponse[]> {
    return this.http.get<MediaResponse[]>(this.API);
  }
 
  getById(id: number): Observable<MediaResponse> {
    return this.http.get<MediaResponse>(`${this.API}/${id}`);
  }
 
  searchByType(type: MediaType): Observable<MediaResponse[]> {
    return this.http.get<MediaResponse[]>(`${this.API}/search/type`, { params: { type } });
  }
 
 
  searchExternalFilms(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/films`, { params: { query } });
  }
 
  searchExternalFilmsYoutube(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/films/youtube`, { params: { query } });
  }
 
  searchExternalFilmsArchive(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/films/archive`, { params: { query } });
  }
 
 
  searchExternalBooks(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/books`, { params: { query } });
  }
 
 
  searchExternalGames(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/games`, { params: { query } });
  }
 
  searchExternalFreeGames(query: string = ''): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/games/free`, { params: { query } });
  }
 
 
  searchExternalPodcastsIndex(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/podcasts`, { params: { query } })
      .pipe(catchError(() => of([])));  
  }
 
  searchExternalPodcastsItunes(query: string): Observable<ExternalMediaResponse[]> {
    return this.http.get<ExternalMediaResponse[]>(`${this.EXT}/podcasts/itunes`, { params: { query } })
      .pipe(catchError(() => of([])));  
  }
 
  
  searchExternalPodcasts(query: string): Observable<ExternalMediaResponse[]> {
    return forkJoin({
      podcastIndex: this.searchExternalPodcastsIndex(query),
      itunes:       this.searchExternalPodcastsItunes(query),
    }).pipe(
      map(({ podcastIndex, itunes }) => {
        const merged = [...podcastIndex, ...itunes];
 
        const seen = new Set<string>();
        return merged.filter(item => {
          const key = item.externalId ?? item.title;
          if (seen.has(key)) return false;
          seen.add(key);
          return true;
        });
      })
    );
  }
}
