import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { IconComponent } from '../../shared/icon.component';
import {
  PageHeaderComponent,
  KpiCardComponent,
  StatusBadgeComponent,
  EmptyStateComponent,
  ErrorStateComponent,
  LoadingSkeletonComponent,
  type KpiAccent,
} from '../../shared/ui';
import {
  AuditService,
  AuditIntegritySummaryResponse,
  AuditRecentItemResponse,
} from '../../core/services/audit.service';
import { StatutDecisionEnum } from '../../core/models/decision.models';
import { statutChipClass } from '../../core/utils/chip-class.util';
import { statutLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-audit-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    IconComponent,
    PageHeaderComponent,
    KpiCardComponent,
    StatusBadgeComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    LoadingSkeletonComponent,
  ],
  templateUrl: './audit-page.component.html',
  styleUrl: './audit-page.component.scss',
})
export class AuditPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auditService = inject(AuditService);

  readonly statuts = Object.values(StatutDecisionEnum);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly integrity = signal<AuditIntegritySummaryResponse | null>(null);
  readonly recentItems = signal<AuditRecentItemResponse[]>([]);
  readonly generatedAt = signal<string | null>(null);

  readonly filters = this.fb.nonNullable.group({
    search: [''],
    statut: ['' as StatutDecisionEnum | ''],
  });

  readonly appliedFilters = signal({ search: '', statut: '' as StatutDecisionEnum | '' });

  readonly filteredItems = computed(() => {
    const { search, statut } = this.appliedFilters();
    const query = search.trim().toLowerCase();

    return this.recentItems().filter((item) => {
      if (statut && item.statutValidation !== statut) {
        return false;
      }
      if (query && !item.decisionId.toLowerCase().includes(query)) {
        return false;
      }
      return true;
    });
  });

  readonly kpiCards = computed(() => {
    const summary = this.integrity();
    if (!summary) {
      return [];
    }

    const chainAccent: KpiAccent = summary.chainIntact ? 'green' : 'danger';
    const invalidAccent: KpiAccent = summary.invalidDecisions > 0 ? 'danger' : 'green';

    return [
      {
        label: 'Total décisions',
        value: summary.totalDecisions,
        unit: '',
        icon: 'file-text',
        accent: 'indigo' as const,
        hint: 'Enregistrements audités',
      },
      {
        label: 'Intégrité valide',
        value: summary.validDecisions,
        unit: '',
        icon: 'shield-check',
        accent: 'green' as const,
        hint: 'Hash SHA-256 cohérent',
      },
      {
        label: 'Intégrité invalide',
        value: summary.invalidDecisions,
        unit: '',
        icon: 'shield-alert',
        accent: invalidAccent,
        hint: summary.invalidDecisions > 0 ? 'Vérification requise' : 'Aucune anomalie',
      },
      {
        label: 'Chaîne intacte',
        value: summary.chainIntact ? 'Oui' : 'Non',
        unit: '',
        icon: 'link',
        accent: chainAccent,
        hint: 'Chaîne cryptographique',
      },
    ];
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      integrity: this.auditService.getIntegritySummary(),
      recent: this.auditService.getRecent(50),
    }).subscribe({
      next: ({ integrity, recent }) => {
        this.integrity.set(integrity);
        this.recentItems.set(recent.items);
        this.generatedAt.set(recent.generatedAt ?? integrity.generatedAt ?? null);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger les données d\'audit.'));
        this.loading.set(false);
      },
    });
  }

  applyFilters(): void {
    const { search, statut } = this.filters.getRawValue();
    this.appliedFilters.set({ search, statut });
  }

  statutLabel = statutLabel;
  statutChipClass = statutChipClass;

  integrityLabel(valid: boolean): string {
    return valid ? 'Valide' : 'Invalide';
  }

  integrityChipClass(valid: boolean): string {
    return valid ? 'chip--approved' : 'chip--rejected';
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' });
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  formatGeneratedAt(iso: string | null): string {
    if (!iso) return '';
    return new Date(iso).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  shortUuid(id: string): string {
    return id.length > 12 ? `${id.slice(0, 8)}…${id.slice(-4)}` : id;
  }
}
