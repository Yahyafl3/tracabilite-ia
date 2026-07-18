import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-model-identity',
  standalone: true,
  imports: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <dl class="model-identity" [attr.aria-label]="ariaLabel">
      <div class="model-identity__item">
        <dt class="model-identity__label">Modèle</dt>
        <dd class="model-identity__value">{{ modelName || '—' }}</dd>
      </div>
      <div class="model-identity__item">
        <dt class="model-identity__label">Version</dt>
        <dd class="model-identity__value">{{ modelVersion || '—' }}</dd>
      </div>
      @if (analyzedAt) {
        <div class="model-identity__item">
          <dt class="model-identity__label">Date d'analyse</dt>
          <dd class="model-identity__value">{{ analyzedAt | date:'short' }}</dd>
        </div>
      }
    </dl>
  `,
})
export class ModelIdentityComponent {
  @Input() modelName?: string | null;
  @Input() modelVersion?: string | null;
  @Input() analyzedAt?: string | Date | null;
  @Input() ariaLabel = 'Identité du modèle ML';
}
