import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';

export const routes: Routes = [
  // This tells Angular: When the URL is /login, load the LoginComponent
  {
    path: 'login',
    component: LoginComponent
  },

  // Wildcard fallback: If they type a random URL, send them to login for now
  { path: '**', redirectTo: 'login' },
];
