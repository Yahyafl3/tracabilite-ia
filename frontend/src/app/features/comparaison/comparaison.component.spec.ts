import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ComparaisonComponent } from './comparaison.component';
import { ComparaisonService } from '../../core/services/comparaison.service';

describe('ComparaisonComponent', () => {
  let fixture: ComponentFixture<ComparaisonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComparaisonComponent],
      providers: [
        {
          provide: ComparaisonService,
          useValue: {
            getOpenRouterAgents: () =>
              of([
                {
                  rang: 1,
                  nom: 'GPT Agent',
                  fournisseur: 'OpenRouter',
                  modele: 'openai/gpt-4o-mini',
                  versionModele: 'latest',
                  totalDecisions: 10,
                  approuvees: 6,
                  modifiees: 1,
                  rejetees: 1,
                  enAttente: 2,
                  scorePourcentage: 80,
                },
                {
                  rang: 2,
                  nom: 'Groq Llama',
                  fournisseur: 'GROQ',
                  modele: 'llama-3.1-70b',
                  versionModele: 'latest',
                  totalDecisions: 4,
                  approuvees: 3,
                  modifiees: 0,
                  rejetees: 0,
                  enAttente: 1,
                  scorePourcentage: 75,
                },
              ]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ComparaisonComponent);
    fixture.detectChanges();
  });

  it('labels success metric as Taux de succès API instead of ambiguous score', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Taux de succès API');
    expect(text).not.toMatch(/\bScore\b/i);
  });

  it('keeps historical OpenRouter and Groq agent data without recalculation', () => {
    const agents = fixture.componentInstance.agentsData();
    expect(agents.some((a) => a.fournisseur === 'OpenRouter')).toBe(true);
    expect(agents.some((a) => a.fournisseur === 'GROQ')).toBe(true);
    expect(agents.find((a) => a.fournisseur === 'OpenRouter')?.scorePourcentage).toBe(80);
    expect(agents.find((a) => a.fournisseur === 'GROQ')?.scorePourcentage).toBe(75);

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('OpenRouter');
    expect(text).toContain('GROQ');
    expect(text).not.toContain('Consensus OpenRouter');
  });

  it('filters by provider without changing API values', () => {
    fixture.componentInstance.providerFilter.set('GROQ');
    fixture.detectChanges();
    const filtered = fixture.componentInstance.agents();
    expect(filtered).toHaveLength(1);
    expect(filtered[0].scorePourcentage).toBe(75);
  });

  it('sorts agents by API success rate descending by default', () => {
    const agents = fixture.componentInstance.agents();
    expect(agents[0].scorePourcentage).toBe(80);
    expect(fixture.componentInstance.sortField()).toBe('scorePourcentage');
  });

  it('shows error state when comparison API fails', async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ComparaisonComponent],
      providers: [
        {
          provide: ComparaisonService,
          useValue: {
            getOpenRouterAgents: () => throwError(() => ({ status: 500 })),
          },
        },
      ],
    }).compileComponents();

    const errorFixture = TestBed.createComponent(ComparaisonComponent);
    errorFixture.detectChanges();
    expect(errorFixture.componentInstance.error()).toContain('Impossible');
  });
});
