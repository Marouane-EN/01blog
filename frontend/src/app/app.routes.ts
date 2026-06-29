import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // ── 1. App Shell (Pages that HAVE a Navbar and Footer) ──
  {
    path: '',
    component: MainLayoutComponent, // The wrapper!
    children: [
      {
        path: '', // Root URL (Home Page)
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/home/home-page.component').then((m) => m.HomePageComponent),
      },
      // Future pages like 'posts/:slug' or 'profile/:username' will go here
    ],
  },

  // ── 2. Standalone Pages (No Navbar - usually Auth) ──
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'oauth2/redirect',
    loadComponent: () =>
      import('./features/auth/oauth2-redirect/oauth2-redirect.component').then(
        (m) => m.Oauth2RedirectComponent,
      ),
  },

  // Catch-all route (fallback)
  { path: '**', redirectTo: '' },
];
