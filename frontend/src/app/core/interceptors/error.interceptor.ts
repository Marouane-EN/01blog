import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 1. Catch 401 Unauthorized — a missing/expired/invalid token.
      // (403 is deliberately excluded: it means "authenticated but not
      // allowed" for a specific action — e.g. reporting or banning an
      // admin — and should surface as an error, not force a logout.)
      if (error.status === 401) {
        let isActivelyLoggingIn = false;

        // 2. Safely check the URL (SSR Safe)
        if (typeof window !== 'undefined') {
          const currentUrl = window.location.href;
          // Check if they are on the OAuth redirect page or if the URL has a token
          isActivelyLoggingIn = currentUrl.includes('token=') || currentUrl.includes('oauth2');
        }

        // 3. Apply your exact logic: Skip the redirect if they are logging in!
        if (!isActivelyLoggingIn) {
          const hadToken = authService.hasToken();
          authService.logout(); // Clears localStorage
          router.navigate(['/login']);

          if (hadToken) {
            toastService.error('Your session has expired. Please log in again.');
          }
        }
      }

      // Pass the error back so the application doesn't freeze
      return throwError(() => error);
    }),
  );
};
