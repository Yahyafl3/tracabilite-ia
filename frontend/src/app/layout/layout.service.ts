import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export interface LayoutConfig {
  preset: string;
  primary: string;
  darkTheme: boolean;
  menuMode: 'static' | 'overlay';
}

interface LayoutState {
  staticMenuDesktopInactive: boolean;
  overlayMenuActive: boolean;
  mobileMenuActive: boolean;
  configSidebarVisible: boolean;
}

/** Single source of truth for light/dark preference (also used by landing ThemeService). */
export const THEME_STORAGE_KEY = 'theme';

@Injectable({ providedIn: 'root' })
export class LayoutService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  readonly layoutConfig = signal<LayoutConfig>({
    preset: 'Aura',
    primary: 'indigo',
    darkTheme: false,
    menuMode: 'static',
  });

  readonly layoutState = signal<LayoutState>({
    staticMenuDesktopInactive: false,
    overlayMenuActive: false,
    mobileMenuActive: false,
    configSidebarVisible: false,
  });

  readonly isDarkTheme = computed(() => this.layoutConfig().darkTheme);
  readonly isOverlay = computed(() => this.layoutConfig().menuMode === 'overlay');
  readonly isMobileMenuOpen = computed(
    () => this.layoutState().mobileMenuActive || this.layoutState().overlayMenuActive,
  );

  constructor() {
    if (this.isBrowser) {
      const dark = this.resolveInitialDark();
      this.layoutConfig.update((c) => ({ ...c, darkTheme: dark }));
      this.applyDarkMode(dark);
    }
  }

  onMenuToggle(): void {
    if (this.isOverlay()) {
      this.layoutState.update((s) => ({ ...s, overlayMenuActive: !s.overlayMenuActive }));
      return;
    }
    if (this.isDesktop()) {
      this.layoutState.update((s) => ({
        ...s,
        staticMenuDesktopInactive: !s.staticMenuDesktopInactive,
      }));
    } else {
      this.layoutState.update((s) => ({ ...s, mobileMenuActive: !s.mobileMenuActive }));
    }
  }

  closeMobileMenu(): void {
    this.layoutState.update((s) => ({
      ...s,
      mobileMenuActive: false,
      overlayMenuActive: false,
    }));
  }

  toggleDarkMode(): void {
    this.setDarkTheme(!this.layoutConfig().darkTheme);
  }

  setDarkTheme(darkTheme: boolean): void {
    this.layoutConfig.update((c) => ({ ...c, darkTheme }));
    this.applyDarkMode(darkTheme);
    this.persistTheme(darkTheme);
  }

  toggleConfigSidebar(): void {
    this.layoutState.update((s) => ({ ...s, configSidebarVisible: !s.configSidebarVisible }));
  }

  hideConfigSidebar(): void {
    this.layoutState.update((s) => ({ ...s, configSidebarVisible: false }));
  }

  isDesktop(): boolean {
    return typeof window !== 'undefined' && window.innerWidth > 991;
  }

  private resolveInitialDark(): boolean {
    try {
      const stored = localStorage.getItem(THEME_STORAGE_KEY);
      if (stored === 'dark') return true;
      if (stored === 'light') return false;
    } catch {
      // ignore storage errors
    }
    return (
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-color-scheme: dark)').matches
    );
  }

  private persistTheme(darkTheme: boolean): void {
    if (!this.isBrowser) return;
    try {
      localStorage.setItem(THEME_STORAGE_KEY, darkTheme ? 'dark' : 'light');
    } catch {
      // ignore storage errors
    }
  }

  private applyDarkMode(darkTheme: boolean): void {
    if (!this.isBrowser || typeof document === 'undefined') {
      return;
    }
    document.documentElement.classList.toggle('app-dark', darkTheme);
    document.documentElement.setAttribute('data-theme', darkTheme ? 'dark' : 'light');
  }
}
