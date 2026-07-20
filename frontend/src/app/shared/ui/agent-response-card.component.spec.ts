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
      modelId: 'openai/gpt-oss-20b',
      provider: 'GROQ',
      statut: 'MODEL_UNAVAILABLE',
      codeErreur: 'MODEL_UNAVAILABLE',
    };
    fixture.componentRef.setInput('agent', unavailableAgent);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('MODEL_UNAVAILABLE');
  });

  it('displays GROQ provider for new agent responses', () => {
    const groqAgent: AgentResponse = {
      agentKey: 'AGENT_1',
      modelId: 'llama-3.3-70b-versatile',
      displayName: 'Llama 3.3 70B Versatile',
      provider: 'GROQ',
      statut: 'SUCCESS',
      decisionProposee: 'APPROUVER',
      declaredConfidence: 0.77,
    };
    fixture.componentRef.setInput('agent', groqAgent);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('GROQ');
    expect(text).toContain('llama-3.3-70b-versatile');
  });

  it('formats declared confidence 0.8 as 80 % via ConfidenceDisplay', () => {
    const groqAgent: AgentResponse = {
      agentKey: 'AGENT_1',
      modelId: 'llama-3.3-70b-versatile',
      displayName: 'Llama 3.3 70B Versatile',
      provider: 'GROQ',
      statut: 'SUCCESS',
      declaredConfidence: 0.8,
    };
    fixture.componentRef.setInput('agent', groqAgent);
    fixture.detectChanges();

    const text = ((fixture.nativeElement as HTMLElement).textContent ?? '').replace(/\u00a0/g, ' ');
    expect(text).toMatch(/80\s*%/);
    expect(text).not.toMatch(/0\.8\s*%/);
  });

  it('shows Non fournie when declared confidence is null', () => {
    const agent: AgentResponse = {
      agentKey: 'AGENT_1',
      modelId: 'llama-3.3-70b-versatile',
      provider: 'GROQ',
      statut: 'SUCCESS',
      declaredConfidence: null,
    };
    fixture.componentRef.setInput('agent', agent);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Non fournie');
  });

  it('still displays historical OpenRouter provider unchanged', () => {
    const historical: AgentResponse = {
      agentKey: 'AGENT_1',
      modelId: 'meta-llama/llama-3.3-70b-instruct:free',
      provider: 'META_OPENROUTER',
      statut: 'SUCCESS',
    };
    fixture.componentRef.setInput('agent', historical);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('META_OPENROUTER');
  });
});
