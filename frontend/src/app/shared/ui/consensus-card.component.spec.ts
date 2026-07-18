import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsensusCardComponent } from './consensus-card.component';
import type { ConsensusResponse } from '../../core/models/openrouter.models';

describe('ConsensusCardComponent', () => {
  let fixture: ComponentFixture<ConsensusCardComponent>;

  const insufficientConsensus: ConsensusResponse = {
    agentsConsultes: 3,
    agentsReussis: 0,
    successfulAgentCount: 0,
    consensusAvailable: false,
    decisionConsensus: 'INSUFFICIENT_RESPONSES',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsensusCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsensusCardComponent);
    fixture.componentRef.setInput('consensus', insufficientConsensus);
    fixture.detectChanges();
  });

  it('shows INSUFFICIENT_RESPONSES message', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const message = compiled.querySelector('.consensus-card__message')?.textContent ?? '';

    expect(message).toContain('Consensus indisponible');
    expect(message).toContain('nombre insuffisant');
    expect(compiled.querySelector('.status-chip')).toBeNull();
  });
});
