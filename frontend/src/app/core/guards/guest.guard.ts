import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';
import { AuthService } from '../services/auth.service';

/**
 * Blocks authenticated users from visiting guest-only pages (login, register).
 * Redirects to home if they are already signed in.
 */
export const guestGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authStore.isAuthenticated() || authService.hasToken()) {
    return router.createUrlTree(['/']);
  }

  return true;
};
