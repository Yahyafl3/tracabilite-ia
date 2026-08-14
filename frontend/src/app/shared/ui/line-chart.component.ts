import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';

export interface LineChartDataPoint {
  date: string;
  count: number;
}

@Component({
  selector: 'app-line-chart',
  standalone: true,
  imports: [CommonModule, CardModule, ChartModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-card [header]="title">
      @if (isEmpty) {
        <div class="empty-state">
          <i class="pi pi-inbox empty-icon"></i>
          <p class="empty-message">Aucune donnée disponible</p>
        </div>
      } @else {
        <p-chart type="line" [data]="chartData" [options]="chartOptions"></p-chart>
      }
    </p-card>
  `,
  styles: [`
    :host {
      display: block;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem 1rem;
      text-align: center;
    }

    .empty-icon {
      font-size: 3rem;
      color: var(--text-color-secondary);
      margin-bottom: 1rem;
      opacity: 0.5;
    }

    .empty-message {
      font-size: 0.875rem;
      color: var(--text-color-secondary);
      margin: 0;
    }

    ::ng-deep .p-card .p-card-body {
      padding: 1rem;
      min-height: 350px; /* Ensure minimum height for chart visibility */
    }

    ::ng-deep .p-chart {
      display: flex;
      justify-content: center;
      padding: 1rem;
      min-height: 300px; /* Ensure chart has minimum height */
    }

    ::ng-deep .p-chart canvas {
      max-height: 400px !important; /* Limit maximum height */
    }
  `]
})
export class LineChartComponent implements OnChanges {
  @Input({ required: true }) title!: string;
  @Input({ required: true }) data!: LineChartDataPoint[];

  chartData: any;
  chartOptions: any;
  isEmpty = false;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.updateChart();
      // Force change detection with OnPush strategy
      this.cdr.markForCheck();
    }
  }

  private updateChart(): void {
    // Check if data is empty or all counts are zero
    this.isEmpty = !this.data || 
                   this.data.length === 0 || 
                   this.data.every(d => d?.count === 0);

    if (!this.isEmpty) {
      // Build Chart.js line data structure with null-safe operations
      this.chartData = {
        labels: this.data.filter(d => d && d.date).map(d => this.formatDate(d.date)),
        datasets: [{
          label: 'Décisions',
          data: this.data.filter(d => d && d.count != null).map(d => d.count),
          fill: false,
          borderColor: '#3b82f6',
          backgroundColor: '#3b82f6',
          tension: 0.4,
          pointRadius: 4,
          pointHoverRadius: 6,
          pointBackgroundColor: '#3b82f6',
          pointBorderColor: '#fff',
          pointBorderWidth: 2
        }]
      };

      // Configure chart options with Y-axis starting at zero and CSS variable fallbacks
      this.chartOptions = {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: this.getCssVariable('--surface-overlay', '#ffffff'),
            titleColor: this.getCssVariable('--text-color', '#000000'),
            bodyColor: this.getCssVariable('--text-color', '#000000'),
            borderColor: this.getCssVariable('--surface-border', '#dee2e6'),
            borderWidth: 1,
            callbacks: {
              title: (context: any) => {
                return context[0]?.label || '';
              },
              label: (context: any) => {
                return `Décisions: ${context.parsed?.y || 0}`;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1,
              color: this.getCssVariable('--text-color-secondary', '#6c757d'),
              font: {
                family: this.getCssVariable('--font-family', 'system-ui'),
                size: 11
              }
            },
            grid: {
              color: this.getCssVariable('--surface-border', '#dee2e6'),
              drawBorder: false
            }
          },
          x: {
            ticks: {
              color: this.getCssVariable('--text-color-secondary', '#6c757d'),
              font: {
                family: this.getCssVariable('--font-family', 'system-ui'),
                size: 11
              }
            },
            grid: {
              color: this.getCssVariable('--surface-border', '#dee2e6'),
              drawBorder: false
            }
          }
        }
      };
    }
  }

  /**
   * Safely get CSS variable value with fallback
   */
  private getCssVariable(varName: string, fallback: string): string {
    try {
      const value = getComputedStyle(document.documentElement).getPropertyValue(varName).trim();
      return value || fallback;
    } catch {
      return fallback;
    }
  }

  /**
   * Format date string for French locale display
   * Converts ISO date string to format like "15 janv."
   */
  private formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', { 
      month: 'short', 
      day: 'numeric' 
    });
  }
}
