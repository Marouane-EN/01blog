import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';

/**
 * Protects routes that require authentication.
 * Redirects to /login and preserves the attempted URL as returnUrl.
 */
export const authGuard: CanActivateFn = (route) => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  if (authStore.isAuthenticated()) {
    return true;
  }

  const returnUrl = route.url.map((s) => s.toString()).join('/');
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: '/' + returnUrl },
  });
};
