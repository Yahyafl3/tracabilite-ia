import { Injectable, computed, inject } from '@angular/core';
import { LayoutService } from '../layout/layout.service';

export type Theme = 'light' | 'dark';

/**
 * Thin facade for landing / marketing pages.
 * Dark preference is owned by LayoutService (localStorage + .app-dark + data-theme).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly layout = inject(LayoutService);

  readonly theme = computed<Theme>(() => (this.layout.isDarkTheme() ? 'dark' : 'light'));

  toggle(): void {
    this.layout.toggleDarkMode();
  }

  set(theme: Theme): void {
    this.layout.setDarkTheme(theme === 'dark');
  }
}
