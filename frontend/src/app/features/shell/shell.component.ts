import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { IconComponent } from '../../shared/icon.component';
import { ThemeService } from '../../shared/theme.service';
import { roleLabel } from '../../core/utils/label.util';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  badge?: number;
}

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive, IconComponent],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  private readonly authService = inject(AuthService);
  readonly themeService = inject(ThemeService);

  readonly sidebarOpen = signal(true);
  readonly mobileSidebarOpen = signal(false);

  readonly navItems = computed<NavItem[]>(() => {
    const role = this.authService.currentUser?.role;
    const canValidate = role === UserRole.VALIDATEUR || role === UserRole.ADMINISTRATEUR;
    const isAdmin = role === UserRole.ADMINISTRATEUR;
    const isAuditor = role === UserRole.AUDITEUR || isAdmin;

    const items: NavItem[] = [
      { label: 'Tableau de bord', icon: 'activity', route: '/dashboard' },
      { label: 'Décisions', icon: 'file-text', route: '/decisions' },
    ];

    if (canValidate) {
      items.push({ label: 'Validation', icon: 'file-check', route: '/validation' });
    }

    items.push({ label: 'Comparaison IA', icon: 'bar-chart', route: '/comparaison' });

    if (isAuditor) {
      items.push({ label: 'Audit', icon: 'shield-check', route: '/audit' });
    }

    if (isAdmin) {
      items.push({ label: 'Utilisateurs', icon: 'users', route: '/admin/users' });
    }

    return items;
  });

  toggleSidebar(): void {
    this.sidebarOpen.update((v) => !v);
  }

  toggleMobileSidebar(): void {
    this.mobileSidebarOpen.update((v) => !v);
  }

  closeMobileSidebar(): void {
    this.mobileSidebarOpen.set(false);
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

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
