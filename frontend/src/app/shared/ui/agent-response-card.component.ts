import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import {
  AgentResponse,
  agentDisplayName,
  agentFallbackMessage,
} from '../../core/models/openrouter.models';
import { ConfidenceDisplayComponent } from './confidence-display.component';

@Component({
  selector: 'app-agent-response-card',
  standalone: true,
  imports: [ConfidenceDisplayComponent],
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
      <p class="muted">Modèle utilisé : <code>{{ agent.modelId }}</code></p>
      @if (agent.requestedModelId && agent.actualModelId && agent.requestedModelId !== agent.actualModelId) {
        <p class="muted tech-note">
          Demandé : <code>{{ agent.requestedModelId }}</code> · Réel : <code>{{ agent.actualModelId }}</code>
        </p>
      }
      @if (agent.decisionProposee) {
        <p>Décision : <strong>{{ agent.decisionProposee }}</strong></p>
      }
      <p>
        Confiance déclarée par l'agent :
        <strong>
          <app-confidence-display [confidence]="agent.declaredConfidence ?? agent.confianceDeclaree" />
        </strong>
      </p>
      @if (agent.codeErreur) {
        <p class="error-inline">{{ agent.codeErreur }}</p>
      }
      @if (agent.resume) {
        <p class="label-inline">Résumé</p>
        <p class="value value--text">{{ agent.resume }}</p>
      }
      @if (agent.explication) {
        <p class="label-inline">Explication</p>
        <p class="value value--text">{{ agent.explication }}</p>
      }
    </article>
  `,
})
export class AgentResponseCardComponent {
  @Input({ required: true }) agent!: AgentResponse;

  get name(): string {
    return agentDisplayName(this.agent);
  }

  get fallbackMsg(): string | null {
    return agentFallbackMessage(this.agent);
  }
}
