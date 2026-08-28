import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Les pages derrière authGuard/roleGuard ne peuvent pas être prérendues : le garde
 * s'exécute sans session au build et fige une redirection vers /auth/login.
 */
export const serverRoutes: ServerRoute[] = [
  { path: 'dashboard', renderMode: RenderMode.Client },
  { path: 'dashboard/**', renderMode: RenderMode.Client },
  { path: 'decisions', renderMode: RenderMode.Client },
  { path: 'decisions/**', renderMode: RenderMode.Client },
  { path: 'validation', renderMode: RenderMode.Client },
  { path: 'validation/**', renderMode: RenderMode.Client },
  { path: 'comparaison', renderMode: RenderMode.Client },
  { path: 'comparaison/**', renderMode: RenderMode.Client },
  { path: 'audit', renderMode: RenderMode.Client },
  { path: 'audit/**', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];
