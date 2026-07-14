import { Component, computed, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { IconComponent } from '../../shared/icon.component';

export interface AgentStats {
  rang: number;
  systemeIaId: string;
  nom: string;
  fournisseur: string;
  modele: string;
  versionModele: string;
  totalDecisions: number;
  approuvees: number;
  modifiees: number;
  rejetees: number;
  enAttente: number;
  scorePourcentage: number;
}

/** Static demo data — matches the seed in DataInitializer */
const DEMO_AGENTS: AgentStats[] = [
  {
    rang: 0,
    systemeIaId: '1',
    nom: 'ChatGPT',
    fournisseur: 'OpenAI',
    modele: 'gpt-4.1',
    versionModele: 'latest',
    totalDecisions: 3,
    approuvees: 2,
    modifiees: 1,
    rejetees: 0,
    enAttente: 0,
    scorePourcentage: 83.33,
  },
  {
    rang: 0,
    systemeIaId: '2',
    nom: 'Gemini',
    fournisseur: 'Google',
    modele: 'gemini-2.5',
    versionModele: 'latest',
    totalDecisions: 3,
    approuvees: 1,
    modifiees: 1,
    rejetees: 1,
    enAttente: 0,
    scorePourcentage: 50.0,
  },
  {
    rang: 0,
    systemeIaId: '3',
    nom: 'Claude',
    fournisseur: 'Anthropic',
    modele: 'claude-sonnet-4',
    versionModele: 'latest',
    totalDecisions: 3,
    approuvees: 1,
    modifiees: 0,
    rejetees: 2,
    enAttente: 0,
    scorePourcentage: 33.33,
  },
];

type SortField = 'rang' | 'nom' | 'totalDecisions' | 'approuvees' | 'rejetees' | 'scorePourcentage';
type SortDir = 'asc' | 'desc';

@Component({
  selector: 'app-comparaison',
  standalone: true,
  imports: [CommonModule, DecimalPipe, IconComponent],
  templateUrl: './comparaison.component.html',
  styleUrl: './comparaison.component.scss',
})
export class ComparaisonComponent {
  // ── State ────────────────────────────────────────────────────────────────
  readonly sortField = signal<SortField>('scorePourcentage');
  readonly sortDir   = signal<SortDir>('desc');

  // ── Derived ──────────────────────────────────────────────────────────────
  readonly agents = computed<AgentStats[]>(() => {
    const field = this.sortField();
    const dir   = this.sortDir();

    const sorted = [...DEMO_AGENTS].sort((a, b) => {
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

  /** SVG bar-chart: max bar height in px */
  readonly BAR_H  = 160;
  readonly BAR_W  = 56;
  readonly GAP    = 40;

  readonly chartBars = computed(() => {
    const list = [...DEMO_AGENTS].sort((a, b) => b.scorePourcentage - a.scorePourcentage);
    const max  = Math.max(...list.map(a => a.scorePourcentage), 1);
    return list.map((a, i) => ({
      ...a,
      x:      i * (this.BAR_W + this.GAP),
      height: Math.round((a.scorePourcentage / max) * this.BAR_H),
      color:  this.barColor(i),
    }));
  });

  readonly svgWidth = computed(() =>
    DEMO_AGENTS.length * (this.BAR_W + this.GAP) - this.GAP + 60
  );

  // ── Helpers ───────────────────────────────────────────────────────────────
  sort(field: SortField): void {
    if (this.sortField() === field) {
      this.sortDir.update(d => (d === 'asc' ? 'desc' : 'asc'));
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

  scoreClass(score: number): string {
    if (score >= 70) return 'score-high';
    if (score >= 40) return 'score-mid';
    return 'score-low';
  }

  private barColor(index: number): string {
    const palette = ['url(#grad0)', 'url(#grad1)', 'url(#grad2)', 'url(#grad3)'];
    return palette[index % palette.length];
  }
}
