import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { IconComponent } from '../../shared/icon.component';
import {
  EmptyStateComponent,
  ErrorStateComponent,
  LoadingSkeletonComponent,
  PageHeaderComponent,
} from '../../shared/ui';
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
    DecimalPipe,
    IconComponent,
    PageHeaderComponent,
    LoadingSkeletonComponent,
    ErrorStateComponent,
    EmptyStateComponent,
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

  readonly agents = computed<ComparaisonAgent[]>(() => {
    const field = this.sortField();
    const dir = this.sortDir();
    const sorted = [...this.agentsData()].sort((a, b) => {
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
    const list = [...this.agentsData()].sort((a, b) => b.scorePourcentage - a.scorePourcentage);
    const max = Math.max(...list.map((a) => a.scorePourcentage), 1);
    return list.map((a, i) => ({
      ...a,
      x: i * (this.BAR_W + this.GAP),
      height: Math.round((a.scorePourcentage / max) * this.BAR_H),
      color: this.barColor(i),
    }));
  });

  readonly svgWidth = computed(() =>
    Math.max(this.agentsData().length, 1) * (this.BAR_W + this.GAP) - this.GAP + 60,
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
    if (rang === 1) return '🥇';
    if (rang === 2) return '🥈';
    if (rang === 3) return '🥉';
    return String(rang);
  }

  successRateClass(rate: number): string {
    if (rate >= 70) return 'score-high';
    if (rate >= 40) return 'score-mid';
    return 'score-low';
  }

  private barColor(index: number): string {
    const palette = ['url(#grad0)', 'url(#grad1)', 'url(#grad2)', 'url(#grad3)'];
    return palette[index % palette.length];
  }
}
