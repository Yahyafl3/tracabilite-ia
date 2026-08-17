import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../core/i18n/translation.service';

export type ConfidenceScale = 'ratio' | 'percent';

@Component({
  selector: 'app-confidence-display',
  standalone: true,
  imports: [TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @switch (displayState) {
      @case ('value') {
        <span class="ui-confidence">{{ formattedPercent }}</span>
      }
      @case ('empty') {
        <span class="ui-confidence ui-confidence--empty">{{ 'agents.confidenceMissing' | translate }}</span>
      }
      @case ('invalid') {
        <span class="ui-confidence ui-confidence--invalid">{{ 'agents.confidenceInvalid' | translate }}</span>
      }
    }
  `,
})
export class ConfidenceDisplayComponent {
  private readonly i18n = inject(TranslationService);
  /**
   * - ratio: valeur backend dans [0, 1] (confiance déclarée agent) → affichée × 100
   * - percent: valeur déjà en pourcent (confiance ML typiquement 0–100)
   */
  @Input() scale: ConfidenceScale = 'ratio';
  @Input() confidence: number | null | undefined;

  get displayState(): 'value' | 'empty' | 'invalid' {
    this.i18n.currentLang();
    if (this.confidence == null || Number.isNaN(this.confidence)) {
      return 'empty';
    }
    if (this.scale === 'ratio') {
      if (this.confidence < 0 || this.confidence > 1) {
        console.warn('[ConfidenceDisplay] Valeur hors [0,1]:', this.confidence);
        return 'invalid';
      }
      return 'value';
    }
    if (this.confidence < 0 || this.confidence > 100) {
      console.warn('[ConfidenceDisplay] Pourcentage hors [0,100]:', this.confidence);
      return 'invalid';
    }
    return 'value';
  }

  get formattedPercent(): string {
    const value = this.confidence ?? 0;
    const percent = this.scale === 'ratio' ? value * 100 : value;
    const formatted = percent.toLocaleString(this.i18n.localeId(), {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    });
    return `${formatted} %`;
  }
}
