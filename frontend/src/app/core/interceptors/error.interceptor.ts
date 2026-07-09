import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 1. Catch 401 Unauthorized or 403 Forbidden errors
      if (error.status === 401 || error.status === 403) {
        let isActivelyLoggingIn = false;

        // 2. Safely check the URL (SSR Safe)
        if (typeof window !== 'undefined') {
          const currentUrl = window.location.href;
          // Check if they are on the OAuth redirect page or if the URL has a token
          isActivelyLoggingIn = currentUrl.includes('token=') || currentUrl.includes('oauth2');
        }

        // 3. Apply your exact logic: Skip the redirect if they are logging in!
        if (!isActivelyLoggingIn) {
          console.warn('[Interceptor] Invalid token detected. Forcing logout.');
          authService.logout(); // Clears localStorage
          router.navigate(['/login']);
        } else {
          console.log('[Interceptor] 401 ignored because user is actively authenticating.');
        }
      }

      // Pass the error back so the application doesn't freeze
      return throwError(() => error);
    }),
  );
};
