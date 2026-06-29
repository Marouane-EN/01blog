import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from '../store/auth.store';

export const authGuard: CanActivateFn = () => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  // If the user is logged in, allow them to pass
  if (authStore.isAuthenticated()) {
    return true;
  }

  // Otherwise, kick them back to the login page safely
  return router.parseUrl('/login');
};
