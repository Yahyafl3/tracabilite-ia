import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../core/i18n/translation.service';

@Component({
  selector: 'app-model-identity',
  standalone: true,
  imports: [TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <dl class="model-identity" [attr.aria-label]="resolvedAria">
      <div class="model-identity__item">
        <dt class="model-identity__label">{{ 'common.model' | translate }}</dt>
        <dd class="model-identity__value">{{ modelName || ('common.dash' | translate) }}</dd>
      </div>
      <div class="model-identity__item">
        <dt class="model-identity__label">{{ 'common.version' | translate }}</dt>
        <dd class="model-identity__value">{{ modelVersion || ('common.dash' | translate) }}</dd>
      </div>
      @if (analyzedAt) {
        <div class="model-identity__item">
          <dt class="model-identity__label">{{ 'shared.analysisDate' | translate }}</dt>
          <dd class="model-identity__value">{{ formattedAnalyzedAt }}</dd>
        </div>
      }
    </dl>
  `,
})
export class ModelIdentityComponent {
  private readonly i18n = inject(TranslationService);
  @Input() modelName?: string | null;
  @Input() modelVersion?: string | null;
  @Input() analyzedAt?: string | Date | null;
  @Input() ariaLabel?: string;

  get locale(): string {
    return this.i18n.localeId();
  }

  get formattedAnalyzedAt(): string {
    if (!this.analyzedAt) return '';
    return new Date(this.analyzedAt).toLocaleString(this.i18n.localeId(), {
      dateStyle: 'short',
      timeStyle: 'short',
    });
  }

  get resolvedAria(): string {
    this.i18n.currentLang();
    return this.ariaLabel ?? this.i18n.t('shared.modelIdentity');
  }
}
