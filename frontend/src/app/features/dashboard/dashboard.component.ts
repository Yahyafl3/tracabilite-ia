import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../shared/icon.component';
import {
  PageHeaderComponent,
  KpiCardComponent,
  StatusBadgeComponent,
  ErrorStateComponent,
  LoadingSkeletonComponent,
} from '../../shared/ui';
import { DashboardRecentDecision, DashboardService } from '../../core/services/dashboard.service';
import type { ComparaisonAgent } from '../../core/services/comparaison.service';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DecimalPipe,
    RouterLink,
    IconComponent,
    PageHeaderComponent,
    KpiCardComponent,
    StatusBadgeComponent,
    ErrorStateComponent,
    LoadingSkeletonComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly stats = signal<import('../../core/services/dashboard.service').DashboardResponse | null>(null);

  readonly totalDecisions = computed(() => this.stats()?.totalDecisions ?? 0);
  readonly totalApprouvees = computed(() => this.stats()?.approuvees ?? 0);
  readonly totalModifiees = computed(() => this.stats()?.modifiees ?? 0);
  readonly totalRejetees = computed(() => this.stats()?.rejetees ?? 0);
  readonly totalEnAttente = computed(() => this.stats()?.enAttente ?? 0);
  readonly totalValideesHumainement = computed(() => this.totalApprouvees() + this.totalModifiees());
  readonly tauxValidation = computed(() => this.stats()?.tauxValidation ?? 0);
  readonly agentsLabel = computed(() => this.stats()?.agentsLabel ?? '');
  readonly agentsActifs = computed(() => this.stats()?.agentsActifs ?? 0);
  readonly hashChainIntact = computed(() => this.stats()?.hashChainIntact ?? false);
  readonly generatedAt = computed(() => this.stats()?.generatedAt ?? null);
  readonly agentPerformance = computed(() => this.stats()?.agentPerformance ?? []);
  readonly recentDecisions = computed(() => this.stats()?.recentDecisions ?? []);

  readonly kpiCards = computed(() => [
    {
      label: 'Total décisions',
      value: this.totalDecisions(),
      unit: '',
      icon: 'file-text',
      accent: 'indigo' as const,
      hint: `${this.totalDecisions()} enregistrées`,
    },
    {
      label: 'En attente',
      value: this.totalEnAttente(),
      unit: '',
      icon: 'history',
      accent: 'amber' as const,
      hint: 'À valider',
    },
    {
      label: 'Validées humainement',
      value: this.totalValideesHumainement(),
      unit: '',
      icon: 'check-circle',
      accent: 'green' as const,
      hint: `${this.totalApprouvees()} approuvées + ${this.totalModifiees()} modifiées`,
    },
    {
      label: 'Rejetées',
      value: this.totalRejetees(),
      unit: '',
      icon: 'shield-alert',
      accent: 'danger' as const,
      hint: `${this.tauxValidation()}% taux de validation`,
    },
    {
      label: 'Agents configurés',
      value: this.agentsActifs(),
      unit: '',
      icon: 'server',
      accent: 'violet' as const,
      hint: this.agentsLabel(),
    },
    {
      label: 'Intégrité chaîne',
      value: this.hashChainIntact() ? 'Oui' : 'Non',
      unit: '',
      icon: 'shield-check',
      accent: this.hashChainIntact() ? 'green' as const : 'danger' as const,
      hint: 'SHA-256',
    },
  ]);

  readonly DONUT_R = 60;
  readonly DONUT_GAP = 16;
  readonly DONUT_CX = 90;
  readonly DONUT_CY = 90;

  readonly donutSegments = computed(() => {
    const data = [
      { label: 'Approuvées', value: this.totalApprouvees(), color: '#10b981' },
      { label: 'Modifiées', value: this.totalModifiees(), color: '#f59e0b' },
      { label: 'Rejetées', value: this.totalRejetees(), color: '#ef4444' },
      { label: 'En attente', value: this.totalEnAttente(), color: '#94a3b8' },
    ].filter((d) => d.value > 0);

    const total = data.reduce((s, d) => s + d.value, 0);
    if (total === 0) {
      return [];
    }

    const r = this.DONUT_R;
    const ir = r - this.DONUT_GAP;
    const cx = this.DONUT_CX;
    const cy = this.DONUT_CY;
    let angle = -Math.PI / 2;

    return data.map((seg) => {
      const fraction = seg.value / total;
      const sweep = fraction * 2 * Math.PI;
      const x1o = cx + r * Math.cos(angle);
      const y1o = cy + r * Math.sin(angle);
      const x1i = cx + ir * Math.cos(angle);
      const y1i = cy + ir * Math.sin(angle);
      angle += sweep;
      const x2o = cx + r * Math.cos(angle);
      const y2o = cy + r * Math.sin(angle);
      const x2i = cx + ir * Math.cos(angle);
      const y2i = cy + ir * Math.sin(angle);
      const large = sweep > Math.PI ? 1 : 0;
      const path = `M ${x1o} ${y1o} A ${r} ${r} 0 ${large} 1 ${x2o} ${y2o} L ${x2i} ${y2i} A ${ir} ${ir} 0 ${large} 0 ${x1i} ${y1i} Z`;
      return { ...seg, path, pct: Math.round(fraction * 100) };
    });
  });

  constructor() {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger le tableau de bord.'));
        this.loading.set(false);
      },
    });
  }

  statutLabel(s: DashboardRecentDecision['statutValidation']): string {
    return {
      APPROUVEE: 'Approuvée',
      MODIFIEE: 'Modifiée',
      REJETEE: 'Rejetée',
      EN_ATTENTE: 'En attente',
      BROUILLON: 'Brouillon',
    }[s];
  }

  statutClass(s: DashboardRecentDecision['statutValidation']): string {
    return {
      APPROUVEE: 'chip--approved',
      MODIFIEE: 'chip--modified',
      REJETEE: 'chip--rejected',
      EN_ATTENTE: 'chip--pending',
      BROUILLON: 'chip--pending',
    }[s];
  }

  agentAvatarClass(index: number): string {
    return `avatar--${(index % 4) + 1}`;
  }

  perfAvatarClass(index: number): string {
    return `perf-avatar--${(index % 3) + 1}`;
  }

  scoreClass(score: number): string {
    if (score >= 70) return 'score-chip--high';
    if (score >= 40) return 'score-chip--mid';
    return 'score-chip--low';
  }

  barFillClass(score: number): string {
    if (score >= 70) return 'perf-bar__fill--high';
    if (score >= 40) return 'perf-bar__fill--mid';
    return 'perf-bar__fill--low';
  }

  agentReussies(agent: ComparaisonAgent): number {
    return Math.round(agent.totalDecisions * agent.scorePourcentage / 100);
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' });
  }

  formatGeneratedAt(iso: string | null): string {
    if (!iso) return '';
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  agentInitial(agent: ComparaisonAgent): string {
    return agent.nom.charAt(0).toUpperCase();
  }
}
