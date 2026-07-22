import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { LandingComponent } from './landing.component';

describe('LandingComponent', () => {
  let fixture: ComponentFixture<LandingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LandingComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(LandingComponent);
    fixture.detectChanges();
  });

  function text(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('affiche le titre principal', () => {
    expect(text()).toContain('Des décisions IA');
    expect(text()).toContain('traçables, explicables et responsables');
  });

  it('affiche les six fonctionnalités principales', () => {
    expect(text()).toContain('Prédiction Machine Learning');
    expect(text()).toContain('Explication SHAP');
    expect(text()).toContain('Agents IA Groq');
    expect(text()).toContain('Consensus multi-agents');
    expect(text()).toContain('Validation humaine');
    expect(text()).toContain('Audit et intégrité');
  });

  it('affiche les cinq étapes du fonctionnement', () => {
    expect(text()).toContain('Saisie du dossier');
    expect(text()).toContain('Analyse ML et SHAP');
    expect(text()).toContain('Consultation multi-agents');
    expect(text()).toContain('Consensus et traçabilité');
    expect(text()).toContain('Validation humaine');
  });

  it('n’affiche aucun contenu demo trompeur', () => {
    const body = text();
    expect(body).not.toContain('Tarifs');
    expect(body).not.toContain('Carrières');
    expect(body).not.toContain('Blog');
    expect(body).not.toContain('Paris, France');
    expect(body).not.toContain('contact@tracabilite-ia.com');
    expect(body).not.toContain('+33 1 84 80 00 00');
    expect(body).not.toContain('Statut de service');
    expect(body).not.toContain('Demander une démo');
    expect(body).not.toContain('ISO 27001');
    expect(body).not.toContain('AI Act ready');
  });

  it('affiche le CTA et les liens de connexion', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(text()).toContain('Découvrez une approche responsable');
    const loginLinks = el.querySelectorAll('a[href="/auth/login"]');
    expect(loginLinks.length).toBeGreaterThanOrEqual(2);
  });

  it('affiche un footer réel de projet académique', () => {
    expect(text()).toContain('Projet académique');
    expect(text()).toContain('Angular, Spring Boot, PostgreSQL');
  });

  it('expose les ancres de navigation demandées', () => {
    const el = fixture.nativeElement as HTMLElement;
    const ids = [
      'accueil',
      'fonctionnalites',
      'fonctionnement',
      'technologies',
      'securite',
      'cas-usage',
      'validation-humaine',
      'cta',
    ];
    for (const id of ids) {
      expect(el.querySelector(`#${id}`)).not.toBeNull();
    }
  });
});
