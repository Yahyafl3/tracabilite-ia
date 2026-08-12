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
 * AdminDashboardComponent - ADMINISTRATEUR role dashboard
 * 
 * Displays global multi-domain statistics including:
 * - Total decisions across all domains
 * - Pending validations (EN_ATTENTE_VALIDATION)
 * - Decisions created today
 * - Active users count
 * 
 * Uses DashboardService.getDashboardStats() which applies server-side data isolation.
 * Uses Angular signals for reactive state management.
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 15.1, 15.5
 */
@Component({
  selector: 'app-admin-dashboard',
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
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
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
   * Navigate to decision creation page
   * For ADMINISTRATEUR, no domain restriction applies
   * Requirement 9.4, 15.1, 15.5: Implement navigateToNewDecision() method (no domain restriction)
   */
  navigateToNewDecision(): void {
    this.router.navigate(['/decisions/new']);
  }

  /**
   * Transform domain distribution data for donut chart
   * Returns data points with labels, values, and colors for CREDIT, MEDICAL, EDUCATION
   */
  getDomainChartData(): DonutChartDataPoint[] {
    const domain = this.stats()?.domainDistribution;
    if (!domain) return [];

    return [
      {
        label: 'Crédit',
        value: domain.creditCount || 0,
        color: '#3b82f6' // Blue
      },
      {
        label: 'Santé',
        value: domain.medicalCount || 0,
        color: '#ef4444' // Red
      },
      {
        label: 'Éducation',
        value: domain.educationCount || 0,
        color: '#10b981' // Green
      }
    ];
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
