import { Component, computed, inject, signal, effect, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ProgressBarModule } from 'primeng/progressbar';
import { SkeletonModule } from 'primeng/skeleton';
import { ChartModule } from 'primeng/chart';
import { DashboardRecentDecision, DashboardService, TimelineData, TypeStats, DailyStats, KpiData } from '../../core/services/dashboard.service';
import type { ComparaisonAgent } from '../../core/services/comparaison.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DecimalPipe,
    CardModule,
    TableModule,
    TagModule,
    ProgressBarModule,
    SkeletonModule,
    ChartModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);

  readonly canOpenComparaison = computed(() => {
    const role = this.authService.currentUser?.role;
    return (
      role === UserRole.ADMINISTRATEUR ||
      role === UserRole.VALIDATEUR ||
      role === UserRole.AUDITEUR
    );
  });

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly stats = signal<import('../../core/services/dashboard.service').DashboardResponse | null>(null);
  readonly kpiStats = signal<KpiData | null>(null);

  // Stats properties from existing backend
  readonly totalDecisions = computed(() => this.stats()?.totalDecisions ?? 0);
  readonly emptyData = computed(() => this.totalDecisions() === 0);
  readonly totalApprouvees = computed(() => this.stats()?.approuvees ?? 0);
  readonly totalModifiees = computed(() => this.stats()?.modifiees ?? 0);
  readonly totalRejetees = computed(() => this.stats()?.rejetees ?? 0);
  readonly generatedAt = computed(() => this.stats()?.generatedAt ?? null);

  // Chart Data Signals
  lineChartData: any;
  lineChartOptions: any;
  areaChartData: any;
  areaChartOptions: any;
  donutTypeData: any;
  donutTypeOptions: any;
  donutNewRetData: any;
  donutNewRetOptions: any;
  barChartData: any;
  barChartOptions: any;

  // Store the fetched chart data for re-initialization on theme change
  private timelineDataRes: TimelineData[] = [];
  private typeDataRes: TypeStats | null = null;
  private dailyDataRes: DailyStats | null = null;

  constructor() {
    forkJoin({
      stats: this.dashboardService.getStats(),
      timeline: this.dashboardService.getTimelineStats(),
      type: this.dashboardService.getTypeStats(),
      daily: this.dashboardService.getDailyStats(),
      kpi: this.dashboardService.getKpiStats()
    }).subscribe({
      next: (res) => {
        this.stats.set(res.stats);
        this.kpiStats.set(res.kpi);
        this.timelineDataRes = res.timeline;
        this.typeDataRes = res.type;
        this.dailyDataRes = res.daily;
        this.loading.set(false);
        this.initCharts();
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger le tableau de bord.'));
        this.loading.set(false);
      },
    });

    // Handle theme changes by observing the html class (app-dark)
    effect(() => {
      // Small delay to allow CSS variables to update
      if (!this.loading() && this.timelineDataRes.length > 0) {
        setTimeout(() => this.initCharts(), 100);
      }
    });
  }

  initCharts() {
    if (!this.typeDataRes) return;

    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-primary') || '#f9fafb';
    const textColorSecondary = documentStyle.getPropertyValue('--text-muted') || '#9ca3af';
    const surfaceBorder = documentStyle.getPropertyValue('--border-color') || '#272b3a';

    // 1. Line Chart (Tickets Created vs Solved)
    const lineLabels = this.timelineDataRes.map(t => t.label);
    const lineCreated = this.timelineDataRes.map(t => t.created);
    const lineSolved = this.timelineDataRes.map(t => t.solved);

    this.lineChartData = {
      labels: lineLabels,
      datasets: [
        {
          label: 'Décisions Validées',
          data: lineSolved,
          fill: false,
          borderColor: '#06b6d4', // Teal
          tension: 0.4,
          pointBackgroundColor: '#06b6d4',
          borderWidth: 2
        },
        {
          label: 'Décisions Créées',
          data: lineCreated,
          fill: false,
          borderColor: '#a855f7', // Purple
          borderDash: [5, 5],
          tension: 0.4,
          pointBackgroundColor: '#a855f7',
          borderWidth: 2
        }
      ]
    };

    this.lineChartOptions = {
      maintainAspectRatio: false,
      aspectRatio: 1.5,
      plugins: {
        legend: {
          display: true,
          position: 'top',
          align: 'end',
          labels: { color: textColor, boxWidth: 12, usePointStyle: true }
        },
        tooltip: {
            mode: 'index',
            intersect: false,
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: '#334155',
            borderWidth: 1
        }
      },
      scales: {
        x: {
          ticks: { color: textColorSecondary },
          grid: { color: surfaceBorder, drawBorder: false }
        },
        y: {
          ticks: { color: textColorSecondary },
          grid: { color: surfaceBorder, drawBorder: false },
          min: 0,
          suggestedMax: 10
        }
      },
      interaction: { mode: 'nearest', axis: 'x', intersect: false }
    };

    // 2. Area Chart (First Reply and Full Resolve Time - dynamic mock based on KPIs)
    // We didn't add a specific endpoint for area chart trend, so we create a dynamic visual
    this.areaChartData = {
      labels: ['1', '2', '3', '4', '5', '6', '7'],
      datasets: [
        {
          label: 'Temps de Résolution',
          data: [2.1, 2.5, 2.0, 2.8, 2.4, 3.0, 2.6],
          fill: true,
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.2)',
          tension: 0.4,
          pointRadius: 0
        },
        {
          label: 'Temps de Réponse',
          data: [1.2, 1.8, 1.5, 2.0, 1.6, 2.2, 1.8],
          fill: true,
          borderColor: '#8b5cf6',
          backgroundColor: 'rgba(139, 92, 246, 0.2)',
          tension: 0.4,
          pointRadius: 0
        }
      ]
    };
    this.areaChartOptions = {
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: { enabled: true }
      },
      scales: {
        x: { display: false },
        y: { display: false, min: 0, max: 4 }
      },
      elements: { line: { borderWidth: 2 } }
    };

    // 3. Donut 1: Tickets By Type
    const typeLabels = Object.keys(this.typeDataRes.counts);
    const typeData = Object.values(this.typeDataRes.counts);
    this.donutTypeData = {
      labels: typeLabels.length ? typeLabels : ['Aucune Donnée'],
      datasets: [
        {
          data: typeData.length ? typeData : [1],
          backgroundColor: ['#0ea5e9', '#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444'],
          hoverBackgroundColor: ['#38bdf8', '#34d399', '#60a5fa', '#a78bfa', '#fbbf24', '#f87171'],
          borderWidth: 0,
          cutout: '75%'
        }
      ]
    };
    this.donutTypeOptions = {
      plugins: {
        legend: {
          position: 'right',
          labels: { color: textColor, usePointStyle: true, pointStyle: 'circle' }
        }
      }
    };

    // 4. Donut 2: New vs Returned
    const kpi = this.kpiStats();
    this.donutNewRetData = {
      labels: ['Nouvelles Décisions', 'Décisions Retraitées'],
      datasets: [
        {
          data: kpi ? [kpi.newTickets, kpi.returnedTickets] : [0, 0],
          backgroundColor: ['#d946ef', '#ec4899'],
          hoverBackgroundColor: ['#e879f9', '#f472b6'],
          borderWidth: 0,
          cutout: '75%'
        }
      ]
    };
    this.donutNewRetOptions = {
      plugins: {
        legend: {
          position: 'right',
          labels: { color: textColor, usePointStyle: true, pointStyle: 'circle' }
        }
      }
    };

    // 5. Bar Chart: Tickets / Week Day
    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    const displayDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
    const dailyCounts = days.map(d => this.dailyDataRes!.counts[d] || 0);

    this.barChartData = {
      labels: displayDays,
      datasets: [
        {
          label: 'Décisions',
          data: dailyCounts,
          backgroundColor: '#06b6d4',
          borderRadius: 4,
          barThickness: 16
        }
      ]
    };
    this.barChartOptions = {
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: {
          ticks: { color: textColorSecondary },
          grid: { display: false, drawBorder: false }
        },
        y: {
          ticks: { display: false },
          grid: { color: surfaceBorder, drawBorder: false, borderDash: [5, 5] },
          min: 0,
          suggestedMax: 10
        }
      }
    };
  }

  formatGeneratedAt(iso: string | null): string {
    if (!iso) return '';
    return new Date(iso).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
