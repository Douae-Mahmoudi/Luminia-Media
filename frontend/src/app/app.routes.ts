import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';

export const routes: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./features/home/home').then(m => m.HomeComponent),
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register').then(m => m.RegisterComponent),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password').then(m => m.ForgotPasswordComponent),
  },

  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard-user/dashboard-user').then(m => m.DashboardUserComponent),
  },

  {
    path: 'watchlist',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/watchlist/watchlist.component').then(m => m.WatchlistComponent),
  },

  {
    path: 'favorites',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/favorites/favorites.component').then(m => m.FavoritesComponent),
  },

  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile.component').then(m => m.ProfileComponent),
  },

  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/notifications/notifications').then(m => m.NotificationsComponent),
  },

  {
    path: 'collections',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/collections/collections.component').then(m => m.CollectionsComponent),
  },
  {
    path: 'collections/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/collections/collection-detail/collection-detail.component')
        .then(m => m.CollectionDetailComponent),
  },

  {
    path: 'media/external',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/media/media-detail/media-detail.component').then(m => m.MediaDetailComponent),
  },
  {
    path: 'media/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/media/media-detail/media-detail.component').then(m => m.MediaDetailComponent),
  },

  {
    path: 'admin/dashboard',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard-admin/dashboard-admin').then(m => m.DashboardAdminComponent),
  },

  {
    path: 'my-media',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/my-media/my-media.component').then(m => m.MyMediaComponent),
  },

  { path: '**', redirectTo: '' },
];