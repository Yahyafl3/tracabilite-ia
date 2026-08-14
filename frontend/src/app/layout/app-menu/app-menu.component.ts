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
    const isDomainValidator =
      role === UserRole.RESPONSABLE_CREDIT ||
      role === UserRole.PROFESSIONNEL_SANTE ||
      role === UserRole.RESPONSABLE_PEDAGOGIQUE;
    const isAgent =
      role === UserRole.AGENT_CREDIT ||
      role === UserRole.AGENT_SANTE ||
      role === UserRole.AGENT_PEDAGOGIQUE ||
      role === UserRole.UTILISATEUR;
    const canValidate = isDomainValidator || isAdmin;
    const canCreate = isAdmin || isAgent;

    // Auditeur — audit only
    if (isAuditeur) {
      return [
        {
          label: 'Audit',
          items: [
            { label: 'Audit & traçabilité', icon: 'pi pi-shield', routerLink: '/audit' },
          ],
        },
      ];
    }

    // Domain validators — validation queue only + mes décisions
    if (isDomainValidator) {
      return [
        {
          label: 'Validation',
          items: [
            { label: 'File de validation', icon: 'pi pi-check-square', routerLink: '/validation' },
            { label: 'Historique des validations', icon: 'pi pi-file', routerLink: '/decisions' },
          ],
        },
      ];
    }

    const appItems: AppMenuItem[] = [
      { label: 'Dashboard', icon: 'pi pi-home', routerLink: '/dashboard' },
      { label: 'Mes décisions', icon: 'pi pi-file', routerLink: '/decisions' },
    ];
    if (canCreate) {
      appItems.push({ label: 'Nouvelle décision', icon: 'pi pi-plus-circle', routerLink: '/decisions/new' });
    }

    const items: AppMenuItem[] = [
      { label: 'Application', items: appItems },
    ];

    // Analyse : réservée aux rôles effectivement autorisés côté API
    // (ComparaisonController = ADMIN / VALIDATOR / AUDITOR — pas ROLE_USER).
    const analysisItems: AppMenuItem[] = [];
    if (canValidate) {
      analysisItems.push({
        label: 'Validation humaine',
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
      analysisItems.push({ label: 'Audit', icon: 'pi pi-shield', routerLink: '/audit' });
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
