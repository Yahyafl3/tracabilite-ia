import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { TranslationService } from '../../core/i18n/translation.service';
import { LayoutService } from '../layout.service';
import { AppConfiguratorComponent } from '../app-configurator/app-configurator.component';
import { LanguageSwitcherComponent } from '../language-switcher/language-switcher.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe, AppConfiguratorComponent, LanguageSwitcherComponent],
  templateUrl: './app-topbar.component.html',
  styleUrl: './app-topbar.component.scss',
})
export class AppTopbarComponent {
  readonly layoutService = inject(LayoutService);
  readonly i18n = inject(TranslationService);
  private readonly authService = inject(AuthService);

  get currentUser() {
    return this.authService.currentUser;
  }

  get userInitials(): string {
    const u = this.currentUser;
    if (!u) return 'U';
    const name = (u as { nom?: string; name?: string }).nom ?? (u as { name?: string }).name ?? '';
    return name ? name.charAt(0).toUpperCase() : (u.email?.charAt(0).toUpperCase() ?? 'U');
  }

  get userName(): string {
    const u = this.currentUser;
    if (!u) return '';
    return (u as { nom?: string }).nom ?? u.email ?? '';
  }

  readonly userRoleLabel = computed(() => {
    const role = this.currentUser?.role;
    if (!role) {
      return '';
    }
    this.i18n.currentLang();
    const key = `roles.${role}`;
    const label = this.i18n.t(key);
    return label === key ? String(role) : label;
  });

  logout(): void {
    this.authService.logout();
  }
}
