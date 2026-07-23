import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LayoutService } from '../layout.service';
import { AppConfiguratorComponent } from '../app-configurator/app-configurator.component';
import { roleLabel } from '../../core/utils/label.util';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterLink, AppConfiguratorComponent],
  templateUrl: './app-topbar.component.html',
  styleUrl: './app-topbar.component.scss',
})
export class AppTopbarComponent {
  readonly layoutService = inject(LayoutService);
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

  get userRoleLabel(): string {
    const role = this.currentUser?.role;
    return role ? roleLabel(String(role)) : '';
  }

  logout(): void {
    this.authService.logout();
  }
}
