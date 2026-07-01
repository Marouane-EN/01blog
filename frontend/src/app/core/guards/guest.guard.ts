import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';
import { AuthService } from '../services/auth.service';
import { isPlatformBrowser } from '@angular/common';

/**
 * Blocks authenticated users from visiting guest-only pages (login, register).
 * Redirects to home if they are already signed in.
 */
export const guestGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // If we are on the server, we have to let it pass because we can't check localStorage
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authStore.isAuthenticated() || authService.hasToken()) {
    router.navigate(['/']);
    return false;
  }

  return true;
};
