import { Routes } from '@angular/router';

export const routes: Routes = [
  // ── Home (the page we just built) ────────────────────────────────────
  {
    path: '',
    loadComponent: () =>
      import('./features/home/home-page.component').then((m) => m.HomePageComponent),
  },

  // ── Auth feature (your existing components — paths match your folder layout) ──
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
    // Your existing OAuth2 redirect handler
    path: 'oauth2/redirect',
    loadComponent: () =>
      import('./features/auth/oauth2-redirect/oauth2-redirect.component').then(
        (m) => m.Oauth2RedirectComponent,
      ),
  },

  // ── Future pages (lazy-loaded, add as you build them) ────────────────
  // {
  //   path: 'posts/:slug',
  //   loadComponent: () =>
  //     import('./features/post-detail/post-detail.component').then((m) => m.PostDetailComponent),
  // },
  // {
  //   path: 'profile/:username',
  //   loadComponent: () =>
  //     import('./features/profile/profile.component').then((m) => m.ProfileComponent),
  // },
  // {
  //   path: 'create-post',
  //   loadComponent: () =>
  //     import('./features/create-post/create-post.component').then((m) => m.CreatePostComponent),
  // },
  // {
  //   path: 'search',
  //   loadComponent: () =>
  //     import('./features/search/search.component').then((m) => m.SearchComponent),
  // },
  // {
  //   path: 't/:tag',
  //   loadComponent: () =>
  //     import('./features/tag-feed/tag-feed.component').then((m) => m.TagFeedComponent),
  // },

  // // ── Catch-all 404 ────────────────────────────────────────────────────
  // {
  //   path: '**',
  //   loadComponent: () =>
  //     import('./features/not-found/not-found.component').then((m) => m.NotFoundComponent),
  // },
];
