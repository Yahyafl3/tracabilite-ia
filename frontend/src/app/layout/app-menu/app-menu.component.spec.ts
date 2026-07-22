import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppMenuComponent } from './app-menu.component';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';

describe('AppMenuComponent', () => {
  async function create(role: UserRole): Promise<ComponentFixture<AppMenuComponent>> {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [AppMenuComponent],
      providers: [
        provideRouter([]),
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

    expect(labels).toContain('Dashboard');
    expect(labels).toContain('Audit');
    expect(labels).toContain('Utilisateurs');
    expect(labels).toContain('Agents Groq');
    expect(labels).toContain('Support');
    expect(labels).not.toContain('Consensus OpenRouter');
  });

  it('hides admin entries for non-admin users', async () => {
    const fixture = await create(UserRole.VALIDATEUR);
    const labels = fixture.componentInstance
      .model()
      .flatMap((group) => group.items ?? [])
      .map((item) => item.label);

    expect(labels).toContain('Validation humaine');
    expect(labels).not.toContain('Utilisateurs');
    expect(labels).not.toContain('Agents Groq');
    expect(labels).not.toContain('Support');
  });
});
