import { Routes } from '@angular/router';
import { AdminLayoutComponent } from '../../core/layout/admin-layout/admin-layout.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./reports/reports.component').then((m) => m.ReportsComponent),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./manage-users/manage-users.component').then((m) => m.ManageUsersComponent),
      },
      {
        path: 'posts',
        loadComponent: () =>
          import('./manage-posts/manage-posts.component').then((m) => m.ManagePostsComponent),
      },
    ],
  },
];
