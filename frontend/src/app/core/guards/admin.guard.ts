import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { decodeJwtPayload } from '../utils/jwt.utils';

interface JwtPayload {
  role?: string;
}

export const adminGuard: CanMatchFn = (): boolean | UrlTree => {
  const platformId = inject(PLATFORM_ID);
  const authService = inject(AuthService);
  const router = inject(Router);

  // Server-side: nothing to check against, let the client-side guard re-verify.
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const token = authService.getToken();
  const payload = token ? decodeJwtPayload<JwtPayload>(token) : null;
  const roles = payload?.role?.split(',') ?? [];

  if (roles.includes('ROLE_ADMIN')) {
    return true;
  }

  console.warn('[AdminGuard] Unauthorized access detected. Redirecting to home.');
  return router.createUrlTree(['/']);
};
