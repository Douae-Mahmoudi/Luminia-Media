import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Comment, UserMediaData } from '../models/interaction.models';
import { AuthService } from './auth';

export interface WatchlistItem {
  id?:      string | number;
  userId?:  string;
  mediaId:  string;
}

@Injectable({ providedIn: 'root' })
export class InteractionService {

  private http        = inject(HttpClient);
  private authService = inject(AuthService);
  private readonly BASE = 'http://localhost:8083';
  private readonly AUTH = 'http://localhost:8081';

  private userIdToEmail = new Map<string, string>();

  private get me(): string {
    return this.authService.getUsername() ?? 'anonyme';
  }

  private get numericUserId(): string {
    const stored = localStorage.getItem('userNumericId');
    if (stored) return stored;
    
    const token = this.token;
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.userId) return String(payload.userId);
        if (payload.sub && !payload.sub.includes('@')) return payload.sub;
        if (payload.id) return String(payload.id);
      } catch (e) { console.warn('Erreur extraction ID du token', e); }
    }
    
    const email = this.me;
   
    return '0';
  }

  private get token(): string {
    const svc = this.authService as any;
    return svc.getToken?.()
      ?? svc.getAccessToken?.()
      ?? svc.token
      ?? localStorage.getItem('access_token')
      ?? localStorage.getItem('token')
      ?? sessionStorage.getItem('token')
      ?? '';
  }

  private headers(extras: Record<string, string> = {}): HttpHeaders {
    const base: Record<string, string> = {
      'X-User-Id': this.me,
      'X-User-Numeric-Id': this.numericUserId,
      ...extras,
    };
    const jwt = this.token;
    if (jwt) base['Authorization'] = `Bearer ${jwt}`;
    return new HttpHeaders(base);
  }

  private resolveEmail(userId: string): Observable<string> {
    if (userId.includes('@')) return of(userId);
    if (this.userIdToEmail.has(userId)) {
      return of(this.userIdToEmail.get(userId)!);
    }
    const cacheKey = `lumina_uid_email_${userId}`;
    const cached   = localStorage.getItem(cacheKey);
    if (cached) {
      this.userIdToEmail.set(userId, cached);
      return of(cached);
    }
    const myUserId = localStorage.getItem('userId') ?? '';
    if (userId === myUserId) {
      const myEmail = this.authService.getUsername() ?? userId;
      this.userIdToEmail.set(userId, myEmail);
      localStorage.setItem(cacheKey, myEmail);
      return of(myEmail);
    }
    const authHeaders = new HttpHeaders({ 'Authorization': `Bearer ${this.token}` });
    return this.http.get<{ id: number; email: string }>(
      `${this.AUTH}/auth/me`,
      { headers: authHeaders }
    ).pipe(
      map(profile => {
        const email = profile.email ?? userId;
        this.userIdToEmail.set(userId, email);
        localStorage.setItem(cacheKey, email);
        return email;
      }),
      catchError(() => of(userId))
    );
  }

  private normalizeComment(raw: any): Comment {
    const username = raw?.username
      || raw?.userName
      || raw?.user?.username
      || raw?.user?.email
      || raw?.userId
      || '';
    return { 
      ...raw, 
      username,
      userId: raw?.userId || raw?.user?.id || raw?.user?.userId || ''
    } as Comment;
  }

  private normalizeComments(raws: any[]): Observable<Comment[]> {
    if (!raws.length) return of([]);
    const resolved$ = raws.map(raw => {
      const comment = this.normalizeComment(raw);
      if (comment.username && /^\d+$/.test(comment.username)) {
        return this.resolveEmail(comment.username).pipe(
          map(email => ({ ...comment, username: email }))
        );
      }
      return of(comment);
    });
    return new Observable<Comment[]>(observer => {
      const results: Comment[] = new Array(resolved$.length);
      let done = 0;
      resolved$.forEach((obs, i) => {
        obs.subscribe({
          next: c => {
            results[i] = c;
            if (++done === resolved$.length) {
              observer.next(results);
              observer.complete();
            }
          },
          error: () => {
            results[i] = { 
              ...raws[i], 
              username: raws[i]?.userId ?? '',
              userId: raws[i]?.userId ?? ''
            };
            if (++done === resolved$.length) {
              observer.next(results);
              observer.complete();
            }
          }
        });
      });
    });
  }

  private userKey(username: string): string {
    return `lumina_user_${encodeURIComponent(username)}`;
  }

  private likesKey(mediaId: number): string {
    return `lumina_likes_media_${mediaId}`;
  }

  private externalLikesKey(externalKey: string): string {
    return `lumina_likes_ext_${externalKey}`;
  }

  private externalCommentsKey(externalKey: string): string {
    return `lumina_comments_ext_${externalKey}`;
  }

  private extPersonalKeyStr(username: string, externalKey: string): string {
    return `lumina_ext_personal_${username}_${encodeURIComponent(externalKey)}`;
  }

  getExtPersonal(username: string, externalKey: string): Record<string, unknown> {
    try {
      const raw = localStorage.getItem(this.extPersonalKeyStr(username, externalKey));
      return raw ? (JSON.parse(raw) as Record<string, unknown>) : {};
    } catch { return {}; }
  }

  private setExtPersonal(username: string, externalKey: string, data: Record<string, unknown>): void {
    localStorage.setItem(this.extPersonalKeyStr(username, externalKey), JSON.stringify(data));
  }

  private getUserData(username: string): Record<string, UserMediaData> {
    try {
      const raw = localStorage.getItem(this.userKey(username));
      return raw ? (JSON.parse(raw) as Record<string, UserMediaData>) : {};
    } catch { return {}; }
  }

  private setUserData(username: string, data: Record<string, UserMediaData>): void {
    localStorage.setItem(this.userKey(username), JSON.stringify(data));
  }

  private getMediaEntry(username: string, mediaId: number): UserMediaData {
    const data = this.getUserData(username);
    return data[String(mediaId)] ?? { favorited: false, inWatchlist: false, userRating: 0 };
  }

  private setMediaEntry(username: string, mediaId: number, entry: UserMediaData): void {
    const data = this.getUserData(username);
    data[String(mediaId)] = entry;
    this.setUserData(username, data);
  }

  private getSharedLikers(storageKey: string): string[] {
    try {
      const raw = localStorage.getItem(storageKey);
      return raw ? (JSON.parse(raw) as string[]) : [];
    } catch { return []; }
  }

  private setSharedLikers(storageKey: string, likers: string[]): void {
    localStorage.setItem(storageKey, JSON.stringify(likers));
  }

  private getSharedComments(storageKey: string): Comment[] {
    try {
      const raw = localStorage.getItem(storageKey);
      return raw ? (JSON.parse(raw) as Comment[]) : [];
    } catch { return []; }
  }

  private setSharedComments(storageKey: string, comments: Comment[]): void {
    localStorage.setItem(storageKey, JSON.stringify(comments));
  }

  getMediaState(mediaId: number): {
    liked: boolean; favorited: boolean; inWatchlist: boolean;
    likesCount: number; userRating: number;
  } {
    const entry  = this.getMediaEntry(this.me, mediaId);
    const likers = this.getSharedLikers(this.likesKey(mediaId));
    return {
      liked:       likers.includes(this.me),
      likesCount:  likers.length,
      favorited:   entry.favorited   ?? false,
      inWatchlist: entry.inWatchlist ?? false,
      userRating:  entry.userRating  ?? 0,
    };
  }


  likeMedia(mediaId: number, mediaTitle = 'Média', ownerId = '0'): Observable<void> {
    const key    = this.likesKey(mediaId);
    const likers = this.getSharedLikers(key);
    if (!likers.includes(this.me)) {
      this.setSharedLikers(key, [...likers, this.me]);
    }
    return this.http.post<void>(
      `${this.BASE}/api/likes/${mediaId}`,
      {},
      { headers: this.headers({ 'X-Username': this.me, 'X-Owner-Id': ownerId, 'X-Media-Title': mediaTitle }) }
    ).pipe(catchError(() => of(undefined as void)));
  }

  unlikeMedia(mediaId: number): Observable<void> {
    const key = this.likesKey(mediaId);
    this.setSharedLikers(key, this.getSharedLikers(key).filter(u => u !== this.me));
    return this.http.delete<void>(
      `${this.BASE}/api/likes/${mediaId}`,
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }

  isLikedByCurrentUser(mediaId: number): boolean {
    return this.getSharedLikers(this.likesKey(mediaId)).includes(this.me);
  }

  getLikesCount(mediaId: number): number {
    return this.getSharedLikers(this.likesKey(mediaId)).length;
  }

  getLikesCountFromBackend(mediaId: number): Observable<number> {
    return this.http.get<{ count: number }>(
      `${this.BASE}/api/likes/${mediaId}/count`,
      { headers: this.headers() }
    ).pipe(
      map(res => res.count),
      catchError(() => of(this.getLikesCount(mediaId)))
    );
  }

  isLikedFromBackend(mediaId: number): Observable<boolean> {
    return this.http.get<{ liked: boolean }>(
      `${this.BASE}/api/likes/${mediaId}/user`,
      { headers: this.headers() }
    ).pipe(
      map(res => res.liked),
      catchError(() => of(this.isLikedByCurrentUser(mediaId)))
    );
  }


  getExternalLikeState(externalKey: string): { liked: boolean; likesCount: number } {
    const likers = this.getSharedLikers(this.externalLikesKey(externalKey));
    return { liked: likers.includes(this.me), likesCount: likers.length };
  }

  getExternalLikeStateFromBackend(externalKey: string): Observable<{ liked: boolean; likesCount: number }> {
    const liked$ = this.http.get<{ liked: boolean }>(
      `${this.BASE}/api/likes/external/${externalKey}/user`,
      { headers: this.headers() }
    ).pipe(map(r => r.liked), catchError(() => of(this.getExternalLikeState(externalKey).liked)));

    const count$ = this.http.get<{ count: number }>(
      `${this.BASE}/api/likes/external/${externalKey}/count`,
      { headers: this.headers() }
    ).pipe(map(r => r.count), catchError(() => of(this.getExternalLikeState(externalKey).likesCount)));

    return new Observable(observer => {
      let liked = false; let count = 0; let done = 0;
      const finish = () => {
        if (++done === 2) { observer.next({ liked, likesCount: count }); observer.complete(); }
      };
      liked$.subscribe({ next: v => { liked = v; finish(); }, error: () => finish() });
      count$.subscribe({ next: v => { count = v; finish(); }, error: () => finish() });
    });
  }

  likeExternal(externalKey: string, mediaTitle = 'Média', ownerId = '0'): Observable<{ liked: boolean; likesCount: number }> {
    const key    = this.externalLikesKey(externalKey);
    const likers = this.getSharedLikers(key);
    if (!likers.includes(this.me)) this.setSharedLikers(key, [...likers, this.me]);
    return this.http.post<void>(
      `${this.BASE}/api/likes/external/${externalKey}`,
      {},
      { headers: this.headers({ 'X-Username': this.me, 'X-Owner-Id': ownerId, 'X-Media-Title': mediaTitle }) }
    ).pipe(
      map(() => this.getExternalLikeState(externalKey)),
      catchError(() => of(this.getExternalLikeState(externalKey)))
    );
  }

  unlikeExternal(externalKey: string): Observable<{ liked: boolean; likesCount: number }> {
    const key = this.externalLikesKey(externalKey);
    this.setSharedLikers(key, this.getSharedLikers(key).filter(u => u !== this.me));
    return this.http.delete<void>(
      `${this.BASE}/api/likes/external/${externalKey}`,
      { headers: this.headers() }
    ).pipe(
      map(() => this.getExternalLikeState(externalKey)),
      catchError(() => of(this.getExternalLikeState(externalKey)))
    );
  }


  getFavoritesFromBackend(): Observable<{ mediaId: string }[]> {
    return this.http.get<{ mediaId: string }[]>(
      `${this.BASE}/api/favorites`,
      { headers: this.headers() }
    ).pipe(catchError(() => of([])));
  }

  isFavoritedFromBackend(mediaId: number): Observable<boolean> {
    return this.getFavoritesFromBackend().pipe(
      map(list => list.some(f => String(f.mediaId) === String(mediaId))),
      catchError(() => of(this.getMediaEntry(this.me, mediaId).favorited ?? false))
    );
  }

  addToFavorites(mediaId: number): Observable<void> {
    const entry = this.getMediaEntry(this.me, mediaId);
    entry.favorited = true;
    this.setMediaEntry(this.me, mediaId, entry);
    return this.http.post<void>(
      `${this.BASE}/api/favorites/${mediaId}`, {},
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }

  removeFromFavorites(mediaId: number): Observable<void> {
    const entry = this.getMediaEntry(this.me, mediaId);
    entry.favorited = false;
    this.setMediaEntry(this.me, mediaId, entry);
    return this.http.delete<void>(
      `${this.BASE}/api/favorites/${mediaId}`,
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }



  isExternalFavoritedFromBackend(externalKey: string): Observable<boolean> {
    const isFav = this.getExtPersonal(this.me, externalKey)['isFavorited'] === true;
    return of(isFav);
  }

  addToExternalFavorites(externalKey: string, meta?: {
    title?: string; author?: string; genre?: string; imageUrl?: string;
    type?: string; description?: string; releaseYear?: number;
  }): Observable<void> {
    const existing = this.getExtPersonal(this.me, externalKey);
    this.setExtPersonal(this.me, externalKey, {
      ...existing,
      isFavorited: true,
      title:       meta?.title       ?? existing['title']       ?? externalKey,
      author:      meta?.author      ?? existing['author']      ?? '',
      genre:       meta?.genre       ?? existing['genre']       ?? '',
      imageUrl:    meta?.imageUrl    ?? existing['imageUrl']    ?? '',
      type:        meta?.type        ?? existing['type']        ?? 'FILM',
      description: meta?.description ?? existing['description'] ?? '',
      releaseYear: meta?.releaseYear ?? existing['releaseYear'] ?? undefined,
      status:      'AVAILABLE',
    });
    return of(undefined as void);
  }

  removeFromExternalFavorites(externalKey: string): Observable<void> {
    const existing = this.getExtPersonal(this.me, externalKey);
    this.setExtPersonal(this.me, externalKey, { ...existing, isFavorited: false });
    return of(undefined as void);
  }


  getWatchlistFromBackend(): Observable<WatchlistItem[]> {
    return this.http.get<WatchlistItem[]>(
      `${this.BASE}/api/watchlist`,
      { headers: this.headers() }
    ).pipe(catchError(() => of([] as WatchlistItem[])));
  }

  isInWatchlistFromBackend(mediaId: number): Observable<boolean> {
    return this.getWatchlistFromBackend().pipe(
      map(list => list.some(w => String(w.mediaId) === String(mediaId))),
      catchError(() => of(this.getMediaEntry(this.me, mediaId).inWatchlist ?? false))
    );
  }

  addToWatchlist(mediaId: number): Observable<void> {
    const entry = this.getMediaEntry(this.me, mediaId);
    entry.inWatchlist = true;
    this.setMediaEntry(this.me, mediaId, entry);
    return this.http.post<void>(
      `${this.BASE}/api/watchlist/${mediaId}`, {},
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }

  removeFromWatchlist(mediaId: number): Observable<void> {
    const entry = this.getMediaEntry(this.me, mediaId);
    entry.inWatchlist = false;
    this.setMediaEntry(this.me, mediaId, entry);
    return this.http.delete<void>(
      `${this.BASE}/api/watchlist/${mediaId}`,
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }


  isInExternalWatchlistFromBackend(externalKey: string): Observable<boolean> {
    return this.http.get<WatchlistItem[]>(
      `${this.BASE}/api/watchlist`,
      { headers: this.headers() }
    ).pipe(
      map(list => list.some(w => String(w.mediaId) === String(externalKey))),
      catchError(() => of(this.getExtPersonal(this.me, externalKey)['inWatchlist'] === true))
    );
  }

  addToExternalWatchlist(externalKey: string, meta?: {
    title?: string; author?: string; genre?: string; imageUrl?: string;
    type?: string; description?: string; releaseYear?: number;
  }): Observable<void> {
    const existing = this.getExtPersonal(this.me, externalKey);
    this.setExtPersonal(this.me, externalKey, {
      ...existing,
      inWatchlist: true,
      title:       meta?.title       ?? existing['title']       ?? externalKey,
      author:      meta?.author      ?? existing['author']      ?? '',
      genre:       meta?.genre       ?? existing['genre']       ?? '',
      imageUrl:    meta?.imageUrl    ?? existing['imageUrl']    ?? '',
      type:        meta?.type        ?? existing['type']        ?? 'FILM',
      description: meta?.description ?? existing['description'] ?? '',
      releaseYear: meta?.releaseYear ?? existing['releaseYear'] ?? undefined,
      status:      'AVAILABLE',
    });
    return this.http.post<void>(
      `${this.BASE}/api/watchlist/${externalKey}`, {},
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }

  removeFromExternalWatchlist(externalKey: string): Observable<void> {
    const existing = this.getExtPersonal(this.me, externalKey);
    this.setExtPersonal(this.me, externalKey, { ...existing, inWatchlist: false });
    return this.http.delete<void>(
      `${this.BASE}/api/watchlist/${externalKey}`,
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }


  saveRating(mediaId: number, rating: number): void {
    const entry = this.getMediaEntry(this.me, mediaId);
    entry.userRating = rating;
    this.setMediaEntry(this.me, mediaId, entry);
  }


  loadComments(mediaId: number): Observable<Comment[]> {
    return this.http.get<any[]>(
      `${this.BASE}/api/comments`,
      { headers: this.headers(), params: { mediaId: String(mediaId) } }
    ).pipe(
      switchMap(comments => this.normalizeComments(comments)),
      catchError(() => of([]))
    );
  }

  postComment(mediaId: number, content: string, rating?: number, ownerId?: string, mediaTitle?: string): Observable<Comment> {
    const username = this.me;
    const body: any = { mediaId, content, rating, ownerId: ownerId || '0', mediaTitle: mediaTitle || '' };
    if (rating !== undefined) body.rating = rating;
    return this.http.post<any>(
      `${this.BASE}/api/comments`,
      body,
      { headers: this.headers({ 'X-Username': username }) }
    ).pipe(
      map(comment => ({ ...this.normalizeComment(comment), username })),
      catchError(() => of({
        id: Date.now(), 
        username, 
        userId: this.me,
        content, 
        rating,
        createdAt: new Date().toISOString(),
      } as Comment))
    );
  }

  deleteComment(commentId: number, _mediaId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.BASE}/api/comments/${commentId}`,
      { headers: this.headers() }
    ).pipe(catchError(() => of(undefined as void)));
  }


  loadExternalComments(externalKey: string): Observable<Comment[]> {
    return this.http.get<any[]>(
      `${this.BASE}/api/comments/external`,
      { headers: this.headers(), params: new HttpParams().set('externalKey', externalKey) }
    ).pipe(
      switchMap(comments => this.normalizeComments(comments)),
      catchError(() => of(this.getSharedComments(this.externalCommentsKey(externalKey))))
    );
  }

  postExternalComment(externalKey: string, content: string, rating?: number, ownerId?: string, mediaTitle?: string): Observable<Comment> {
    const username = this.me;
    const body: any = { 
      mediaId: externalKey, 
      content, 
      rating,
      ownerId: ownerId || '0',
      mediaTitle: mediaTitle || externalKey
    };
    if (rating !== undefined) body.rating = rating;
    return this.http.post<any>(
      `${this.BASE}/api/comments/external`,
      body,
      { headers: this.headers({ 'X-Username': username }) }
    ).pipe(
      map(comment => ({ ...this.normalizeComment(comment), username })),
      catchError(() => {
        const comment: Comment = {
          id: Date.now(), 
          username, 
          userId: this.me,
          content, 
          rating,
          createdAt: new Date().toISOString(),
        };
        const key = this.externalCommentsKey(externalKey);
        this.setSharedComments(key, [comment, ...this.getSharedComments(key)]);
        return of(comment);
      })
    );
  }

  deleteExternalComment(externalKey: string, commentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.BASE}/api/comments/external/${commentId}`,
      { headers: this.headers() }
    ).pipe(
      catchError(() => {
        const key = this.externalCommentsKey(externalKey);
        this.setSharedComments(key, this.getSharedComments(key).filter(c => c.id !== commentId));
        return of(undefined as void);
      })
    );
  }

  getExternalComments(externalKey: string): Comment[] {
    return this.getSharedComments(this.externalCommentsKey(externalKey));
  }
}