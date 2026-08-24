import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, interval, Subscription, firstValueFrom } from 'rxjs';
import { distinctUntilChanged, map, startWith } from 'rxjs/operators';
import { AuthService } from './auth';

export interface NotificationResponse {
  id: number; userId: number; type: string; status: string;
  message: string; referenceId: number; referenceType: string;
  createdAt: string; readAt: string; read: boolean;
}
export interface BadgeCountResponse { count: number; }
export interface PageResponse<T> {
  content: T[]; totalElements: number;
  totalPages: number; number: number; size: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {

  private readonly API      = 'http://localhost:8086/api/notifications';
  private readonly AUTH_API = 'http://localhost:8081/auth';

  private numericUserId: string | null = null;
  private lastSeenToken: string | null = null;

  readonly userChanged$ = new Subject<void>();

  private tokenWatchSub: Subscription;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
  ) {
   
    this.tokenWatchSub = interval(500).pipe(
      startWith(0),
      map(() => this.auth.getToken()),
      distinctUntilChanged(),
    ).subscribe(token => {
      const tokenChanged = this.lastSeenToken !== undefined && token !== this.lastSeenToken;
      this.lastSeenToken = token;

      if (tokenChanged) {
        this.invalidateCache();
        this.userChanged$.next(); 
      }
    });
  }

  ngOnDestroy(): void {
    this.tokenWatchSub.unsubscribe();
    this.userChanged$.complete();
  }

  invalidateCache(): void {
    this.numericUserId = null;
  }


  private async resolveNumericUserId(): Promise<string> {
    if (this.numericUserId) return this.numericUserId;

    const token = this.auth.getToken();
    if (!token) return '0';

    try {
      const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
      const profile = await firstValueFrom(
        this.http.get<{ id: number }>(`${this.AUTH_API}/me`, { headers })
      );
      this.numericUserId = String(profile.id);
      return this.numericUserId;
    } catch {
      return '0';
    }
  }

  private async buildHeaders(): Promise<HttpHeaders> {
    const numericId = await this.resolveNumericUserId();
    const token     = this.auth.getToken() ?? '';
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'X-User-Id':   numericId,
    });
  }


  async getBadgeCountAsync(): Promise<BadgeCountResponse> {
    try {
      const headers = await this.buildHeaders();
      return await firstValueFrom(
        this.http.get<BadgeCountResponse>(`${this.API}/badge`, { headers })
      );
    } catch { return { count: 0 }; }
  }

  getBadgeCount(): Observable<BadgeCountResponse> {
    return new Observable(o => {
      this.getBadgeCountAsync()
        .then(r => { o.next(r); o.complete(); })
        .catch(e => o.error(e));
    });
  }

  async getNotificationsAsync(page = 0, size = 20): Promise<PageResponse<NotificationResponse>> {
    try {
      const headers = await this.buildHeaders();
      return await firstValueFrom(
        this.http.get<PageResponse<NotificationResponse>>(
          `${this.API}?page=${page}&size=${size}&sort=createdAt,desc`, { headers }
        )
      );
    } catch {
      return { content: [], totalElements: 0, totalPages: 0, number: 0, size: 0 };
    }
  }

  getNotifications(page = 0, size = 20): Observable<PageResponse<NotificationResponse>> {
    return new Observable(o => {
      this.getNotificationsAsync(page, size)
        .then(r => { o.next(r); o.complete(); })
        .catch(e => o.error(e));
    });
  }

  async markOneAsReadAsync(id: number): Promise<void> {
    try {
      const headers = await this.buildHeaders();
      await firstValueFrom(
        this.http.patch<void>(`${this.API}/${id}/read`, {}, { headers })
      );
    } catch { }
  }

  markOneAsRead(id: number): Observable<void> {
    return new Observable(o => {
      this.markOneAsReadAsync(id)
        .then(() => { o.next(); o.complete(); })
        .catch(e => o.error(e));
    });
  }

  async markAllAsReadAsync(): Promise<void> {
    try {
      const headers = await this.buildHeaders();
      await firstValueFrom(
        this.http.patch<void>(`${this.API}/read-all`, {}, { headers })
      );
    } catch {  }
  }

  markAllAsRead(): Observable<void> {
    return new Observable(o => {
      this.markAllAsReadAsync()
        .then(() => { o.next(); o.complete(); })
        .catch(e => o.error(e));
    });
  }
}