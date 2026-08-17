import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import type { DecisionResponse } from '../../../../core/models/decision.models';
import { TranslationService } from '../../../../core/i18n/translation.service';

@Component({
  selector: 'app-credit-decision-details',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './credit-decision-details.component.html',
  styleUrl: './credit-decision-details.component.scss',
})
export class CreditDecisionDetailsComponent {
  private readonly i18n = inject(TranslationService);
  @Input({ required: true }) decision!: DecisionResponse;

  get data() {
    return this.decision.creditData ?? {};
  }

  mad(value: number | undefined | null): string {
    if (value == null) return this.i18n.t('common.dash');
    return `${Number(value).toLocaleString(this.i18n.localeId())} MAD`;
  }

  pct(value: number | undefined | null): string {
    if (value == null) return this.i18n.t('common.dash');
    return `${(Number(value) * 100).toLocaleString(this.i18n.localeId(), { maximumFractionDigits: 1 })} %`;
  }

  text(value: unknown): string {
    if (value == null || value === '') return this.i18n.t('common.dash');
    return String(value);
  }
}
