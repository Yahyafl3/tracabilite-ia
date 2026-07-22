import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/guards/auth.guard';
import { UserRole } from './core/models/auth.models';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/app-layout/app-layout.component').then((m) => m.AppLayoutComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'decisions',
      },
      {
        path: 'dashboard',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR, UserRole.UTILISATEUR])],
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'decisions',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR, UserRole.UTILISATEUR])],
        loadComponent: () =>
          import('./features/decisions/decision-list/decision-list.component').then(
            (m) => m.DecisionListComponent,
          ),
      },
      {
        path: 'decisions/new',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR, UserRole.UTILISATEUR])],
        loadComponent: () =>
          import('./features/decisions/decision-new/decision-new.component').then(
            (m) => m.DecisionNewComponent,
          ),
      },
      {
        path: 'decisions/:id',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR, UserRole.VALIDATEUR, UserRole.UTILISATEUR])],
        loadComponent: () =>
          import('./features/decisions/decision-detail/decision-detail.component').then(
            (m) => m.DecisionDetailComponent,
          ),
      },
      {
        path: 'comparaison',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR, UserRole.UTILISATEUR])],
        loadComponent: () =>
          import('./features/comparaison/comparaison.component').then((m) => m.ComparaisonComponent),
      },
      {
        path: 'audit',
        canActivate: [roleGuard([UserRole.AUDITEUR, UserRole.ADMINISTRATEUR])],
        loadComponent: () =>
          import('./features/audit/audit-page.component').then((m) => m.AuditPageComponent),
      },
      {
        path: 'validation',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR, UserRole.VALIDATEUR])],
        loadComponent: () =>
          import('./features/validation/validation-queue.component').then(
            (m) => m.ValidationQueueComponent,
          ),
      },
      {
        path: 'admin/users',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR])],
        loadComponent: () =>
          import('./features/admin/users/users-admin.component').then((m) => m.UsersAdminComponent),
      },
      {
        path: 'admin/groq',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR])],
        loadComponent: () =>
          import('./features/admin/groq/groq-admin.component').then((m) => m.GroqAdminComponent),
      },
    ],
  },
  {
    path: '403',
    loadComponent: () =>
      import('./features/system/system-page.component').then((m) => m.SystemPageComponent),
    data: {
      code: '403',
      title: 'Accès refusé',
      message: 'Vous n’avez pas les permissions nécessaires pour accéder à cette ressource.',
      severity: 'danger',
    },
  },
  {
    path: '404',
    loadComponent: () =>
      import('./features/system/system-page.component').then((m) => m.SystemPageComponent),
    data: {
      code: '404',
      title: 'Page introuvable',
      message: 'La page demandée n’existe pas ou a été déplacée.',
      severity: 'warn',
    },
  },
  {
    path: '500',
    loadComponent: () =>
      import('./features/system/system-page.component').then((m) => m.SystemPageComponent),
    data: {
      code: '500',
      title: 'Erreur serveur',
      message: 'Une erreur inattendue est survenue. Réessayez plus tard ou contactez un administrateur.',
      severity: 'danger',
    },
  },
  {
    path: 'unauthorized',
    redirectTo: '403',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: '404',
  },
];
