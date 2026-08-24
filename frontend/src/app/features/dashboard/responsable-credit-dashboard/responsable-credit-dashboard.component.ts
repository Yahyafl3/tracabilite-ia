import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CardModule } from 'primeng/card';
import { SkeletonModule } from 'primeng/skeleton';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { DashboardService, DashboardStatsDTO } from '../../../core/services/dashboard.service';
import { KpiCardComponent } from '../../../shared/ui/kpi-card.component';
import { DonutChartComponent, DonutChartDataPoint } from '../../../shared/ui/donut-chart.component';
import { LineChartComponent, LineChartDataPoint } from '../../../shared/ui/line-chart.component';

/**
 * ResponsableCreditDashboardComponent - RESPONSABLE_CREDIT role dashboard
 * 
 * Displays domain supervision statistics for CREDIT domain including:
 * - Total decisions (CREDIT domain)
 * - Pending validations (CREDIT EN_ATTENTE_VALIDATION)
 * - Validated this month (CREDIT, current month)
 * - Validation rate (validated / total in domain)
 * 
 * Charts:
 * - Status distribution donut chart (CREDIT domain)
 * - 30-day timeline evolution
 * 
 * Tables:
 * - Pending validations (CREDIT EN_ATTENTE_VALIDATION)
 * - Top creators (CREDIT domain)
 * 
 * Backend automatically applies CREDIT domain scoping for RESPONSABLE_CREDIT role.
 * NO "Nouvelle Décision" button since managers cannot create decisions.
 * 
 * Uses Angular signals for reactive state management.
 * 
 * Requirements: 7.1-7.11, 9.1-9.10, 15.3, 18.1-18.10, 19.1-19.8
 */
@Component({
  selector: 'app-responsable-credit-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    SkeletonModule,
    ButtonModule,
    TableModule,
    TagModule,
    KpiCardComponent,
    DonutChartComponent,
    LineChartComponent
  ],
  templateUrl: './responsable-credit-dashboard.component.html',
  styleUrls: ['./responsable-credit-dashboard.component.scss']
})
export class ResponsableCreditDashboardComponent implements OnInit {
  loading = signal(true);
  error = signal<string | null>(null);
  stats = signal<DashboardStatsDTO | null>(null);

  constructor(
    private dashboardService: DashboardService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading.set(true);
    this.error.set(null);

    this.dashboardService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Erreur lors du chargement des statistiques');
        this.loading.set(false);
        console.error('Dashboard error:', err);
      }
    });
  }

  retry(): void {
    this.loadDashboardData();
  }

  /**
   * Transform status distribution data for donut chart
   * Returns data points with labels, values, and colors for decision statuses
   */
  getStatusChartData(): DonutChartDataPoint[] {
    const status = this.stats()?.statusDistribution;
    if (!status) return [];

    return [
      {
        label: 'Validée',
        value: status.validee || 0,
        color: '#10b981' // Green
      },
      {
        label: 'En attente',
        value: status.enAttenteValidation || 0,
        color: '#f59e0b' // Orange
      },
      {
        label: 'Rejetée',
        value: status.rejetee || 0,
        color: '#ef4444' // Red
      }
    ];
  }

  /**
   * Transform timeline data for line chart
   * Returns data points with dates and decision counts for 30 days
   */
  getTimelineChartData(): LineChartDataPoint[] {
    const timeline = this.stats()?.timelineData;
    if (!timeline || timeline.length === 0) return [];

    return timeline.map(point => ({
      date: point.date,
      count: point.decisionCount || 0
    }));
  }
}
