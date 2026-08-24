import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserProfileDto } from '../models/auth.models';
import { MediaResponse, UpdateMediaRequest, MediaStatus } from '../models/media.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly ADMIN_API = 'http://localhost:8081/admin';
  private readonly MEDIA_API = 'http://localhost:8082/api/admin/media';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<UserProfileDto[]> {
    return this.http.get<UserProfileDto[]>(`${this.ADMIN_API}/users`);
  }

  updateUserRole(id: number, role: string): Observable<UserProfileDto> {
    return this.http.patch<UserProfileDto>(`${this.ADMIN_API}/users/${id}/role`, null, {
      params: { role },
    });
  }

  getAllMedia(): Observable<MediaResponse[]> {
    return this.http.get<MediaResponse[]>(`${this.MEDIA_API}`);
  }

  getPendingMedia(): Observable<MediaResponse[]> {
    return this.http.get<MediaResponse[]>(`${this.MEDIA_API}/pending`);
  }

  approveMedia(id: number): Observable<MediaResponse> {
    return this.http.put<MediaResponse>(`${this.MEDIA_API}/${id}/approve`, {});
  }

  rejectMedia(id: number): Observable<MediaResponse> {
    return this.http.put<MediaResponse>(`${this.MEDIA_API}/${id}/reject`, {});
  }

  updateMedia(id: number, request: UpdateMediaRequest): Observable<MediaResponse> {
    return this.http.put<MediaResponse>(`${this.MEDIA_API}/${id}`, request);
  }

  deleteMedia(id: number): Observable<void> {
    return this.http.delete<void>(`${this.MEDIA_API}/${id}`);
  }
}