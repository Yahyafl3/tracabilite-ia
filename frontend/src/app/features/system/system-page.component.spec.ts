import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { SystemPageComponent } from './system-page.component';
import { ActivatedRoute } from '@angular/router';

describe('SystemPageComponent', () => {
  async function create(data: Record<string, string>): Promise<ComponentFixture<SystemPageComponent>> {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [SystemPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data } },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(SystemPageComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('renders 403 access denied page', async () => {
    const fixture = await create({
      code: '403',
      title: 'Accès refusé',
      message: 'Permissions insuffisantes',
      severity: 'danger',
    });
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('403');
    expect(text).toContain('Accès refusé');
    expect(text).toContain('Tableau de bord');
  });

  it('renders 404 not found page', async () => {
    const fixture = await create({
      code: '404',
      title: 'Page introuvable',
      message: 'Introuvable',
      severity: 'warn',
    });
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('404');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Page introuvable');
  });

  it('navigates back via Location on Retour', async () => {
    const fixture = await create({
      code: '500',
      title: 'Erreur serveur',
      message: 'Erreur',
      severity: 'danger',
    });
    const goBackSpy = vi.spyOn(fixture.componentInstance, 'goBack');
    fixture.componentInstance.goBack();
    expect(goBackSpy).toHaveBeenCalled();
  });
});

describe('System routes', () => {
  it('maps unauthorized to 403 and unknown paths to 404', async () => {
    const { routes } = await import('../../app.routes');
    const unauthorized = routes.find((r) => r.path === 'unauthorized');
    const wildcard = routes.find((r) => r.path === '**');
    const forbidden = routes.find((r) => r.path === '403');
    const notFound = routes.find((r) => r.path === '404');

    expect(forbidden).toBeTruthy();
    expect(notFound).toBeTruthy();
    expect(unauthorized?.redirectTo).toBe('403');
    expect(wildcard?.redirectTo).toBe('404');
  });
});
