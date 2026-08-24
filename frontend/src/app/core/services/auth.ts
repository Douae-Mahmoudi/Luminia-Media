
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ForgotPasswordRequest,
  VerifyCodeRequest,
  ResetPasswordRequest,
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = 'http://localhost:8081/auth';

  constructor(private http: HttpClient, private router: Router) {}

  register(body: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/register`, body).pipe(
      map((res) => {
        if (!res.token) throw new Error(res.message || 'Erreur inscription');
        this.saveSession(res);
        return res;
      })
    );
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/login`, body).pipe(
      map((res) => {
        if (!res.token) throw new Error(res.message || 'Erreur connexion');
        this.saveSession(res);
        return res;
      })
    );
  }

  forgotPassword(body: ForgotPasswordRequest): Observable<string> {
    return this.http.post(`${this.API}/forgot-password`, body, { responseType: 'text' });
  }

  verifyCode(body: VerifyCodeRequest): Observable<string> {
    return this.http.post(`${this.API}/verify-code`, body, { responseType: 'text' });
  }

  resetPassword(body: ResetPasswordRequest): Observable<string> {
    return this.http.post(`${this.API}/reset-password`, body, { responseType: 'text' });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  getUserId(): number | null {
    const cached = localStorage.getItem('userId');
    if (cached) return Number(cached);

    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const id = payload.id ?? payload.userId ?? payload.sub ?? null;
      if (id) localStorage.setItem('userId', String(id));
      return id ? Number(id) : null;
    } catch {
      return null;
    }
  }

  getUsername(): string | null {
    const cached = localStorage.getItem('username');
    if (cached) return cached;

    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const username = payload.sub ?? payload.username ?? payload.email ?? null;
      if (username) localStorage.setItem('username', username);
      return username;
    } catch {
      return null;
    }
  }
  

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  private saveSession(res: AuthResponse): void {
    localStorage.setItem('token', res.token!);
    localStorage.setItem('role', res.role!);

    try {
      const payload = JSON.parse(atob(res.token!.split('.')[1]));

      const username = payload.sub ?? payload.username ?? payload.email ?? null;
      if (username) localStorage.setItem('username', username);

      const id = payload.id ?? payload.userId ?? null;
      if (id) localStorage.setItem('userId', String(id));
    } catch {
    }
  }
}