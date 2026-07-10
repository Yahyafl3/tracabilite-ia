import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Theme = 'light' | 'dark';

/**
 * Gestion du thème clair / sombre.
 * - Persiste le choix dans localStorage.
 * - Retombe sur la préférence système (prefers-color-scheme) par défaut.
 * - Compatible SSR (aucun accès au DOM côté serveur).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly storageKey = 'theme';

  readonly theme = signal<Theme>('light');

  constructor() {
    if (this.isBrowser) {
      const initial = this.resolveInitial();
      this.theme.set(initial);
      this.apply(initial);
    }
  }

  toggle(): void {
    this.set(this.theme() === 'dark' ? 'light' : 'dark');
  }

  set(theme: Theme): void {
    this.theme.set(theme);
    if (this.isBrowser) {
      localStorage.setItem(this.storageKey, theme);
      this.apply(theme);
    }
  }

  private resolveInitial(): Theme {
    const stored = localStorage.getItem(this.storageKey);
    if (stored === 'light' || stored === 'dark') {
      return stored;
    }
    const prefersDark =
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-color-scheme: dark)').matches;
    return prefersDark ? 'dark' : 'light';
  }

  private apply(theme: Theme): void {
    document.documentElement.setAttribute('data-theme', theme);
  }
}
