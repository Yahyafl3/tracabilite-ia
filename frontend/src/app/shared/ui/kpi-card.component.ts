import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { SkeletonModule } from 'primeng/skeleton';

export type KpiAccent = 'indigo' | 'green' | 'amber' | 'violet' | 'danger' | 'info';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, CardModule, SkeletonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p-card styleClass="kpi-card">
      @if (loading) {
        <p-skeleton height="4rem"></p-skeleton>
      } @else {
        <div class="kpi-content">
          <i [class]="icon" class="kpi-icon"></i>
          <div class="kpi-body">
            <span class="kpi-label">{{ label }}</span>
            <strong class="kpi-value">{{ formattedValue }}</strong>
            @if (trend !== undefined && trend !== null) {
              <span class="kpi-trend" [class.positive]="trend > 0" [class.negative]="trend < 0">
                <i [class]="trendIcon"></i> {{ Math.abs(trend) }}%
              </span>
            }
          </div>
        </div>
      }
    </p-card>
  `,
  styles: [`
    :host {
      display: block;
    }

    .kpi-card {
      height: 100%;
    }

    .kpi-content {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .kpi-icon {
      font-size: 2.5rem;
      color: var(--primary-color);
    }

    .kpi-body {
      display: flex;
      flex-direction: column;
      flex: 1;
    }

    .kpi-label {
      font-size: 0.875rem;
      color: var(--text-color-secondary);
      margin-bottom: 0.25rem;
    }

    .kpi-value {
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--text-color);
      line-height: 1.2;
    }

    .kpi-trend {
      font-size: 0.875rem;
      margin-top: 0.25rem;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
    }

    .kpi-trend.positive {
      color: var(--green-600);
    }

    .kpi-trend.negative {
      color: var(--red-600);
    }

    .kpi-trend i {
      font-size: 0.75rem;
    }
  `]
})
export class KpiCardComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) value!: string | number;
  @Input() unit = '';
  @Input() icon: string = 'pi pi-chart-bar';
  @Input() accent: KpiAccent = 'indigo';
  @Input() hint?: string;
  @Input() trend?: number;
  @Input() loading: boolean = false;

  protected readonly Math = Math;

  get formattedValue(): string {
    // Handle string values as-is
    if (typeof this.value === 'string') {
      return this.value + this.unit;
    }

    // Handle numeric values with formatting
    if (this.unit === '%') {
      return `${this.value.toFixed(1)}%`;
    }

    // Format with thousand separators for French locale
    return this.value.toLocaleString('fr-FR') + this.unit;
  }

  get trendIcon(): string {
    if (this.trend === undefined || this.trend === null) {
      return '';
    }
    return this.trend > 0 ? 'pi pi-arrow-up' : 'pi pi-arrow-down';
  }
}
