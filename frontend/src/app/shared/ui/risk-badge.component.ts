import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { riskChipClass } from '../../core/utils/chip-class.util';
import { riskLabel } from '../../core/utils/label.util';

@Component({
  selector: 'app-risk-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (riskLevel) {
      <span class="status-chip" [class]="chipClass">{{ label }}</span>
    } @else {
      <span class="status-chip chip--pending">—</span>
    }
  `,
})
export class RiskBadgeComponent {
  @Input() riskLevel?: string | null;

  get chipClass(): string {
    return riskChipClass(this.riskLevel);
  }

  get label(): string {
    return riskLabel(this.riskLevel);
  }
}
