import { Injectable, computed, effect, signal } from '@angular/core';

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

@Injectable({ providedIn: 'root' })
export class LayoutService {
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

  private initialized = false;

  constructor() {
    effect(() => {
      const config = this.layoutConfig();
      if (!this.initialized) {
        this.initialized = true;
        this.applyDarkMode(config);
        return;
      }
      this.applyDarkMode(config);
    });
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
    this.layoutConfig.update((c) => ({ ...c, darkTheme: !c.darkTheme }));
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

  private applyDarkMode(config: LayoutConfig): void {
    if (typeof document === 'undefined') {
      return;
    }
    document.documentElement.classList.toggle('app-dark', config.darkTheme);
  }
}
