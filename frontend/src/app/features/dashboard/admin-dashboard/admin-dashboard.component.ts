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
    console.log('[AdminDashboard] Component initialized');
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
        console.log('[AdminDashboard] Received dashboard data from API:', data);
        console.log('[AdminDashboard] Setting stats signal...');
        this.stats.set(data);
        console.log('[AdminDashboard] Stats signal set. Current value:', this.stats());
        this.loading.set(false);
        console.log('[AdminDashboard] Loading set to false. Current value:', this.loading());
        
        // Trigger chart data transformations to log their output
        console.log('[AdminDashboard] Calling getDomainChartData()...');
        this.getDomainChartData();
        console.log('[AdminDashboard] Calling getStatusChartData()...');
        this.getStatusChartData();
        console.log('[AdminDashboard] Calling getTimelineChartData()...');
        this.getTimelineChartData();
      },
      error: (err) => {
        console.error('[AdminDashboard] Error loading dashboard data:', err);
        this.error.set(err.message || 'Erreur lors du chargement des statistiques');
        this.loading.set(false);
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
    console.log('[AdminDashboard] Domain distribution data:', domain);
    if (!domain) {
      console.log('[AdminDashboard] No domain distribution data available');
      return [];
    }

    const chartData = [
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
    console.log('[AdminDashboard] Transformed domain chart data:', chartData);
    return chartData;
  }

  /**
   * Transform status distribution data for donut chart
   * Returns data points with labels, values, and colors for decision statuses
   */
  getStatusChartData(): DonutChartDataPoint[] {
    const status = this.stats()?.statusDistribution;
    console.log('[AdminDashboard] Status distribution data:', status);
    if (!status) {
      console.log('[AdminDashboard] No status distribution data available');
      return [];
    }

    const chartData = [
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
    console.log('[AdminDashboard] Transformed status chart data:', chartData);
    return chartData;
  }

  /**
   * Transform timeline data for line chart
   * Returns data points with dates and decision counts for the last 7 days
   */
  getTimelineChartData(): LineChartDataPoint[] {
    const timeline = this.stats()?.timelineData;
    console.log('[AdminDashboard] Timeline data:', timeline);
    if (!timeline || timeline.length === 0) {
      console.log('[AdminDashboard] No timeline data available');
      return [];
    }

    const chartData = timeline.map(point => ({
      date: point.date,
      count: point.decisionCount || 0
    }));
    console.log('[AdminDashboard] Transformed timeline chart data:', chartData);
    return chartData;
  }
}
