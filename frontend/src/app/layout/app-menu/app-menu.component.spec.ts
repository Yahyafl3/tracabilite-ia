import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppMenuComponent } from './app-menu.component';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { provideI18nTesting } from '../../core/i18n/provide-i18n';

describe('AppMenuComponent', () => {
  async function create(role: UserRole): Promise<ComponentFixture<AppMenuComponent>> {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [AppMenuComponent],
      providers: [
        provideRouter([]),
        ...provideI18nTesting(),
        {
          provide: AuthService,
          useValue: {
            currentUser: {
              id: '1',
              nom: 'User',
              email: 'user@test.fr',
              role,
            },
          },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(AppMenuComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('builds admin navigation including audit, users and Groq', async () => {
    const fixture = await create(UserRole.ADMINISTRATEUR);
    const labels = fixture.componentInstance
      .model()
      .flatMap((group) => group.items ?? [])
      .map((item) => item.label);

    expect(labels).toContain('nav.dashboard');
    expect(labels).toContain('nav.myDecisions');
    expect(labels).toContain('nav.audit');
    expect(labels).toContain('nav.users');
    expect(labels).toContain('nav.groqAgents');
    expect(labels).toContain('nav.support');
    expect(labels).toContain('nav.backupRestore');
    expect(labels).toContain('nav.aiComparison');
    expect(labels).not.toContain('Consensus OpenRouter');
  });

  it('hides admin entries for non-admin users', async () => {
    const fixture = await create(UserRole.RESPONSABLE_CREDIT);
    const labels = fixture.componentInstance
      .model()
      .flatMap((group) => group.items ?? [])
      .map((item) => item.label);

    expect(labels).toContain('nav.validationQueue');
    expect(labels).not.toContain('nav.users');
    expect(labels).not.toContain('nav.groqAgents');
    expect(labels).not.toContain('nav.support');
    expect(labels).not.toContain('nav.backupRestore');
  });

  it('shows only Agent de crédit menus for AGENT_CREDIT role', async () => {
    const fixture = await create(UserRole.AGENT_CREDIT);
    const labels = fixture.componentInstance
      .model()
      .flatMap((group) => group.items ?? [])
      .map((item) => item.label);

    expect(labels).toEqual(['nav.dashboard', 'nav.myDecisions', 'nav.newDecision']);
    expect(labels).not.toContain('nav.users');
    expect(labels).not.toContain('nav.groqAgents');
    expect(labels).not.toContain('nav.support');
    expect(labels).not.toContain('nav.backupRestore');
    expect(labels).not.toContain('nav.validationQueue');
    expect(labels).not.toContain('nav.aiComparison');
    expect(labels).not.toContain('nav.audit');
  });
});
