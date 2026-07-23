import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Select } from 'primeng/select';
import { DatePicker } from 'primeng/datepicker';
import { ProgressBar } from 'primeng/progressbar';
import { Skeleton } from 'primeng/skeleton';
import { Message } from 'primeng/message';
import { Button } from 'primeng/button';
import { ComparaisonAgent, ComparaisonService } from '../../core/services/comparaison.service';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';

type SortField =
  | 'rang'
  | 'nom'
  | 'totalDecisions'
  | 'approuvees'
  | 'modifiees'
  | 'rejetees'
  | 'enAttente'
  | 'scorePourcentage';
type SortDir = 'asc' | 'desc';

@Component({
  selector: 'app-comparaison',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DecimalPipe,
    Card,
    TableModule,
    Tag,
    Select,
    DatePicker,
    ProgressBar,
    Skeleton,
    Message,
    Button,
  ],
  templateUrl: './comparaison.component.html',
  styleUrl: './comparaison.component.scss',
})
export class ComparaisonComponent {
  private readonly comparaisonService = inject(ComparaisonService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly agentsData = signal<ComparaisonAgent[]>([]);
  readonly sortField = signal<SortField>('scorePourcentage');
  readonly sortDir = signal<SortDir>('desc');

  readonly providerFilter = signal<string | null>(null);
  readonly modelFilter = signal<string | null>(null);
  /** UI only — API aggregates have no per-period timestamps; values are never recalculated. */
  readonly periodRange = signal<Date[] | null>(null);

  readonly providerOptions = computed(() => {
    const values = [...new Set(this.agentsData().map((a) => a.fournisseur).filter(Boolean))];
    return [{ label: 'Tous les providers', value: null }, ...values.map((v) => ({ label: v, value: v }))];
  });

  readonly modelOptions = computed(() => {
    const values = [...new Set(this.agentsData().map((a) => a.modele).filter(Boolean))];
    return [{ label: 'Tous les modèles', value: null }, ...values.map((v) => ({ label: v, value: v }))];
  });

  readonly filteredAgentsData = computed(() => {
    const provider = this.providerFilter();
    const model = this.modelFilter();
    return this.agentsData().filter((agent) => {
      if (provider && agent.fournisseur !== provider) return false;
      if (model && agent.modele !== model) return false;
      return true;
    });
  });

  readonly periodFilterActive = computed(() => {
    const range = this.periodRange();
    return Array.isArray(range) && range.some((d) => !!d);
  });

  readonly agents = computed<ComparaisonAgent[]>(() => {
    const field = this.sortField();
    const dir = this.sortDir();
    const sorted = [...this.filteredAgentsData()].sort((a, b) => {
      const va = a[field] as number | string;
      const vb = b[field] as number | string;
      if (typeof va === 'string') {
        return dir === 'asc'
          ? (va as string).localeCompare(vb as string)
          : (vb as string).localeCompare(va as string);
      }
      return dir === 'asc' ? (va as number) - (vb as number) : (vb as number) - (va as number);
    });
    return sorted.map((a, i) => ({ ...a, rang: i + 1 }));
  });

  readonly BAR_H = 160;
  readonly BAR_W = 56;
  readonly GAP = 40;

  readonly chartBars = computed(() => {
    const list = [...this.filteredAgentsData()].sort((a, b) => b.scorePourcentage - a.scorePourcentage);
    const max = Math.max(...list.map((a) => a.scorePourcentage), 1);
    return list.map((a, i) => ({
      ...a,
      x: i * (this.BAR_W + this.GAP),
      height: Math.round((a.scorePourcentage / max) * this.BAR_H),
      color: this.barColor(i),
    }));
  });

  readonly svgWidth = computed(() =>
    Math.max(this.filteredAgentsData().length, 1) * (this.BAR_W + this.GAP) - this.GAP + 60,
  );

  constructor() {
    this.comparaisonService.getOpenRouterAgents().subscribe({
      next: (agents) => {
        this.agentsData.set(agents);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger la comparaison agents.'));
        this.loading.set(false);
      },
    });
  }

  clearFilters(): void {
    this.providerFilter.set(null);
    this.modelFilter.set(null);
    this.periodRange.set(null);
  }

  sort(field: SortField): void {
    if (this.sortField() === field) {
      this.sortDir.update((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortField.set(field);
      this.sortDir.set('desc');
    }
  }

  isSorted(field: SortField): boolean {
    return this.sortField() === field;
  }

  rankClass(rang: number): string {
    if (rang === 1) return 'rank-gold';
    if (rang === 2) return 'rank-silver';
    if (rang === 3) return 'rank-bronze';
    return '';
  }

  rankIcon(rang: number): string {
    if (rang === 1) return '1';
    if (rang === 2) return '2';
    if (rang === 3) return '3';
    return String(rang);
  }

  successRateClass(rate: number): string {
    if (rate >= 70) return 'score-high';
    if (rate >= 40) return 'score-mid';
    return 'score-low';
  }

  scoreSeverity(rate: number): 'success' | 'warn' | 'danger' {
    if (rate >= 70) return 'success';
    if (rate >= 40) return 'warn';
    return 'danger';
  }

  providerSeverity(provider: string): 'info' | 'secondary' | 'success' {
    const normalized = provider.toLowerCase();
    if (normalized.includes('groq')) return 'success';
    if (normalized.includes('openrouter')) return 'info';
    return 'secondary';
  }

  private barColor(index: number): string {
    const palette = ['url(#grad0)', 'url(#grad1)', 'url(#grad2)', 'url(#grad3)'];
    return palette[index % palette.length];
  }
}
