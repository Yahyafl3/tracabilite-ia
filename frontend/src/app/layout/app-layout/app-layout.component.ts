import { Component, computed, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { LayoutService } from '../layout.service';
import { AppTopbarComponent } from '../app-topbar/app-topbar.component';
import { AppSidebarComponent } from '../app-sidebar/app-sidebar.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, AppTopbarComponent, AppSidebarComponent],
  templateUrl: './app-layout.component.html',
  styleUrl: './app-layout.component.scss',
})
export class AppLayoutComponent {
  readonly layoutService = inject(LayoutService);

  readonly containerClass = computed(() => {
    const config = this.layoutService.layoutConfig();
    const state = this.layoutService.layoutState();
    return {
      'layout-static': config.menuMode === 'static',
      'layout-overlay': config.menuMode === 'overlay',
      'layout-static-inactive': state.staticMenuDesktopInactive && config.menuMode === 'static',
      'layout-overlay-active': state.overlayMenuActive,
      'layout-mobile-active': state.mobileMenuActive,
    };
  });

  constructor() {
    effect(() => {
      const mobile = this.layoutService.layoutState().mobileMenuActive;
      if (typeof document === 'undefined') {
        return;
      }
      document.body.classList.toggle('layout-blocked-scroll', mobile);
    });
  }
}
