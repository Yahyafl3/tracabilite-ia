import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AgentResponseCardComponent } from './agent-response-card.component';
import type { AgentResponse } from '../../core/models/openrouter.models';

describe('AgentResponseCardComponent', () => {
  let fixture: ComponentFixture<AgentResponseCardComponent>;

  const agentWithFallback: AgentResponse = {
    agentKey: 'AGENT_1',
    modelId: 'google/gemma-4-26b-a4b-it:free',
    actualModelId: 'google/gemma-4-26b-a4b-it:free',
    provider: 'GOOGLE_OPENROUTER',
    statut: 'SUCCESS',
    fallbackUsed: true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgentResponseCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AgentResponseCardComponent);
    fixture.componentRef.setInput('agent', agentWithFallback);
    fixture.detectChanges();
  });

  it('shows fallback message when fallbackUsed is true', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const banner = compiled.querySelector('.state-banner')?.textContent ?? '';

    expect(banner).toContain('Modèle principal indisponible');
  });

  it('shows technical error code for MODEL_UNAVAILABLE agent', () => {
    const unavailableAgent: AgentResponse = {
      agentKey: 'AGENT_2',
      modelId: 'anthropic/claude-3-haiku',
      provider: 'OpenRouter',
      statut: 'MODEL_UNAVAILABLE',
      codeErreur: 'MODEL_UNAVAILABLE',
    };
    fixture.componentRef.setInput('agent', unavailableAgent);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('MODEL_UNAVAILABLE');
  });
});
