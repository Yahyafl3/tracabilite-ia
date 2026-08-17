import { Component, Input, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Card } from 'primeng/card';
import { UIChart } from 'primeng/chart';
import { TranslatePipe } from '@ngx-translate/core';
import type { AuditRecentItemResponse } from '../../../core/services/audit.service';
import { StatutDecisionEnum } from '../../../core/models/decision.models';
import { TranslationService } from '../../../core/i18n/translation.service';

type StatusBucket = 'approuvee' | 'enAttente' | 'rejetee' | 'modifiee' | 'autres';

/**
 * Le backend expose les statuts canoniques (VALIDEE, EN_ATTENTE_VALIDATION) et
 * leurs équivalents legacy (APPROUVEE, EN_ATTENTE). Les deux doivent alimenter
 * la même part du graphique.
 */
const STATUS_BUCKETS: Record<StatutDecisionEnum, StatusBucket> = {
  [StatutDecisionEnum.VALIDEE]: 'approuvee',
  [StatutDecisionEnum.APPROUVEE]: 'approuvee',
  [StatutDecisionEnum.EN_ATTENTE_VALIDATION]: 'enAttente',
  [StatutDecisionEnum.EN_ATTENTE]: 'enAttente',
  [StatutDecisionEnum.BROUILLON]: 'enAttente',
  [StatutDecisionEnum.EN_ANALYSE]: 'enAttente',
  [StatutDecisionEnum.ANALYSEE]: 'enAttente',
  [StatutDecisionEnum.REJETEE]: 'rejetee',
  [StatutDecisionEnum.MODIFIEE]: 'modifiee',
  [StatutDecisionEnum.A_REVOIR]: 'modifiee',
  [StatutDecisionEnum.ARCHIVEE]: 'autres',
};

interface StatisticsData {
  totalDecisions: number;
  validIntegrity: number;
  invalidIntegrity: number;
  chainIntact: boolean;
  byStatus: Record<StatutDecisionEnum, number>;
  byBucket: Record<StatusBucket, number>;
  byDay: { date: string; count: number; validCount: number }[];
  integrityRate: number;
}

