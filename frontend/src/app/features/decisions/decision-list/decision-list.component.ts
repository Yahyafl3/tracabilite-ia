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
import { ConfidenceDisplayComponent, RiskBadgeComponent } from '../../../shared/ui';
import { DecisionService } from '../../../core/services/decision.service';
import {
  DecisionResponse,
  StatutDecisionEnum,
  consensusLabel,
  humanFinalLabel,
  mlDecision,
  mlConfidence,
} from '../../../core/models/decision.models';
import { statutLabel } from '../../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';

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
  ],
  templateUrl: './decision-list.component.html',
  styleUrl: './decision-list.component.scss',
})
export class DecisionListComponent {
  @ViewChild('rowMenu') rowMenu!: Menu;

  private readonly fb = inject(FormBuilder);
  private readonly decisionService = inject(DecisionService);
  private readonly router = inject(Router);

  readonly statutOptions = [
    { label: 'Tous', value: '' },
    ...Object.values(StatutDecisionEnum).map((statut) => ({
      label: statutLabel(statut),
      value: statut,
    })),
  ];

  readonly pageSizeOptions = [5, 10, 20];
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly page = signal(0);
  readonly size = signal(10);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly rowMenuItems = signal<MenuItem[]>([]);

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
          this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger les décisions.'));
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

  openRowMenu(event: Event, row: DecisionResponse): void {
    this.rowMenuItems.set([
      {
        label: 'Voir le détail',
        icon: 'pi pi-eye',
        command: () => void this.router.navigate(['/decisions', row.decisionId]),
      },
    ]);
    this.rowMenu.toggle(event);
  }

  goToNew(): void {
    void this.router.navigate(['/decisions/new']);
  }

  statutLabel = statutLabel;

  statutSeverity(
    statut: StatutDecisionEnum,
  ): 'success' | 'warn' | 'danger' | 'secondary' | 'info' {
    switch (statut) {
      case StatutDecisionEnum.APPROUVEE:
        return 'success';
      case StatutDecisionEnum.MODIFIEE:
        return 'warn';
      case StatutDecisionEnum.REJETEE:
        return 'danger';
      case StatutDecisionEnum.EN_ATTENTE:
        return 'secondary';
      default:
        return 'info';
    }
  }

  mlSeverity(label: string | undefined): 'success' | 'danger' | 'secondary' {
    if (label === 'APPROUVER') return 'success';
    if (label === 'REJETER') return 'danger';
    return 'secondary';
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  reference(row: DecisionResponse): string {
    return row.reference ?? row.decisionId.slice(0, 8).toUpperCase();
  }

  mlDecisionLabel = mlDecision;
  mlConfidenceValue = mlConfidence;
  consensusText = consensusLabel;
  humanFinal = humanFinalLabel;

  riskLevel(row: DecisionResponse): string | undefined {
    return row.mlPrediction?.riskLevel ?? row.riskLevel;
  }
}
