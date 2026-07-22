import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { AppMenuItemComponent, type AppMenuItem } from '../app-menu-item/app-menu-item.component';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, AppMenuItemComponent],
  templateUrl: './app-menu.component.html',
  styleUrl: './app-menu.component.scss',
})
export class AppMenuComponent {
  private readonly authService = inject(AuthService);

  readonly model = computed<AppMenuItem[]>(() => {
    const role = this.authService.currentUser?.role;
    const canValidate = role === UserRole.VALIDATEUR || role === UserRole.ADMINISTRATEUR;
    const isAdmin = role === UserRole.ADMINISTRATEUR;
    const isAuditor = role === UserRole.AUDITEUR || isAdmin;

    const items: AppMenuItem[] = [
      {
        label: 'Application',
        items: [
          { label: 'Dashboard', icon: 'pi pi-home', routerLink: '/dashboard' },
          { label: 'Décisions', icon: 'pi pi-file', routerLink: '/decisions' },
          { label: 'Nouvelle décision', icon: 'pi pi-plus-circle', routerLink: '/decisions/new' },
        ],
      },
    ];

    const analysisItems: AppMenuItem[] = [];
    if (canValidate) {
      analysisItems.push({
        label: 'Validation humaine',
        icon: 'pi pi-check-square',
        routerLink: '/validation',
      });
    }
    analysisItems.push({
      label: 'Comparaison IA',
      icon: 'pi pi-chart-bar',
      routerLink: '/comparaison',
    });
    if (isAuditor) {
      analysisItems.push({ label: 'Audit', icon: 'pi pi-shield', routerLink: '/audit' });
    }

    items.push({ label: 'Analyse', items: analysisItems });

    if (isAdmin) {
      items.push({
        label: 'Administration',
        items: [
          { label: 'Utilisateurs', icon: 'pi pi-users', routerLink: '/admin/users' },
          { label: 'Agents Groq', icon: 'pi pi-server', routerLink: '/admin/groq' },
          { label: 'Support', icon: 'pi pi-envelope', routerLink: '/admin/support' },
        ],
      });
    }

    return items;
  });
}
