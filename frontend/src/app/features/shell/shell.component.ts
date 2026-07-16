import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { IconComponent } from '../../shared/icon.component';
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

  // ── Sidebar ──────────────────────────────────────────────────
  readonly sidebarOpen = signal(true);

  readonly navItems = computed<NavItem[]>(() => {
    const role = this.authService.currentUser?.role;
    const canValidate = role === UserRole.VALIDATEUR || role === UserRole.ADMINISTRATEUR;
    const items: NavItem[] = [
      { label: 'Tableau de bord', icon: 'activity', route: '/dashboard' },
      { label: 'Décisions', icon: 'file-text', route: '/decisions' },
    ];
    if (canValidate) {
      items.push({ label: 'Validation', icon: 'file-check', route: '/validation' });
    }
    items.push({ label: 'Comparaison IA', icon: 'bar-chart', route: '/comparaison' });
    return items;
  });
  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  // ── User ─────────────────────────────────────────────────────
  get currentUser() {
    return this.authService.currentUser;
  }

  get userInitials(): string {
    const u = this.currentUser;
    if (!u) return 'U';
    const name = (u as any).nom ?? (u as any).name ?? '';
    return name ? name.charAt(0).toUpperCase() : (u.email?.charAt(0).toUpperCase() ?? 'U');
  }

  get userName(): string {
    const u = this.currentUser;
    if (!u) return '';
    return (u as any).nom ?? u.email ?? '';
  }

  get userRole(): string {
    const u = this.currentUser;
    return (u as any)?.role ?? '';
  }

  logout(): void {
    this.authService.logout();
  }
}
