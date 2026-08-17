import { Component, computed, inject, signal, effect, ChangeDetectorRef, PLATFORM_ID } from '@angular/core';
import { CommonModule, DecimalPipe, isPlatformBrowser } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ProgressBarModule } from 'primeng/progressbar';
import { SkeletonModule } from 'primeng/skeleton';
import { ChartModule } from 'primeng/chart';
import { ButtonModule } from 'primeng/button';
import { TranslatePipe } from '@ngx-translate/core';
import { DashboardRecentDecision, DashboardService, TimelineData, TypeStats, DailyStats, KpiData } from '../../core/services/dashboard.service';
import type { ComparaisonAgent } from '../../core/services/comparaison.service';
import { AuthService } from '../../core/services/auth.service';
import { TranslationService } from '../../core/i18n/translation.service';
import { LayoutService } from '../../layout/layout.service';
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
    ChartModule,
    ButtonModule,
    TranslatePipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);
  private readonly layoutService = inject(LayoutService);
  private readonly i18n = inject(TranslationService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  readonly canOpenComparaison = computed(() => {
    const role = this.authService.currentUser?.role;
    return (
      role === UserRole.ADMINISTRATEUR ||
      role === UserRole.AUDITEUR
    );
  });

  readonly canCreateDecision = computed(() => {
    const role = this.authService.currentUser?.role;
    return (
      role === UserRole.ADMINISTRATEUR ||
      role === UserRole.AGENT_CREDIT ||
      role === UserRole.AGENT_SANTE ||
      role === UserRole.AGENT_PEDAGOGIQUE
    );
  });

  readonly dashboardTitle = computed(() => {
    this.i18n.currentLang();
    const role = this.authService.currentUser?.role;
    switch (role) {
      case UserRole.AGENT_CREDIT:
      case UserRole.RESPONSABLE_CREDIT:
        return this.i18n.t('dashboard.titles.credit');
      case UserRole.AGENT_SANTE:
      case UserRole.PROFESSIONNEL_SANTE:
        return this.i18n.t('dashboard.titles.health');
      case UserRole.AGENT_PEDAGOGIQUE:
      case UserRole.RESPONSABLE_PEDAGOGIQUE:
        return this.i18n.t('dashboard.titles.education');
      case UserRole.AUDITEUR:
        return this.i18n.t('dashboard.titles.audit');
      case UserRole.ADMINISTRATEUR:
      default:
        return this.i18n.t('dashboard.titles.admin');
    }
  });

  readonly activeDomainCode = computed(() => {
    const role = this.authService.currentUser?.role;
    switch (role) {
      case UserRole.AGENT_CREDIT:
      case UserRole.RESPONSABLE_CREDIT:
        return 'CREDIT';
      case UserRole.AGENT_SANTE:
      case UserRole.PROFESSIONNEL_SANTE:
        return 'MEDICAL';
      case UserRole.AGENT_PEDAGOGIQUE:
      case UserRole.RESPONSABLE_PEDAGOGIQUE:
        return 'EDUCATION';
      case UserRole.AUDITEUR:
        return 'AUDIT';
      case UserRole.ADMINISTRATEUR:
      default:
        return 'ADMIN';
    }
  });

  readonly userRole = computed(() => this.authService.currentUser?.role);

  readonly isDomainRestricted = computed(() => {
    const role = this.authService.currentUser?.role;
    return role !== UserRole.ADMINISTRATEUR && role !== UserRole.AUDITEUR;
  });

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly stats = signal<import('../../core/services/dashboard.service').DashboardResponse | null>(null);
  readonly kpiStats = signal<KpiData | null>(null);

  // Stats properties from existing backend
  readonly totalDecisions = computed(() => this.stats()?.totalDecisions ?? 0);
  readonly emptyData = computed(() => this.totalDecisions() === 0);

  /** Part des dossiers signés dont l'empreinte se recalcule à l'identique. */
  readonly integrityRate = computed(() => {
    const kpi = this.kpiStats();
    if (!kpi || !kpi.integrityTotal) {
      return 100;
    }
    return Math.round((kpi.integrityVerified / kpi.integrityTotal) * 1000) / 10;
  });

  /** Part des arbitrages humains ayant confirmé la suggestion de l'IA. */
  readonly aiAgreementRate = computed(() => {
    const kpi = this.kpiStats();
    const arbitrated = (kpi?.aiAgreement ?? 0) + (kpi?.aiDisagreement ?? 0);
    if (!arbitrated) {
      return 0;
    }
    return Math.round((kpi!.aiAgreement / arbitrated) * 1000) / 10;
  });
  readonly totalApprouvees = computed(() => this.stats()?.approuvees ?? 0);
  readonly totalModifiees = computed(() => this.stats()?.modifiees ?? 0);
  readonly totalRejetees = computed(() => this.stats()?.rejetees ?? 0);
  readonly generatedAt = computed(() => this.stats()?.generatedAt ?? null);

  readonly currentConfiance = computed(() => {
    const arr = this.kpiStats()?.sparklines?.['confiance'] || [];
    return arr.length > 0 ? arr[arr.length - 1] : 0;
  });
  
  readonly currentConformite = computed(() => {
    const arr = this.kpiStats()?.sparklines?.['conformite'] || [];
    return arr.length > 0 ? arr[arr.length - 1] : 0;
  });
  
  readonly currentRisque = computed(() => {
    const arr = this.kpiStats()?.sparklines?.['risque'] || [];
    return arr.length > 0 ? arr[arr.length - 1] : 0;
  });

  readonly trendConfiance = computed(() => {
    const arr = this.kpiStats()?.sparklines?.['confiance'] || [];
    if (arr.length < 2) return 0;
    const diff = arr[arr.length - 1] - arr[arr.length - 2];
    return Math.round(diff * 10) / 10;
  });

  readonly trendConformite = computed(() => {
    const arr = this.kpiStats()?.sparklines?.['conformite'] || [];
    if (arr.length < 2) return 0;
    const diff = arr[arr.length - 1] - arr[arr.length - 2];
    return Math.round(diff * 10) / 10;
  });

  readonly trendRisque = computed(() => {
    const arr = this.kpiStats()?.sparklines?.['risque'] || [];
    if (arr.length < 2) return 0;
    const diff = arr[arr.length - 1] - arr[arr.length - 2];
    return Math.round(diff * 10) / 10;
  });

  // Chart Data Signals
  lineChartData: any;
  lineChartOptions: any;
  donutRiskData: any;
  donutRiskOptions: any;
  donutTypeData: any;
  donutTypeOptions: any;
  donutAiAlignmentData: any;
  donutAiAlignmentOptions: any;
  barChartData: any;
  barChartOptions: any;

  // Sparkline Chart Data Signals
  sparklineConfianceData: any;
  sparklineConformiteData: any;
  sparklineRisqueData: any;
  sparklineOptions: any;

  radarChartData: any;
  radarChartOptions: any;

  gaugeChartData: any;
  gaugeChartOptions: any;

  anomaliesChartData: any;
  anomaliesChartOptions: any;

  // Store the fetched chart data for re-initialization on theme change
  private timelineDataRes: TimelineData[] = [];
  private typeDataRes: TypeStats | null = null;
  private dailyDataRes: DailyStats | null = null;

  constructor() {
    this.loadDashboardData();

    // Handle theme changes by observing the html class (app-dark)
    effect(() => {
      this.layoutService.isDarkTheme();
      this.i18n.currentLang();

      if (!this.loading() && this.timelineDataRes.length >= 0 && this.typeDataRes) {
        setTimeout(() => {
          this.initCharts();
          this.cdr.markForCheck();
        }, 50);
      }
    });
  }

  loadDashboardData(): void {
    this.loading.set(true);
    this.error.set(null);

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
        this.cdr.markForCheck();

        // Allow Angular to render the @else branch and compute DOM dimensions before Chart.js canvas init
        setTimeout(() => {
          this.initCharts();
          this.cdr.markForCheck();
        }, 50);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('dashboard.loadErrorFallback')));
        this.loading.set(false);
        this.cdr.markForCheck();
      },
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
          label: this.i18n.t('dashboard.charts.validated'),
          data: lineSolved,
          fill: false,
          borderColor: '#06b6d4', // Teal
          tension: 0.4,
          pointBackgroundColor: '#06b6d4',
          borderWidth: 2
        },
        {
          label: this.i18n.t('dashboard.charts.created'),
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
      locale: this.i18n.localeId(),
      plugins: {
        legend: {
          display: true,
          position: 'top',
          align: this.i18n.isRtl() ? 'start' : 'end',
          rtl: this.i18n.isRtl(),
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

    // 2. Risk Breakdown Donut Chart
    const kpiData = this.kpiStats();
    const riskBreakdown = kpiData?.riskBreakdown || {};
    const riskLabels = Object.keys(riskBreakdown);
    const riskValues = Object.values(riskBreakdown);
    const sumRisk = riskValues.reduce((a, b) => a + b, 0);
    
    if (sumRisk === 0) {
      this.donutRiskData = {
        labels: [this.i18n.t('dashboard.charts.noData')],
        datasets: [{ data: [1], backgroundColor: ['#e5e7eb'], hoverBackgroundColor: ['#d1d5db'], borderWidth: 0, cutout: '75%' }]
      };
    } else {
      const bgColors = [];
      const hoverColors = [];
      for (const label of riskLabels) {
        if (label === 'Élevé') { bgColors.push('#ef4444'); hoverColors.push('#f87171'); }
        else if (label === 'Moyen' || label === 'Modéré') { bgColors.push('#f59e0b'); hoverColors.push('#fbbf24'); }
        else if (label === 'Faible') { bgColors.push('#10b981'); hoverColors.push('#34d399'); }
        else { bgColors.push('#6b7280'); hoverColors.push('#9ca3af'); }
      }
      this.donutRiskData = {
        labels: riskLabels.map((label) => this.translateRiskLabel(label)),
        datasets: [{
          data: riskValues,
          backgroundColor: bgColors,
          hoverBackgroundColor: hoverColors,
          borderWidth: 0,
          cutout: '75%'
        }]
      };
    }

    this.donutRiskOptions = {
      maintainAspectRatio: false,
      cutout: '75%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: textColor, usePointStyle: true, pointStyle: 'circle' }
        }
      }
    };

    // 3. Donut 1: Tickets By Type
    const typeLabels = Object.keys(this.typeDataRes.counts);
    const typeData = Object.values(this.typeDataRes.counts);
    const sumType = typeData.reduce((a, b) => a + b, 0);

    if (sumType === 0) {
      this.donutTypeData = {
        labels: [this.i18n.t('dashboard.charts.noData')],
        datasets: [{ data: [1], backgroundColor: ['#e5e7eb'], hoverBackgroundColor: ['#d1d5db'], borderWidth: 0, cutout: '75%' }]
      };
    } else {
      this.donutTypeData = {
        labels: typeLabels,
        datasets: [{
          data: typeData,
          backgroundColor: ['#0ea5e9', '#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#ef4444'],
          hoverBackgroundColor: ['#38bdf8', '#34d399', '#60a5fa', '#a78bfa', '#fbbf24', '#f87171'],
          borderWidth: 0,
          cutout: '75%'
        }]
      };
    }
    this.donutTypeOptions = {
      maintainAspectRatio: false,
      cutout: '75%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: textColor, usePointStyle: true, pointStyle: 'circle' }
        }
      }
    };

    // 4. Alignement des arbitrages humains avec la suggestion de l'IA
    const kpi = this.kpiStats();
    if (!kpi) return;
    const arbitrated = kpi.aiAgreement + kpi.aiDisagreement;
    if (arbitrated + kpi.aiNotArbitrated === 0) {
      this.donutAiAlignmentData = {
        labels: [this.i18n.t('dashboard.charts.noData')],
        datasets: [{ data: [1], backgroundColor: ['#e5e7eb'], hoverBackgroundColor: ['#d1d5db'], borderWidth: 0, cutout: '75%' }]
      };
    } else {
      this.donutAiAlignmentData = {
        labels: [
          this.i18n.t('dashboard.charts.agreeWithAi'),
          this.i18n.t('dashboard.charts.assumedGap'),
          this.i18n.t('dashboard.charts.awaitingReview'),
        ],
        datasets: [
          {
            data: [kpi.aiAgreement, kpi.aiDisagreement, kpi.aiNotArbitrated],
            backgroundColor: ['#10b981', '#f59e0b', '#cbd5e1'],
            hoverBackgroundColor: ['#34d399', '#fbbf24', '#e2e8f0'],
            borderWidth: 0,
            hoverOffset: 4,
            cutout: '75%'
          }
        ]
      };
    }
    this.donutAiAlignmentOptions = {
      maintainAspectRatio: false,
      cutout: '75%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: textColor, usePointStyle: true, pointStyle: 'circle' }
        }
      }
    };

    // 5. Bar Chart: Tickets / Week Day
    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    const displayDays = [
      this.i18n.t('dashboard.days.mon'),
      this.i18n.t('dashboard.days.tue'),
      this.i18n.t('dashboard.days.wed'),
      this.i18n.t('dashboard.days.thu'),
      this.i18n.t('dashboard.days.fri'),
      this.i18n.t('dashboard.days.sat'),
      this.i18n.t('dashboard.days.sun'),
    ];
    const dailyCounts = days.map(d => this.dailyDataRes!.counts[d] || 0);

    this.barChartData = {
      labels: displayDays,
      datasets: [
        {
          label: this.i18n.t('dashboard.charts.decisions'),
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

    // 6. Sparklines (Mini-charts with gradients)
    const sparklines = kpiData?.sparklines || {};
    const sConf = sparklines['confiance'] || [0,0,0,0,0,0,0];
    const sConfm = sparklines['conformite'] || [0,0,0,0,0,0,0];
    const sRisk = sparklines['risque'] || [0,0,0,0,0,0,0];

    this.sparklineOptions = {
      maintainAspectRatio: false,
      plugins: { legend: { display: false }, tooltip: { enabled: false } },
      scales: {
        x: { display: false },
        y: { display: false, min: 0, max: 100 }
      },
      interaction: { mode: 'none' },
      layout: { padding: 0 }
    };

    const commonSparklineConfig = {
      fill: true,
      borderWidth: 2,
      tension: 0.4,
      pointRadius: 0
    };

    this.sparklineConfianceData = {
      labels: ['J-6','J-5','J-4','J-3','J-2','J-1', this.i18n.t('dashboard.sparkline.today')],
      datasets: [{
        ...commonSparklineConfig,
        data: sConf,
        borderColor: '#10b981', // green
        backgroundColor: (context: any) => {
          const ctx = context.chart.ctx;
          const gradient = ctx.createLinearGradient(0, 0, 0, 80);
          gradient.addColorStop(0, 'rgba(16, 185, 129, 0.4)');
          gradient.addColorStop(1, 'rgba(16, 185, 129, 0.0)');
          return gradient;
        }
      }]
    };

    this.sparklineConformiteData = {
      labels: ['J-6','J-5','J-4','J-3','J-2','J-1', this.i18n.t('dashboard.sparkline.today')],
      datasets: [{
        ...commonSparklineConfig,
        data: sConfm,
        borderColor: '#3b82f6', // blue
        backgroundColor: (context: any) => {
          const ctx = context.chart.ctx;
          const gradient = ctx.createLinearGradient(0, 0, 0, 80);
          gradient.addColorStop(0, 'rgba(59, 130, 246, 0.4)');
          gradient.addColorStop(1, 'rgba(59, 130, 246, 0.0)');
          return gradient;
        }
      }]
    };

    this.sparklineRisqueData = {
      labels: ['J-6','J-5','J-4','J-3','J-2','J-1', this.i18n.t('dashboard.sparkline.today')],
      datasets: [{
        ...commonSparklineConfig,
        data: sRisk,
        borderColor: '#ef4444', // rouge
        backgroundColor: (context: any) => {
          const chart = context.chart;
          const { ctx, chartArea } = chart;
          if (!chartArea) return null; // before render
          const gradient = ctx.createLinearGradient(0, chartArea.bottom, 0, chartArea.top);
          gradient.addColorStop(0, 'rgba(239, 68, 68, 0.4)'); // Stronger opacity at the bottom
          gradient.addColorStop(1, 'rgba(239, 68, 68, 0.05)');
          return gradient;
        }
      }]
    };

    // 7. Radar Chart for Responsable
    const radarData = kpiData?.domainMetrics?.['radarChart'];
    if (radarData) {
      this.radarChartData = {
        labels: [
          this.i18n.t('dashboard.charts.precision'),
          this.i18n.t('dashboard.charts.speed'),
          this.i18n.t('dashboard.charts.compliance'),
        ],
        datasets: [
          {
            label: this.i18n.t('dashboard.charts.aiPerformance'),
            backgroundColor: 'rgba(59, 130, 246, 0.2)',
            borderColor: '#3b82f6',
            pointBackgroundColor: '#3b82f6',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: '#3b82f6',
            data: [radarData['Precision'] || 0, radarData['Rapidite'] || 0, radarData['Conformite'] || 0]
          }
        ]
      };
      this.radarChartOptions = {
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { color: textColor } }
        },
        scales: {
          r: {
            grid: { color: surfaceBorder },
            pointLabels: { color: textColorSecondary },
            ticks: { color: textColorSecondary, backdropColor: 'transparent', min: 0, max: 100 }
          }
        }
      };
    }

    // 8. Gauge Chart for Agent
    const dailyRes = kpiData?.domainMetrics?.['dailyResolutionRate'];
    if (dailyRes !== undefined) {
      this.gaugeChartData = {
        labels: [this.i18n.t('dashboard.charts.resolved'), this.i18n.t('dashboard.charts.remaining')],
        datasets: [{
          data: [dailyRes, 100 - dailyRes],
          backgroundColor: ['#10b981', surfaceBorder],
          borderWidth: 0
        }]
      };
      this.gaugeChartOptions = {
        maintainAspectRatio: false,
        cutout: '80%',
        circumference: 180,
        rotation: -90,
        plugins: {
          legend: { display: false },
          tooltip: { enabled: false }
        }
      };
    }

    // 9. Anomalies Timeline for Auditeur
    const anomaliesList = kpiData?.domainMetrics?.['anomaliesTimeline'];
    if (anomaliesList && Array.isArray(anomaliesList)) {
      this.anomaliesChartData = {
        labels: anomaliesList.map((x: any) => x.label),
        datasets: [{
          label: this.i18n.t('dashboard.charts.highRiskAnomalies'),
          data: anomaliesList.map((x: any) => x.count),
          fill: true,
          borderColor: '#ef4444',
          backgroundColor: 'rgba(239, 68, 68, 0.2)',
          tension: 0.4
        }]
      };
      this.anomaliesChartOptions = {
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom', labels: { color: textColor } } },
        scales: {
          x: { ticks: { color: textColorSecondary }, grid: { display: false } },
          y: { ticks: { color: textColorSecondary }, grid: { color: surfaceBorder }, min: 0, suggestedMax: 10 }
        }
      };
    }
  }

  formatGeneratedAt(iso: string | null): string {
    if (!iso) return '';
    return new Date(iso).toLocaleDateString(this.i18n.localeId(), { day: '2-digit', month: 'short', year: 'numeric' });
  }

  private translateRiskLabel(label: string): string {
    const normalized = label.trim().toUpperCase();
    if (normalized === 'ÉLEVÉ' || normalized === 'ELEVE' || normalized === 'HIGH') {
      return this.i18n.t('risk.highShort');
    }
    if (normalized === 'MOYEN' || normalized === 'MODÉRÉ' || normalized === 'MODERE' || normalized === 'MEDIUM') {
      return this.i18n.t('risk.mediumShort');
    }
    if (normalized === 'FAIBLE' || normalized === 'LOW') {
      return this.i18n.t('risk.lowShort');
    }
    return label;
  }
}
