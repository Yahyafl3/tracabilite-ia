import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';

export interface DonutChartDataPoint {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-donut-chart',
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
        <p-chart type="doughnut" [data]="chartData" [options]="chartOptions"></p-chart>
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
export class DonutChartComponent implements OnChanges {
  @Input({ required: true }) title!: string;
  @Input({ required: true }) data!: DonutChartDataPoint[];

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
    // Check if data is empty or all values are zero
    this.isEmpty = !this.data || 
                   this.data.length === 0 || 
                   this.data.every(d => d.value === 0);

    if (!this.isEmpty) {
      // Build Chart.js data structure
      this.chartData = {
        labels: this.data.map(d => d.label),
        datasets: [{
          data: this.data.map(d => d.value),
          backgroundColor: this.data.map(d => d.color),
          borderWidth: 2,
          borderColor: 'var(--surface-card)'
        }]
      };

      // Configure chart options with legend and percentage tooltips
      this.chartOptions = {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              color: 'var(--text-color)',
              font: {
                family: 'var(--font-family)',
                size: 12
              },
              padding: 15,
              usePointStyle: true
            }
          },
          tooltip: {
            backgroundColor: 'var(--surface-overlay)',
            titleColor: 'var(--text-color)',
            bodyColor: 'var(--text-color)',
            borderColor: 'var(--surface-border)',
            borderWidth: 1,
            callbacks: {
              label: (context: any) => {
                const label = context.label || '';
                const value = context.parsed || 0;
                const dataset = context.dataset.data;
                const total = dataset.reduce((a: number, b: number) => a + b, 0);
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
                return `${label}: ${value} (${percentage}%)`;
              }
            }
          }
        },
        cutout: '60%'
      };
    }
  }
}
