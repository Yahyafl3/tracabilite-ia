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
 * AuditeurDashboardComponent - AUDITEUR role dashboard
 * 
 * Displays global read-only audit statistics across ALL domains (CREDIT, MEDICAL, EDUCATION).
 * AUDITEUR has no create/edit/validate actions - strictly read-only access.
 * 
 * KPIs displayed:
 * - Total decisions (all domains)
 * - Validated decisions (all domains, VALIDEE status)
 * - Rejected decisions (all domains, REJETEE status)
 * - Compliance rate (validated / total across all domains)
 * 
 * Charts:
 * - Domain distribution donut chart (CREDIT, MEDICAL, EDUCATION)
 * - Status distribution donut chart (all domains)
 * - 30-day timeline evolution (all domains)
 * 
 * Tables:
 * - All decisions (read-only access)
 * - Audit trail entries (validation actions by validators)
 * - Validation activity by validator
 * 
 * Backend automatically applies all-domain scoping for AUDITEUR role.
 * Absolutely NO "Nouvelle Décision" button (auditor is read-only).
 * 
 * Uses Angular signals for reactive state management.
 * 
 * Requirements: 8.1-8.10, 9.1-9.10, 15.4, 18.1-18.10, 19.1-19.8
 */
@Component({
  selector: 'app-auditeur-dashboard',
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
  templateUrl: './auditeur-dashboard.component.html',
  styleUrls: ['./auditeur-dashboard.component.scss']
})
export class AuditeurDashboardComponent implements OnInit {
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

  /**
   * NO navigateToNewDecision() method
   * AUDITEUR cannot create decisions (read-only audit role)
   * Requirement 15.4: Auditors SHALL NOT display "Nouvelle Décision" button
   */
}
