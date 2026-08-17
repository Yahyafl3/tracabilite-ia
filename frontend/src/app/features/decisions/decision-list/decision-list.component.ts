import { Component, computed, inject, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { InputText } from 'primeng/inputtext';
import { Skeleton } from 'primeng/skeleton';
import { Paginator, type PaginatorState } from 'primeng/paginator';
import { Menu } from 'primeng/menu';
import { Message } from 'primeng/message';
import type { MenuItem } from 'primeng/api';
import { TranslatePipe } from '@ngx-translate/core';
import { ConfidenceDisplayComponent, RiskBadgeComponent } from '../../../shared/ui';
import { DecisionService } from '../../../core/services/decision.service';
import { ExportService } from '../../../core/services/export.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  DecisionResponse,
  StatutDecisionEnum,
  humanFinalLabel,
  mlDecision,
  mlConfidence,
} from '../../../core/models/decision.models';
import { UserRole } from '../../../core/models/auth.models';
import { decisionLabel, domainLabel, roleLabel, statutLabel } from '../../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { DECISION_DOMAINS } from '../../../core/config/domains/domain.config';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-decision-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    Card,
    TableModule,
    Tag,
    Button,
    Select,
    InputText,
    Skeleton,
    Paginator,
    Menu,
    Message,
    ConfidenceDisplayComponent,
    RiskBadgeComponent,
    TranslatePipe,
  ],
  templateUrl: './decision-list.component.html',
  styleUrl: './decision-list.component.scss',
})
export class DecisionListComponent {
  private readonly exportService = inject(ExportService);
  @ViewChild('rowMenu') rowMenu!: Menu;

