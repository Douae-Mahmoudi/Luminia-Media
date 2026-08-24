
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getToken();

  if (!token) return next(req);

  const headers: Record<string, string> = {
    Authorization: `Bearer ${token}`,
  };

  const userId = auth.getUserId();
  if (userId !== null && userId !== undefined && !isNaN(userId)) {
    headers['X-User-Id'] = String(userId);
  }

  return next(req.clone({ setHeaders: headers }));
};