@Component({
  selector: 'app-audit-statistics',
  standalone: true,
  imports: [CommonModule, Card, UIChart, TranslatePipe],
  template: `
    <div class="statistics-grid">
      <p-card styleClass="chart-card">
        <ng-template pTemplate="title">
          <div class="card-header mb-4">
            <h3 class="text-lg font-semibold text-gray-800 m-0">{{ 'audit.byStatus' | translate }}</h3>
            <span class="card-hint">{{ 'audit.nDecisions' | translate:{ count: stats().totalDecisions } }}</span>
          </div>
        </ng-template>
        <p-chart type="doughnut" [data]="statusChartData()" [options]="chartOptions" [style]="{ width: '100%', height: '280px' }" />
      </p-card>

      <p-card styleClass="chart-card">
        <ng-template pTemplate="title">
          <div class="card-header mb-4">
            <h3 class="text-lg font-semibold text-gray-800 m-0">{{ 'audit.integrityTrend' | translate }}</h3>
            <span class="card-hint">{{ 'audit.last7' | translate }}</span>
          </div>
        </ng-template>
        <p-chart type="line" [data]="integrityTrendData()" [options]="lineChartOptions" [style]="{ width: '100%', height: '280px' }" />
      </p-card>

      <p-card styleClass="chart-card gauge-card">
        <ng-template pTemplate="title">
          <div class="card-header mb-4">
            <h3 class="text-lg font-semibold text-gray-800 m-0">{{ 'audit.integrityRate' | translate }}</h3>
            <span class="card-hint">{{ 'audit.successValidations' | translate }}</span>
          </div>
        </ng-template>
        <div class="gauge-container">
          <div class="gauge-circle" [class.gauge-high]="stats().integrityRate >= 90" [class.gauge-medium]="stats().integrityRate >= 70 && stats().integrityRate < 90" [class.gauge-low]="stats().integrityRate < 70">
            <svg viewBox="0 0 200 200" class="gauge-svg">
              <circle cx="100" cy="100" r="80" fill="none" stroke="#e5e7eb" stroke-width="20" />
              <circle
                cx="100"
                cy="100"
                r="80"
                fill="none"
                stroke="currentColor"
                stroke-width="20"
                stroke-dasharray="502.65"
                [attr.stroke-dashoffset]="502.65 - (502.65 * stats().integrityRate / 100)"
                transform="rotate(-90 100 100)"
                stroke-linecap="round"
              />
            </svg>
            <div class="gauge-value">
              <span class="gauge-percentage">{{ stats().integrityRate.toFixed(1) }}%</span>
              <span class="gauge-label">{{ 'audit.integrity' | translate }}</span>
            </div>
          </div>
          <div class="gauge-stats">
            <div class="gauge-stat">
              <i class="pi pi-check-circle"></i>
              <span>{{ 'audit.nValid' | translate:{ count: stats().validIntegrity } }}</span>
            </div>
            <div class="gauge-stat gauge-stat-danger">
              <i class="pi pi-times-circle"></i>
              <span>{{ 'audit.nInvalid' | translate:{ count: stats().invalidIntegrity } }}</span>
            </div>
          </div>
        </div>
      </p-card>

      <p-card styleClass="chart-card">
        <ng-template pTemplate="title">
          <div class="card-header mb-4">
            <h3 class="text-lg font-semibold text-gray-800 m-0">{{ 'audit.dailyActivity' | translate }}</h3>
            <span class="card-hint">{{ 'audit.perDay' | translate }}</span>
          </div>
        </ng-template>
        <p-chart type="bar" [data]="activityChartData()" [options]="barChartOptions" [style]="{ width: '100%', height: '280px' }" />
      </p-card>
    </div>
  `,
  styles: [`
    .statistics-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }

    :host ::ng-deep .chart-card {
      height: 100%;
    }

    :host ::ng-deep .chart-card .p-card-header {
      padding: 1.25rem 1.25rem 0.75rem;
    }

    :host ::ng-deep .chart-card .p-card-body {
      padding: 1rem 1.25rem 1.25rem;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-header h3 {
      font-size: 1rem;
      font-weight: 600;
      color: var(--ink);
    }

    .card-hint {
      font-size: 0.8125rem;
      color: var(--muted);
    }

    .gauge-card {
      grid-column: span 1;
    }

    .gauge-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1.5rem;
      padding: 1rem 0;
    }

    .gauge-circle {
      position: relative;
      width: 150px;
      height: 150px;
      flex-shrink: 0;
    }

    .gauge-svg {
      width: 100%;
      height: 100%;
    }

    .gauge-circle circle:last-child {
      transition: stroke-dashoffset 0.8s ease;
    }

    .gauge-high circle:last-child {
      stroke: #10b981;
    }

    .gauge-medium circle:last-child {
      stroke: #f59e0b;
    }

    .gauge-low circle:last-child {
      stroke: #ef4444;
    }

    .gauge-value {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      text-align: center;
      width: 90%;
      pointer-events: none;
    }

    .gauge-percentage {
      display: block;
      font-size: 1.35rem;
      font-weight: 800;
      color: var(--ink);
      line-height: 1;
      white-space: nowrap;
    }

    .gauge-label {
      display: block;
      font-size: 0.65rem;
      font-weight: 600;
      color: var(--muted);
      margin-top: 0.25rem;
      letter-spacing: 0.06em;
      text-transform: uppercase;
    }

    .gauge-stats {
      display: flex;
      gap: 2rem;
      align-items: center;
    }

    .gauge-stat {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
      color: var(--ink-soft);
    }

    .gauge-stat i {
      font-size: 1.125rem;
      color: var(--chip-approved-fg);
    }

    .gauge-stat-danger i {
      color: var(--chip-rejected-fg);
    }

    @media (max-width: 1200px) {
      .statistics-grid {
        grid-template-columns: 1fr;
      }
    }
  `],
})
export class AuditStatisticsComponent {
  private readonly i18n = inject(TranslationService);

  @Input() set items(value: AuditRecentItemResponse[]) {
    this.auditItems.set(value);
  }

  private readonly auditItems = signal<AuditRecentItemResponse[]>([]);

  readonly stats = computed(() => this.calculateStatistics(this.auditItems()));

