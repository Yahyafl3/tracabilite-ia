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
 * ValidateurDashboardComponent - VALIDATEUR role dashboard
 * 
 * Displays validation queue and personal validation activity in CREDIT domain only.
 * VALIDATEUR is a legacy CREDIT-only validator role.
 * 
 * KPIs displayed:
 * - Pending validations (CREDIT EN_ATTENTE_VALIDATION)
 * - Validated by me (CREDIT decisions validated by user)
 * - Rejected by me (CREDIT decisions rejected by user)
 * - Total processed (validated + rejected)
 * 
 * Charts:
 * - Status distribution donut chart (CREDIT domain)
 * - 7-day validation activity timeline
 * 
 * Tables:
 * - Pending validations (all CREDIT EN_ATTENTE_VALIDATION)
 * - Recent validation actions (10 most recent by user)
 * 
 * Backend automatically applies CREDIT domain scoping for VALIDATEUR role.
 * NO "Nouvelle Décision" button since validators cannot create decisions.
 * 
 * Uses Angular signals for reactive state management.
 * 
 * Requirements: 6.1-6.9, 9.1-9.10, 15.3, 18.1-18.10, 19.1-19.8
 */
@Component({
  selector: 'app-validateur-dashboard',
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
  templateUrl: './validateur-dashboard.component.html',
  styleUrls: ['./validateur-dashboard.component.scss']
})
export class ValidateurDashboardComponent implements OnInit {
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
   * Backend automatically scopes to CREDIT domain for VALIDATEUR role
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
   * Backend automatically applies CREDIT domain scoping for VALIDATEUR role
   * Requirement 9.3, 6.1-6.9: Implement loadDashboardData() with CREDIT scoping
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
   * Requirement 9.4, 13.5: Implement retry() method
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
   * Returns data points with dates and validation counts for the last 7 days
   */
  getTimelineChartData(): LineChartDataPoint[] {
    const timeline = this.stats()?.timelineData;
    if (!timeline || timeline.length === 0) return [];

    return timeline.map(point => ({
      date: point.date,
      count: point.validationCount || 0
    }));
  }

  /**
   * NO navigateToNewDecision() method
   * VALIDATEUR cannot create decisions (validator role only)
   * Requirement 15.3: Validators SHALL NOT display "Nouvelle Décision" button
   */
}
