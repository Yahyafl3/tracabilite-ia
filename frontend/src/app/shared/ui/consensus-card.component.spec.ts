import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsensusCardComponent } from './consensus-card.component';
import type { ConsensusResponse } from '../../core/models/openrouter.models';

describe('ConsensusCardComponent', () => {
  let fixture: ComponentFixture<ConsensusCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsensusCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsensusCardComponent);
  });

  it('shows INSUFFICIENT_RESPONSES message', () => {
    const consensus: ConsensusResponse = {
      agentsConsultes: 3,
      agentsReussis: 0,
      successfulAgentCount: 0,
      consensusAvailable: false,
      decisionConsensus: 'INSUFFICIENT_RESPONSES',
    };
    fixture.componentRef.setInput('consensus', consensus);
    fixture.detectChanges();

    const message = fixture.nativeElement.querySelector('.consensus-card__message')?.textContent ?? '';
    expect(message).toContain('Consensus indisponible');
    expect(fixture.nativeElement.querySelector('.status-chip')).toBeNull();
  });

  it('shows NO_CONSENSUS message without REVIEW fallback badge', () => {
    const consensus: ConsensusResponse = {
      agentsConsultes: 3,
      agentsReussis: 2,
      successfulAgentCount: 2,
      consensusAvailable: false,
      decisionConsensus: 'REVIEW',
    };
    fixture.componentRef.setInput('consensus', consensus);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent ?? '';
    expect(text).toContain('Pas de consensus');
    expect(text).not.toContain('REVIEW');
  });

  it('shows consensus decision badge when available', () => {
    const consensus: ConsensusResponse = {
      agentsConsultes: 3,
      agentsReussis: 3,
      successfulAgentCount: 3,
      consensusAvailable: true,
      decisionConsensus: 'APPROUVER',
      agreementRate: 100,
    };
    fixture.componentRef.setInput('consensus', consensus);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.status-chip')?.textContent).toContain('Approuver');
  });
});
