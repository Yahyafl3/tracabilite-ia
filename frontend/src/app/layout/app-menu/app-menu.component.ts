import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { AppMenuItemComponent, type AppMenuItem } from '../app-menu-item/app-menu-item.component';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, TranslatePipe, AppMenuItemComponent],
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
      role === UserRole.AGENT_PEDAGOGIQUE;
    const canValidate = isDomainValidator || isAdmin;
    const canCreate = isAdmin || isAgent;

    const appItems: AppMenuItem[] = [
      { label: 'nav.dashboard', icon: 'pi pi-home', routerLink: '/dashboard' }
    ];

    if (!isAuditeur) {
      if (isDomainValidator) {
         appItems.push({ label: 'nav.validationHistory', icon: 'pi pi-file', routerLink: '/decisions' });
      } else {
         appItems.push({ label: 'nav.myDecisions', icon: 'pi pi-file', routerLink: '/decisions' });
      }
    }

    if (canCreate) {
      appItems.push({ label: 'nav.newDecision', icon: 'pi pi-plus-circle', routerLink: '/decisions/new' });
    }

    const items: AppMenuItem[] = [
      { label: 'nav.application', items: appItems },
    ];

    const analysisItems: AppMenuItem[] = [];
    if (canValidate) {
      analysisItems.push({
        label: 'nav.validationQueue',
        icon: 'pi pi-check-square',
        routerLink: '/validation',
      });
    }
    if (isAdmin) {
      analysisItems.push({
        label: 'nav.aiComparison',
        icon: 'pi pi-chart-bar',
        routerLink: '/comparaison',
      });
    }
    if (isAdmin || isAuditeur) {
      analysisItems.push({ label: 'nav.audit', icon: 'pi pi-shield', routerLink: '/audit' });
    }

    if (analysisItems.length > 0) {
      items.push({ label: 'nav.analysis', items: analysisItems });
    }

    if (isAdmin) {
      items.push({
        label: 'nav.administration',
        items: [
          { label: 'nav.users', icon: 'pi pi-users', routerLink: '/admin/users' },
          { label: 'nav.groqAgents', icon: 'pi pi-server', routerLink: '/admin/groq' },
          { label: 'nav.support', icon: 'pi pi-envelope', routerLink: '/admin/support' },
          { label: 'nav.backupRestore', icon: 'pi pi-database', routerLink: '/admin/backup' },
        ],
      });
    }

    return items;
  });
}