  private readonly fb = inject(FormBuilder);
  private readonly decisionService = inject(DecisionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly i18n = inject(TranslationService);

  readonly statutOptions = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('common.all'), value: '' },
      ...[
        StatutDecisionEnum.BROUILLON,
        StatutDecisionEnum.EN_ANALYSE,
        StatutDecisionEnum.ANALYSEE,
        StatutDecisionEnum.EN_ATTENTE_VALIDATION,
        StatutDecisionEnum.EN_ATTENTE,
        StatutDecisionEnum.VALIDEE,
        StatutDecisionEnum.APPROUVEE,
        StatutDecisionEnum.A_REVOIR,
        StatutDecisionEnum.REJETEE,
        StatutDecisionEnum.ARCHIVEE,
        StatutDecisionEnum.MODIFIEE,
      ].map((statut) => ({ label: statutLabel(statut), value: statut })),
    ];
  });

  readonly domaineOptions = computed(() => {
    this.i18n.currentLang();
    const role = this.authService.currentUser?.role as UserRole | undefined;
    if (role === UserRole.RESPONSABLE_CREDIT) {
      return [{ label: domainLabel('CREDIT'), value: 'CREDIT' }];
    }
    if (role === UserRole.PROFESSIONNEL_SANTE) {
      return [{ label: domainLabel('MEDICAL'), value: 'MEDICAL' }];
    }
    if (role === UserRole.RESPONSABLE_PEDAGOGIQUE) {
      return [{ label: domainLabel('EDUCATION'), value: 'EDUCATION' }];
    }
    return [
      { label: this.i18n.t('common.all'), value: '' },
      ...DECISION_DOMAINS.map((d) => ({ label: domainLabel(d.value), value: d.value })),
    ];
  });

  readonly riskOptions = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('common.all'), value: '' },
      { label: this.i18n.t('riskCode.FAIBLE'), value: 'FAIBLE' },
      { label: this.i18n.t('riskCode.MODERE'), value: 'MODERE' },
      { label: this.i18n.t('riskCode.MOYEN'), value: 'MOYEN' },
      { label: this.i18n.t('riskCode.ELEVE'), value: 'ELEVE' },
      { label: this.i18n.t('decisions.list.legacyLow'), value: 'LOW' },
      { label: this.i18n.t('decisions.list.legacyMedium'), value: 'MEDIUM' },
      { label: this.i18n.t('decisions.list.legacyHigh'), value: 'HIGH' },
    ];
  });

  readonly pageSizeOptions = [5, 10, 20];
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly loading = signal(false);
  readonly exporting = signal(false);
  readonly error = signal<string | null>(null);
  readonly rowMenuItems = signal<MenuItem[]>([]);

  readonly canExport = computed(() => {
    const role = this.authService.currentUser?.role;
    return role === UserRole.ADMINISTRATEUR || role === UserRole.AUDITEUR || role === 'ADMINISTRATEUR' || role === 'AUDITEUR';
  });

  readonly canCreate = computed(() => {
    const role = this.authService.currentUser?.role;
    return (
      role === UserRole.ADMINISTRATEUR ||
      role === UserRole.AGENT_CREDIT ||
      role === UserRole.AGENT_SANTE ||
      role === UserRole.AGENT_PEDAGOGIQUE
    );
  });

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.totalElements() / this.size())),
  );

  readonly rangeStart = computed(() =>
    this.totalElements() === 0 ? 0 : this.page() * this.size() + 1,
  );

  readonly rangeEnd = computed(() =>
    Math.min((this.page() + 1) * this.size(), this.totalElements()),
  );

  readonly filters = this.fb.nonNullable.group({
    search: [''],
    statut: ['' as StatutDecisionEnum | ''],
    domaine: [''],
    riskLevel: [''],
    decisionFinale: [''],
    validateur: [''],
    fromDate: [''],
    toDate: [''],
  });

  constructor() {
    this.load();
  }

  load(page = this.page(), size = this.size()): void {
    this.loading.set(true);
    this.error.set(null);
    const filters = this.filters.getRawValue();
    this.decisionService
      .search({
        search: filters.search,
        statut: filters.statut,
        domaine: filters.domaine || undefined,
        riskLevel: filters.riskLevel || undefined,
        decisionFinale: filters.decisionFinale || undefined,
        validateur: filters.validateur || undefined,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
        page,
        size,
      })
      .subscribe({
        next: (response) => {
          this.decisions.set(response.content);
          this.totalElements.set(response.totalElements);
          this.page.set(response.page);
          this.size.set(response.size);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(resolveHttpErrorMessage(err, this.i18n.t('decisions.list.loadError')));
          this.loading.set(false);
        },
      });
  }

  applyFilters(): void {
    this.load(0, this.size());
  }

  onPageChange(event: PaginatorState): void {
    this.load(event.page ?? 0, event.rows ?? this.size());
  }

  export(format: 'csv' | 'xlsx'): void {
    if (!this.canExport()) return;
    this.exporting.set(true);
    const filters = this.filters.getRawValue();
    const filename = format === 'xlsx' ? 'decisions-export.xls' : 'decisions-export.csv';
    this.decisionService
      .exportDecisions({
        format,
        domaine: filters.domaine || undefined,
        statut: filters.statut || undefined,
        validateur: filters.validateur || undefined,
        fromDate: filters.fromDate || undefined,
        toDate: filters.toDate || undefined,
      })
      .subscribe({
        next: (blob) => {
          void this.exportService
            .assertDownloadableExport(blob)
            .then((file) => this.exportService.downloadBlob(file, filename))
            .catch((err) => {
              this.error.set(resolveHttpErrorMessage(err, this.i18n.t('decisions.list.exportError')));
            })
            .finally(() => this.exporting.set(false));
        },
        error: (err) => {
          this.error.set(resolveHttpErrorMessage(err, this.i18n.t('decisions.list.exportError')));
          this.exporting.set(false);
        },
      });
  }

  openRowMenu(event: Event, row: DecisionResponse): void {
    this.rowMenuItems.set([
      {
        label: this.i18n.t('decisions.list.viewDetail'),
        icon: 'pi pi-eye',
        command: () => void this.router.navigate(['/decisions', row.decisionId]),
      },
    ]);
    this.rowMenu.toggle(event);
  }

  goToNew(): void {
    void this.router.navigate(['/decisions/new']);
  }

  statutLabel = (statut: string) => {
    this.i18n.currentLang();
    return statutLabel(statut);
  };
  domainLabel = (domain: string) => {
    this.i18n.currentLang();
    return domainLabel(domain);
  };
  decisionLabel = (decision?: string | null) => {
    this.i18n.currentLang();
    return decisionLabel(decision);
  };
  roleLabel = (role: string) => {
    this.i18n.currentLang();
    return roleLabel(role);
  };

  domainBadge(row: DecisionResponse): string {
    return row.domaine || 'CREDIT';
  }

  domainSeverity(domain: string | undefined): 'info' | 'success' | 'warn' | 'secondary' {
    switch (domain) {
      case 'MEDICAL':
        return 'warn';
      case 'EDUCATION':
        return 'success';
      case 'CREDIT':
        return 'info';
      default:
        return 'secondary';
    }
  }

  statutSeverity(
    statut: StatutDecisionEnum,
  ): 'success' | 'warn' | 'danger' | 'secondary' | 'info' {
    switch (statut) {
      case StatutDecisionEnum.APPROUVEE:
      case StatutDecisionEnum.VALIDEE:
        return 'success';
      case StatutDecisionEnum.MODIFIEE:
      case StatutDecisionEnum.A_REVOIR:
        return 'warn';
      case StatutDecisionEnum.REJETEE:
        return 'danger';
      case StatutDecisionEnum.EN_ATTENTE:
      case StatutDecisionEnum.EN_ATTENTE_VALIDATION:
        return 'secondary';
      default:
        return 'info';
    }
  }

  mlSeverity(label: string | undefined): 'success' | 'danger' | 'warn' | 'secondary' {
    if (!label) return 'secondary';
    if (label.includes('FAIBLE') || label === 'APPROUVER') return 'success';
    if (label.includes('MOYEN') || label.includes('MODERE')) return 'warn';
    if (label.includes('ELEVE') || label === 'REJETER') return 'danger';
    return 'secondary';
  }

  formatDate(iso: string | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString(this.i18n.localeId(), {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatTime(iso: string | undefined): string {
    if (!iso) return '';
    return new Date(iso).toLocaleTimeString(this.i18n.localeId(), { hour: '2-digit', minute: '2-digit' });
  }

  reference(row: DecisionResponse): string {
    return row.dossierReference ?? row.reference ?? row.decisionId.slice(0, 8).toUpperCase();
  }

  mlDecisionLabel = mlDecision;
  mlConfidenceValue = mlConfidence;
  humanFinal = humanFinalLabel;

  riskLevel(row: DecisionResponse): string | undefined {
    return row.mlPrediction?.riskLevel ?? row.riskLevel;
  }
}
