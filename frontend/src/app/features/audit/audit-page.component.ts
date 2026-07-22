import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
import { Timeline } from 'primeng/timeline';
import { Dialog } from 'primeng/dialog';
import { Button } from 'primeng/button';
import { Skeleton } from 'primeng/skeleton';
import { Message } from 'primeng/message';
import { AuditStatisticsComponent } from './components/audit-statistics.component';
import {
  AuditService,
  AuditDecisionResponse,
  AuditIntegritySummaryResponse,
  AuditRecentItemResponse,
} from '../../core/services/audit.service';
import { ExportService } from '../../core/services/export.service';
import { StatutDecisionEnum } from '../../core/models/decision.models';
import { statutLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { CopyHashComponent } from '../../shared/ui';

interface AuditTimelineEvent {
  status?: string;
  date?: string;
  icon?: string;
  color?: string;
  action: string;
  actor?: string;
  detail?: string;
  correlationId?: string;
}

@Component({
  selector: 'app-audit-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    Card,
    TableModule,
    Tag,
    InputText,
    Select,
    DatePicker,
    Timeline,
    Dialog,
    Button,
    Skeleton,
    Message,
    CopyHashComponent,
    AuditStatisticsComponent,
  ],
  templateUrl: './audit-page.component.html',
  styleUrl: './audit-page.component.scss',
})
export class AuditPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auditService = inject(AuditService);
  private readonly exportService = inject(ExportService);
  private readonly router = inject(Router);

  readonly statuts = Object.values(StatutDecisionEnum);
  readonly statutOptions = [
    { label: 'Tous', value: '' },
    ...this.statuts.map((statut) => ({ label: statutLabel(statut), value: statut })),
  ];

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly integrity = signal<AuditIntegritySummaryResponse | null>(null);
  readonly recentItems = signal<AuditRecentItemResponse[]>([]);
  readonly generatedAt = signal<string | null>(null);

  readonly detailVisible = signal(false);
  readonly detailLoading = signal(false);
  readonly detailError = signal<string | null>(null);
  readonly detail = signal<AuditDecisionResponse | null>(null);
  readonly selectedRecent = signal<AuditRecentItemResponse | null>(null);

  readonly filters = this.fb.group({
    search: this.fb.nonNullable.control(''),
    statut: this.fb.nonNullable.control('' as StatutDecisionEnum | ''),
    period: this.fb.control<Date[] | null>(null),
    integrityFilter: this.fb.nonNullable.control('all' as 'all' | 'valid' | 'invalid'),
    promptSearch: this.fb.nonNullable.control(''),
  });

  readonly appliedFilters = signal<{
    search: string;
    statut: StatutDecisionEnum | '';
    period: Date[] | null;
    integrityFilter: 'all' | 'valid' | 'invalid';
    promptSearch: string;
  }>({
    search: '',
    statut: '',
    period: null,
    integrityFilter: 'all',
    promptSearch: '',
  });

  readonly filteredItems = computed(() => {
    const { search, statut, period, integrityFilter, promptSearch } = this.appliedFilters();
    const query = search.trim().toLowerCase();
    const promptQuery = promptSearch.trim().toLowerCase();
    const from = period?.[0] ? new Date(period[0]) : null;
    const to = period?.[1] ? new Date(period[1]) : null;
    if (from) from.setHours(0, 0, 0, 0);
    if (to) to.setHours(23, 59, 59, 999);

    return this.recentItems().filter((item) => {
      if (statut && item.statutValidation !== statut) {
        return false;
      }
      if (query && !item.decisionId.toLowerCase().includes(query)) {
        return false;
      }
      if (promptQuery && !item.prompt.toLowerCase().includes(promptQuery)) {
        return false;
      }
      if (integrityFilter === 'valid' && !item.integrityValid) {
        return false;
      }
      if (integrityFilter === 'invalid' && item.integrityValid) {
        return false;
      }
      if (from || to) {
        const ts = new Date(item.timestamp).getTime();
        if (from && ts < from.getTime()) return false;
        if (to && ts > to.getTime()) return false;
      }
      return true;
    });
  });

  readonly kpiCards = computed(() => {
    const summary = this.integrity();
    if (!summary) {
      return [];
    }

    return [
      {
        label: 'Total décisions',
        value: String(summary.totalDecisions),
        severity: 'info' as const,
        hint: 'Enregistrements audités',
      },
      {
        label: 'Intégrité valide',
        value: String(summary.validDecisions),
        severity: 'success' as const,
        hint: 'Hash SHA-256 cohérent',
      },
      {
        label: 'Intégrité invalide',
        value: String(summary.invalidDecisions),
        severity: (summary.invalidDecisions > 0 ? 'danger' : 'success') as 'danger' | 'success',
        hint: summary.invalidDecisions > 0 ? 'Vérification requise' : 'Aucune anomalie',
      },
      {
        label: 'Chaîne intacte',
        value: summary.chainIntact ? 'Oui' : 'Non',
        severity: (summary.chainIntact ? 'success' : 'danger') as 'success' | 'danger',
        hint: 'Chaîne cryptographique',
      },
    ];
  });

  readonly timelineEvents = computed<AuditTimelineEvent[]>(() => {
    const audit = this.detail();
    if (!audit?.history?.length) {
      return [];
    }
    return audit.history.map((entry) => ({
      action: entry.action,
      date: entry.createdAt,
      actor: entry.performedByEmail,
      detail: entry.comment || entry.justification,
      correlationId: entry.correlationId,
      icon: 'pi pi-history',
      color: '#4f46e5',
      status: entry.newStatus,
    }));
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
    const raw = this.filters.getRawValue();
    this.appliedFilters.set({
      search: raw.search ?? '',
      statut: raw.statut ?? '',
      period: raw.period ?? null,
      integrityFilter: raw.integrityFilter ?? 'all',
      promptSearch: raw.promptSearch ?? '',
    });
  }

  resetFilters(): void {
    this.filters.reset({
      search: '',
      statut: '',
      period: null,
      integrityFilter: 'all',
      promptSearch: '',
    });
    this.applyFilters();
  }

  get hasActiveFilters(): boolean {
    const filters = this.appliedFilters();
    return !!(
      filters.search ||
      filters.statut ||
      filters.period ||
      filters.integrityFilter !== 'all' ||
      filters.promptSearch
    );
  }

  openDetail(row: AuditRecentItemResponse): void {
    this.selectedRecent.set(row);
    this.detailVisible.set(true);
    this.detail.set(null);
    this.detailError.set(null);
    this.detailLoading.set(true);

    this.auditService.getDecisionAudit(row.decisionId).subscribe({
      next: (audit) => {
        this.detail.set(audit);
        this.detailLoading.set(false);
      },
      error: (err) => {
        this.detailError.set(resolveHttpErrorMessage(err, 'Impossible de charger le détail d\'audit.'));
        this.detailLoading.set(false);
      },
    });
  }

  closeDetail(): void {
    this.detailVisible.set(false);
    this.selectedRecent.set(null);
    this.detail.set(null);
    this.detailError.set(null);
  }

  openDecision(decisionId: string): void {
    this.closeDetail();
    void this.router.navigate(['/decisions', decisionId]);
  }

  async copyText(value: string | undefined | null, _label?: string): Promise<void> {
    if (!value) return;
    try {
      await navigator.clipboard.writeText(value);
    } catch {
      // Presse-papiers indisponible : aucun secret n'est exposé autrement.
    }
  }

  statutLabel = statutLabel;

  statutSeverity(statut: StatutDecisionEnum): 'success' | 'danger' | 'warn' | 'secondary' | 'info' {
    if (statut === StatutDecisionEnum.APPROUVEE) return 'success';
    if (statut === StatutDecisionEnum.REJETEE) return 'danger';
    if (statut === StatutDecisionEnum.EN_ATTENTE) return 'warn';
    if (statut === StatutDecisionEnum.MODIFIEE) return 'info';
    return 'secondary';
  }

  integritySeverity(valid: boolean): 'success' | 'danger' {
    return valid ? 'success' : 'danger';
  }

  integrityLabel(valid: boolean): string {
    return valid ? 'Valide' : 'Invalide';
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

  exportCSV(): void {
    const items = this.filteredItems();
    const filters = this.appliedFilters();
    this.exportService.exportAuditCSV(items, filters);
  }

  async exportPDF(): Promise<void> {
    const items = this.filteredItems();
    const summary = this.integrity();
    const filters = this.appliedFilters();
    
    if (!summary) {
      return;
    }

    await this.exportService.exportAuditPDF(items, summary, filters);
  }
}
