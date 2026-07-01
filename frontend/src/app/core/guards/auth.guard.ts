import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const authService = inject(AuthService);
  const router = inject(Router);

  // If the user is logged in, allow them to pass
  if (authStore.isAuthenticated() || authService.hasToken()) {
    return true;
  }

  // Otherwise, kick them back to the login page safely
  return router.parseUrl('/login');
};
