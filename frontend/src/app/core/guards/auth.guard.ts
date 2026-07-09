import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { AuthStore } from '../store/auth.store';

// Notice we added UrlTree to the return type
export const authGuard: CanActivateFn = (route, state): boolean | UrlTree => {
  const platformId = inject(PLATFORM_ID);
  const authStore = inject(AuthStore);
  const router = inject(Router);

  // 1. Server-Blind Bypass
  if (!isPlatformBrowser(platformId)) {
    return true; 
  }

  // 2. Browser confirms authentication
  if (authStore.isAuthenticated()) {
    return true;
  }

  // 3. THE FIX: Return a UrlTree instead of false!
  // This physically forces the browser to dump the current page and redirect.
  console.warn('[AuthGuard] Unauthorized access detected. Redirecting to login.');
  return router.createUrlTree(['/login']);
};