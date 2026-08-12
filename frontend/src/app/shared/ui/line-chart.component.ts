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
    }

    ::ng-deep .p-chart {
      display: flex;
      justify-content: center;
      padding: 1rem;
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
                   this.data.every(d => d.count === 0);

    if (!this.isEmpty) {
      // Build Chart.js line data structure
      this.chartData = {
        labels: this.data.map(d => this.formatDate(d.date)),
        datasets: [{
          label: 'Décisions',
          data: this.data.map(d => d.count),
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

      // Configure chart options with Y-axis starting at zero
      this.chartOptions = {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'var(--surface-overlay)',
            titleColor: 'var(--text-color)',
            bodyColor: 'var(--text-color)',
            borderColor: 'var(--surface-border)',
            borderWidth: 1,
            callbacks: {
              title: (context: any) => {
                return context[0].label;
              },
              label: (context: any) => {
                return `Décisions: ${context.parsed.y}`;
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1,
              color: 'var(--text-color-secondary)',
              font: {
                family: 'var(--font-family)',
                size: 11
              }
            },
            grid: {
              color: 'var(--surface-border)',
              drawBorder: false
            }
          },
          x: {
            ticks: {
              color: 'var(--text-color-secondary)',
              font: {
                family: 'var(--font-family)',
                size: 11
              }
            },
            grid: {
              color: 'var(--surface-border)',
              drawBorder: false
            }
          }
        }
      };
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
