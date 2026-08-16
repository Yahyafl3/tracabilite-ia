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
    const isAdmin = role === UserRole.ADMINISTRATEUR;
    const isAuditeur = role === UserRole.AUDITEUR;
    const isValidateur = role === UserRole.VALIDATEUR;
    const isDomainValidator =
      role === UserRole.RESPONSABLE_CREDIT ||
      role === UserRole.PROFESSIONNEL_SANTE ||
      role === UserRole.RESPONSABLE_PEDAGOGIQUE;
    const isAgent =
      role === UserRole.AGENT_CREDIT ||
      role === UserRole.AGENT_SANTE ||
      role === UserRole.AGENT_PEDAGOGIQUE ||
      role === UserRole.UTILISATEUR;
    const canValidate = isValidateur || isDomainValidator || isAdmin;
    const canCreate = isAdmin || isAgent;

    // All roles see Dashboard
    const appItems: AppMenuItem[] = [
      { label: 'Tableau de bord', icon: 'pi pi-home', routerLink: '/dashboard' }
    ];

    if (!isAuditeur) {
      if (isValidateur || isDomainValidator) {
         appItems.push({ label: 'Historique des validations', icon: 'pi pi-file', routerLink: '/decisions' });
      } else {
         appItems.push({ label: 'Mes décisions', icon: 'pi pi-file', routerLink: '/decisions' });
      }
    }

    if (canCreate) {
      appItems.push({ label: 'Nouvelle décision', icon: 'pi pi-plus-circle', routerLink: '/decisions/new' });
    }

    const items: AppMenuItem[] = [
      { label: 'Application', items: appItems },
    ];

    const analysisItems: AppMenuItem[] = [];
    if (canValidate) {
      analysisItems.push({
        label: 'File de validation',
        icon: 'pi pi-check-square',
        routerLink: '/validation',
      });
    }
    if (isAdmin) {
      analysisItems.push({
        label: 'Comparaison IA',
        icon: 'pi pi-chart-bar',
        routerLink: '/comparaison',
      });
    }
    if (isAdmin || isAuditeur) {
      analysisItems.push({ label: 'Audit & traçabilité', icon: 'pi pi-shield', routerLink: '/audit' });
    }

    if (analysisItems.length > 0) {
      items.push({ label: 'Analyse', items: analysisItems });
    }

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
