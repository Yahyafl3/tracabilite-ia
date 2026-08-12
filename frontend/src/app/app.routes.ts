import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/guards/auth.guard';
import { UserRole } from './core/models/auth.models';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'support',
    loadComponent: () =>
      import('./features/support/support.component').then((m) => m.SupportComponent),
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
        path: 'forgot-password',
        loadComponent: () =>
          import('./features/auth/forgot-password/forgot-password.component').then(
            (m) => m.ForgotPasswordComponent,
          ),
      },
      {
        path: 'reset-password',
        loadComponent: () =>
          import('./features/auth/reset-password/reset-password.component').then(
            (m) => m.ResetPasswordComponent,
          ),
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
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/dashboard/dashboard-router.component').then((m) => m.DashboardRouterComponent),
      },
      {
        path: 'dashboard/admin',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR])],
        loadComponent: () =>
          import('./features/dashboard/admin-dashboard/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
      },
      {
        path: 'dashboard/agent-credit',
        canActivate: [roleGuard([UserRole.AGENT_CREDIT])],
        loadComponent: () =>
          import('./features/dashboard/agent-credit-dashboard/agent-credit-dashboard.component').then((m) => m.AgentCreditDashboardComponent),
      },
      {
        path: 'dashboard/agent-sante',
        canActivate: [roleGuard([UserRole.AGENT_SANTE])],
        loadComponent: () =>
          import('./features/dashboard/agent-sante-dashboard/agent-sante-dashboard.component').then((m) => m.AgentSanteDashboardComponent),
      },
      {
        path: 'dashboard/agent-pedagogique',
        canActivate: [roleGuard([UserRole.AGENT_PEDAGOGIQUE])],
        loadComponent: () =>
          import('./features/dashboard/agent-pedagogique-dashboard/agent-pedagogique-dashboard.component').then((m) => m.AgentPedagogiqueDashboardComponent),
      },
      {
        path: 'dashboard/validateur',
        canActivate: [roleGuard([UserRole.VALIDATEUR])],
        loadComponent: () =>
          import('./features/dashboard/validateur-dashboard/validateur-dashboard.component').then((m) => m.ValidateurDashboardComponent),
      },
      {
        path: 'dashboard/responsable-credit',
        canActivate: [roleGuard([UserRole.RESPONSABLE_CREDIT])],
        loadComponent: () =>
          import('./features/dashboard/responsable-credit-dashboard/responsable-credit-dashboard.component').then((m) => m.ResponsableCreditDashboardComponent),
      },
      {
        path: 'dashboard/professionnel-sante',
        canActivate: [roleGuard([UserRole.PROFESSIONNEL_SANTE])],
        loadComponent: () =>
          import('./features/dashboard/professionnel-sante-dashboard/professionnel-sante-dashboard.component').then((m) => m.ProfessionnelSanteDashboardComponent),
      },
      {
        path: 'dashboard/responsable-pedagogique',
        canActivate: [roleGuard([UserRole.RESPONSABLE_PEDAGOGIQUE])],
        loadComponent: () =>
          import('./features/dashboard/responsable-pedagogique-dashboard/responsable-pedagogique-dashboard.component').then((m) => m.ResponsablePedagogiqueDashboardComponent),
      },
      {
        path: 'dashboard/auditeur',
        canActivate: [roleGuard([UserRole.AUDITEUR])],
        loadComponent: () =>
          import('./features/dashboard/auditeur-dashboard/auditeur-dashboard.component').then((m) => m.AuditeurDashboardComponent),
      },
      {
        path: 'decisions',
        canActivate: [
          roleGuard([
            UserRole.ADMINISTRATEUR,
            UserRole.UTILISATEUR,
            UserRole.AUDITEUR,
            UserRole.VALIDATEUR,
            UserRole.RESPONSABLE_CREDIT,
            UserRole.PROFESSIONNEL_SANTE,
            UserRole.RESPONSABLE_PEDAGOGIQUE,
            UserRole.AGENT_CREDIT,
            UserRole.AGENT_SANTE,
            UserRole.AGENT_PEDAGOGIQUE,
          ]),
        ],
        loadComponent: () =>
          import('./features/decisions/decision-list/decision-list.component').then(
            (m) => m.DecisionListComponent,
          ),
      },
      {
        path: 'decisions/new',
        canActivate: [roleGuard([
          UserRole.ADMINISTRATEUR,
          UserRole.UTILISATEUR,
          UserRole.AGENT_CREDIT,
          UserRole.AGENT_SANTE,
          UserRole.AGENT_PEDAGOGIQUE,
        ])],
        loadComponent: () =>
          import('./features/decisions/decision-new/decision-new.component').then(
            (m) => m.DecisionNewComponent,
          ),
      },
      {
        path: 'decisions/:id',
        canActivate: [
          roleGuard([
            UserRole.ADMINISTRATEUR,
            UserRole.VALIDATEUR,
            UserRole.UTILISATEUR,
            UserRole.AUDITEUR,
            UserRole.RESPONSABLE_CREDIT,
            UserRole.PROFESSIONNEL_SANTE,
            UserRole.RESPONSABLE_PEDAGOGIQUE,
            UserRole.AGENT_CREDIT,
            UserRole.AGENT_SANTE,
            UserRole.AGENT_PEDAGOGIQUE,
          ]),
        ],
        loadComponent: () =>
          import('./features/decisions/decision-detail/decision-detail.component').then(
            (m) => m.DecisionDetailComponent,
          ),
      },
      {
        path: 'comparaison',
        canActivate: [
          roleGuard([UserRole.ADMINISTRATEUR, UserRole.VALIDATEUR, UserRole.AUDITEUR]),
        ],
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
        canActivate: [
          roleGuard([
            UserRole.ADMINISTRATEUR,
            UserRole.VALIDATEUR,
            UserRole.RESPONSABLE_CREDIT,
            UserRole.PROFESSIONNEL_SANTE,
            UserRole.RESPONSABLE_PEDAGOGIQUE,
          ]),
        ],
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
      {
        path: 'admin/support',
        canActivate: [roleGuard([UserRole.ADMINISTRATEUR])],
        loadComponent: () =>
          import('./features/admin/support/support-admin.component').then(
            (m) => m.SupportAdminComponent,
          ),
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
