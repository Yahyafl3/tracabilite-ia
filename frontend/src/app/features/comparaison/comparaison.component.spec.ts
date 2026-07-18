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
