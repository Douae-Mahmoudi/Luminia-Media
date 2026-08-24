 
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth';
 
 
export type NotificationType =
  | 'LIKE' | 'COMMENT' | 'FAVORITE' | 'WATCHLIST'
  | 'MEDIA_APPROVED' | 'MEDIA_REJECTED' | 'BROADCAST' | 'SYSTEM';
 
export type NotificationStatus = 'UNREAD' | 'READ';
export type ReferenceType      = 'MEDIA' | 'COMMENT' | 'USER' | 'SYSTEM';
 
export interface AdminNotificationResponse {
  id:            number;
  userId:        number;
  type:          NotificationType;
  status:        NotificationStatus;
  message:       string;
  referenceId:   number;
  referenceType: ReferenceType;
  createdAt:     string;
  readAt?:       string;
}
 
export interface NotificationStatsResponse {
  totalCount:      number;
  readCount:       number;
  unreadCount:     number;
  openRatePercent: number;
  countByType:     Record<string, number>;
}
 
export interface PageResponse<T> {
  content:       T[];
  currentPage:   number;
  totalPages:    number;
  totalElements: number;
  last:          boolean;
}
 
export interface BroadcastRequest {
  message: string;
  type?:   NotificationType;
}
 
@Injectable({ providedIn: 'root' })
export class AdminNotificationService {
 
  private readonly API = 'http://localhost:8086/api/admin/notifications';
 
  constructor(private http: HttpClient, private auth: AuthService) {}
 
  private get headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` });
  }
 
  getAll(params: {
    userId?:  number;
    type?:    NotificationType;
    from?:    string;
    to?:      string;
    page?:    number;
    size?:    number;
  } = {}): Observable<PageResponse<AdminNotificationResponse>> {
    let p = new HttpParams();
    if (params.userId) p = p.set('userId',  String(params.userId));
    if (params.type)   p = p.set('type',    params.type);
    if (params.from)   p = p.set('from',    params.from);
    if (params.to)     p = p.set('to',      params.to);
    p = p.set('page', String(params.page ?? 0));
    p = p.set('size', String(params.size ?? 30));
 
    return this.http.get<PageResponse<AdminNotificationResponse>>(
      this.API, { headers: this.headers, params: p }
    ).pipe(catchError(() => of({
      content: [], currentPage: 0, totalPages: 0, totalElements: 0, last: true,
    })));
  }
 
  getStats(): Observable<NotificationStatsResponse> {
    return this.http.get<NotificationStatsResponse>(
      `${this.API}/stats`, { headers: this.headers }
    ).pipe(catchError(() => of({
      totalCount: 0, readCount: 0, unreadCount: 0,
      openRatePercent: 0, countByType: {},
    })));
  }
 
  broadcast(request: BroadcastRequest): Observable<void> {
    return this.http.post<void>(
      `${this.API}/broadcast`, request, { headers: this.headers }
    ).pipe(catchError(() => of(undefined as any)));
  }
}
