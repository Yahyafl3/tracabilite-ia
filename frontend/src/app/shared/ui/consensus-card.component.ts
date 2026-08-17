import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import {
  ConsensusResponse,
  formatConsensusDisplay,
} from '../../core/models/openrouter.models';
import { decisionChipClass } from '../../core/utils/chip-class.util';
import { decisionLabel } from '../../core/utils/label.util';
import { TranslationService } from '../../core/i18n/translation.service';

@Component({
  selector: 'app-consensus-card',
  standalone: true,
  imports: [TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (consensus) {
      <article class="consensus-card">
        @let view = display;
        <div class="consensus-card__head">
          @if (view.showDecisionBadge && view.decisionLabel) {
            <span class="status-chip" [class]="decisionChipClass(view.decisionLabel)">
              {{ labelOf(view.decisionLabel) }}
            </span>
          } @else {
            <p class="consensus-card__message">{{ view.message }}</p>
          }
          <span class="consensus-card__agents">{{ view.agentsLabel }}</span>
        </div>
        @if (consensus.agreementRate != null) {
          <p class="consensus-card__meta">{{ 'shared.agreementRate' | translate }} {{ consensus.agreementRate }} %</p>
        }
        @if (consensus.note) {
          <p class="consensus-card__note">{{ consensus.note }}</p>
        }
        @if (consensus.resume) {
          <p class="consensus-card__resume">{{ consensus.resume }}</p>
        }
      </article>
    }
  `,
})
export class ConsensusCardComponent {
  private readonly i18n = inject(TranslationService);
  @Input({ required: true }) consensus!: ConsensusResponse;

  decisionChipClass = decisionChipClass;

  get display() {
    this.i18n.currentLang();
    return formatConsensusDisplay(this.consensus);
  }

  labelOf(decision: string): string {
    this.i18n.currentLang();
    return decisionLabel(decision);
  }
}
