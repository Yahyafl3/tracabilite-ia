import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { IconComponent } from '../icon.component';

export type KpiAccent = 'indigo' | 'green' | 'amber' | 'violet' | 'danger' | 'info';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="ui-kpi-card ui-kpi-card--{{ accent }}">
      <div class="ui-kpi-card__icon">
        <app-icon [name]="icon" [size]="20" />
      </div>
      <div class="ui-kpi-card__body">
        <p class="ui-kpi-card__label">{{ label }}</p>
        <p class="ui-kpi-card__value">{{ value }}{{ unit }}</p>
        @if (hint) {
          <p class="ui-kpi-card__hint">{{ hint }}</p>
        }
        @if (trend) {
          <p class="ui-kpi-card__trend">{{ trend }}</p>
        }
      </div>
    </div>
  `,
})
export class KpiCardComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) value!: string | number;
  @Input() unit = '';
  @Input({ required: true }) icon!: string;
  @Input() accent: KpiAccent = 'indigo';
  @Input() hint?: string;
  @Input() trend?: string;
}
