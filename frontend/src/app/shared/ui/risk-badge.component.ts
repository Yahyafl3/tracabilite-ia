import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { riskChipClass } from '../../core/utils/chip-class.util';
import { riskLabel } from '../../core/utils/label.util';
import { TranslationService } from '../../core/i18n/translation.service';

@Component({
  selector: 'app-risk-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (riskLevel) {
      <span class="status-chip" [class]="chipClass">{{ label }}</span>
    } @else {
      <span class="status-chip chip--pending">{{ dash }}</span>
    }
  `,
})
export class RiskBadgeComponent {
  private readonly i18n = inject(TranslationService);
  @Input() riskLevel?: string | null;

  get chipClass(): string {
    return riskChipClass(this.riskLevel);
  }

  get label(): string {
    this.i18n.currentLang();
    return riskLabel(this.riskLevel);
  }

  get dash(): string {
    this.i18n.currentLang();
    return this.i18n.t('common.dash');
  }
}
