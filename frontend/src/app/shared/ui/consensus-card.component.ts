import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import {
  ConsensusResponse,
  formatConsensusDisplay,
} from '../../core/models/openrouter.models';
import { decisionChipClass } from '../../core/utils/chip-class.util';
import { decisionLabel } from '../../core/utils/label.util';

@Component({
  selector: 'app-consensus-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (consensus) {
      <article class="consensus-card">
        @let view = display;
        <div class="consensus-card__head">
          @if (view.showDecisionBadge && view.decisionLabel) {
            <span class="status-chip" [class]="decisionChipClass(view.decisionLabel)">
              {{ decisionLabel(view.decisionLabel) }}
            </span>
          } @else {
            <p class="consensus-card__message">{{ view.message }}</p>
          }
          <span class="consensus-card__agents">{{ view.agentsLabel }}</span>
        </div>
        @if (consensus.agreementRate != null) {
          <p class="consensus-card__meta">Taux d'accord : {{ consensus.agreementRate }} %</p>
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
  @Input({ required: true }) consensus!: ConsensusResponse;

  decisionChipClass = decisionChipClass;
  decisionLabel = decisionLabel;

  get display() {
    return formatConsensusDisplay(this.consensus);
  }
}
