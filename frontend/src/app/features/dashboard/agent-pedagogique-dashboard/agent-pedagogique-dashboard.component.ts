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
 * AgentPedagogiqueDashboardComponent - AGENT_PEDAGOGIQUE role dashboard
 * 
 * Displays personal activity statistics in EDUCATION domain including:
 * - My decisions count (own + ADMINISTRATEUR decisions in EDUCATION)
 * - Pending validations (own EN_ATTENTE_VALIDATION decisions)
 * - Validated decisions (own VALIDEE decisions)
 * - Acceptance rate (validated / total)
 * 
 * Backend applies agent scoping: own decisions + ADMINISTRATEUR decisions in EDUCATION domain.
 * Uses Angular signals for reactive state management.
 * 
 * Requirements: 5.1-5.11, 9.1-9.10, 15.4, 15.8, 18.1-18.10, 19.1-19.8
 */
@Component({
  selector: 'app-agent-pedagogique-dashboard',
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
  templateUrl: './agent-pedagogique-dashboard.component.html',
  styleUrls: ['./agent-pedagogique-dashboard.component.scss']
})
export class AgentPedagogiqueDashboardComponent implements OnInit {
  /**
   * Loading state signal - true while fetching data from API
   * Requirement 9.4: Implement loading signal
   */
  loading = signal(true);

  /**
   * Error state signal - contains error message if fetch fails, null otherwise
   * Requirement 9.4: Implement error signal
   */
  error = signal<string | null>(null);

  /**
   * Dashboard statistics signal - contains all dashboard data from API
   * Requirement 9.4: Implement stats signal
   */
  stats = signal<DashboardStatsDTO | null>(null);

  constructor(
    private dashboardService: DashboardService,
    public router: Router
  ) {}

  /**
   * Component initialization - fetch dashboard data on load
   * Requirement 9.3: Implement ngOnInit() calling loadDashboardData()
   */
  ngOnInit(): void {
    this.loadDashboardData();
  }

  /**
   * Fetch dashboard statistics from backend API
   * Sets loading state, clears previous errors, and updates stats signal on success
   * Backend applies agent scoping automatically based on authenticated user
   * Requirement 9.3: Implement loadDashboardData()
   */
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

  /**
   * Retry fetching dashboard data after an error
   * Requirement 9.4: Implement retry() method
   */
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
   * Returns data points with dates and decision counts for the last 7 days
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