  readonly statusChartData = computed(() => {
    this.i18n.currentLang();
    const stats = this.stats();
    if (stats.totalDecisions === 0) {
      return {
        labels: [this.i18n.t('audit.noData')],
        datasets: [{ data: [1], backgroundColor: ['#e5e7eb'], hoverBackgroundColor: ['#d1d5db'], borderWidth: 0 }]
      };
    }

    const labels = [
      this.i18n.t('audit.statusApproved'),
      this.i18n.t('audit.statusPending'),
      this.i18n.t('audit.statusRejected'),
      this.i18n.t('audit.statusModified'),
    ];
    const colors = ['#10b981', '#f59e0b', '#ef4444', '#6366f1'];
    const data = [
      stats.byBucket.approuvee,
      stats.byBucket.enAttente,
      stats.byBucket.rejetee,
      stats.byBucket.modifiee,
    ];

    if (stats.byBucket.autres > 0) {
      labels.push(this.i18n.t('audit.others'));
      colors.push('#94a3b8');
      data.push(stats.byBucket.autres);
    }

    return {
      labels,
      datasets: [{ data, backgroundColor: colors, borderWidth: 0 }],
    };
  });

  readonly integrityTrendData = computed(() => {
    this.i18n.currentLang();
    const stats = this.stats();
    return {
      labels: stats.byDay.map((d) => this.formatShortDate(d.date)),
      datasets: [
        {
          label: this.i18n.t('audit.validCount'),
          data: stats.byDay.map((d) => d.validCount),
          borderColor: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.4,
          fill: true,
        },
        {
          label: this.i18n.t('audit.kpiTotal'),
          data: stats.byDay.map((d) => d.count),
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
          fill: true,
        },
      ],
    };
  });

  readonly activityChartData = computed(() => {
    this.i18n.currentLang();
    const stats = this.stats();
    return {
      labels: stats.byDay.map((d) => this.formatShortDate(d.date)),
      datasets: [
        {
          label: this.i18n.t('audit.dailyCount'),
          data: stats.byDay.map((d) => d.count),
          backgroundColor: '#6366f1',
          borderRadius: 4,
          maxBarThickness: 40,
        },
      ],
    };
  });

  readonly chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '75%',
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          padding: 15,
          usePointStyle: true,
          font: { size: 12 },
        },
      },
    },
  };

  readonly lineChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: {
          padding: 15,
          usePointStyle: true,
          font: { size: 12 },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { stepSize: 1 },
      },
    },
  };

  readonly barChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { stepSize: 1 },
      },
    },
  };

  private calculateStatistics(items: AuditRecentItemResponse[]): StatisticsData {
    const totalDecisions = items.length;
    const validIntegrity = items.filter((i) => i.integrityValid).length;
    const invalidIntegrity = totalDecisions - validIntegrity;
    const integrityRate = totalDecisions > 0 ? (validIntegrity / totalDecisions) * 100 : 100;

    // Group by status
    const byStatus = items.reduce(
      (acc, item) => {
        acc[item.statutValidation] = (acc[item.statutValidation] || 0) + 1;
        return acc;
      },
      {} as Record<StatutDecisionEnum, number>
    );

    const byBucket = items.reduce(
      (acc, item) => {
        const bucket = STATUS_BUCKETS[item.statutValidation] ?? 'autres';
        acc[bucket] += 1;
        return acc;
      },
      { approuvee: 0, enAttente: 0, rejetee: 0, modifiee: 0, autres: 0 } as Record<StatusBucket, number>
    );

    // Group by day (last 7 days)
    const byDay = this.groupByDay(items);

    return {
      totalDecisions,
      validIntegrity,
      invalidIntegrity,
      chainIntact: invalidIntegrity === 0,
      byStatus,
      byBucket,
      byDay,
      integrityRate,
    };
  }

  private groupByDay(items: AuditRecentItemResponse[]): { date: string; count: number; validCount: number }[] {
    const dayMap = new Map<string, { count: number; validCount: number }>();

    // Get last 7 days
    const today = new Date();
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      const dateStr = date.toISOString().split('T')[0];
      dayMap.set(dateStr, { count: 0, validCount: 0 });
    }

    // Count items by day
    items.forEach((item) => {
      const dateStr = new Date(item.timestamp).toISOString().split('T')[0];
      const existing = dayMap.get(dateStr);
      if (existing) {
        existing.count++;
        if (item.integrityValid) {
          existing.validCount++;
        }
      }
    });

    return Array.from(dayMap.entries())
      .map(([date, data]) => ({ date, ...data }))
      .sort((a, b) => a.date.localeCompare(b.date));
  }

  private formatShortDate(isoDate: string): string {
    const date = new Date(isoDate);
    return date.toLocaleDateString(this.i18n.localeId(), { day: 'numeric', month: 'short' });
  }
}
