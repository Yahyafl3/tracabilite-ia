import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import {
  AgentResponse,
  agentDisplayName,
  agentFallbackMessage,
} from '../../core/models/openrouter.models';
import { TranslationService } from '../../core/i18n/translation.service';
import { ConfidenceDisplayComponent } from './confidence-display.component';

@Component({
  selector: 'app-agent-response-card',
  standalone: true,
  imports: [ConfidenceDisplayComponent, TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <article class="agent-card">
      <header class="agent-card__head">
        <strong>{{ name }}</strong>
        <span class="status-chip chip--pending">{{ agent.statut }}</span>
      </header>
      @if (fallbackMsg) {
        <p class="state-banner state-banner--info">{{ fallbackMsg }}</p>
      }
      <p class="muted">{{ agent.agentKey }} · {{ agent.provider }}</p>
      <p class="muted">{{ 'shared.modelUsed' | translate }} <code>{{ agent.modelId }}</code></p>
      @if (agent.requestedModelId && agent.actualModelId && agent.requestedModelId !== agent.actualModelId) {
        <p class="muted tech-note">
          {{ 'shared.requested' | translate }} <code>{{ agent.requestedModelId }}</code>
          · {{ 'shared.actual' | translate }} <code>{{ agent.actualModelId }}</code>
        </p>
      }
      @if (agent.decisionProposee) {
        <p>{{ 'shared.decision' | translate }} <strong>{{ agent.decisionProposee }}</strong></p>
      }
      <p>
        {{ 'shared.declaredConfidence' | translate }}
        <strong>
          <app-confidence-display [confidence]="agent.declaredConfidence ?? agent.confianceDeclaree" />
        </strong>
      </p>
      @if (agent.codeErreur) {
        <p class="error-inline">{{ agent.codeErreur }}</p>
      }
      @if (agent.resume) {
        <p class="label-inline">{{ 'shared.summary' | translate }}</p>
        <p class="value value--text">{{ agent.resume }}</p>
      }
      @if (agent.explication) {
        <p class="label-inline">{{ 'shared.explanation' | translate }}</p>
        <p class="value value--text">{{ agent.explication }}</p>
      }
    </article>
  `,
})
export class AgentResponseCardComponent {
  private readonly i18n = inject(TranslationService);
  @Input({ required: true }) agent!: AgentResponse;

  get name(): string {
    this.i18n.currentLang();
    return agentDisplayName(this.agent);
  }

  get fallbackMsg(): string | null {
    this.i18n.currentLang();
    return agentFallbackMessage(this.agent);
  }
}
