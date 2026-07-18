import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/guards/auth.guard';
import { UserRole } from './core/models/auth.models';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      }
    ]
  },
  {
    // All authenticated pages share the shell (sidebar + topbar)
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/shell/shell.component').then(m => m.ShellComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'decisions',
        loadComponent: () => import('./features/decisions/decision-list/decision-list.component').then(m => m.DecisionListComponent)
      },
      {
        path: 'decisions/new',
        loadComponent: () => import('./features/decisions/decision-new/decision-new.component').then(m => m.DecisionNewComponent)
      },
      {
        path: 'decisions/:id',
        loadComponent: () => import('./features/decisions/decision-detail/decision-detail.component').then(m => m.DecisionDetailComponent)
      },
      {
        path: 'comparaison',
        loadComponent: () => import('./features/comparaison/comparaison.component').then(m => m.ComparaisonComponent)
      },
      {
        path: 'validation',
        loadComponent: () => import('./features/validation/validation-queue.component').then(m => m.ValidationQueueComponent)
      },
      {
        path: 'admin/users',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR])],
        loadComponent: () => import('./features/admin/users/users-admin.component').then(m => m.UsersAdminComponent)
      }
    ]
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./features/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
  },
  {
    path: '**',
    redirectTo: '/'
  }
];
