import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConfirmationService } from 'primeng/api';
import { UsersAdminComponent } from './users-admin.component';
import { UserAdminService } from '../../../core/services/user-admin.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/models/auth.models';

describe('UsersAdminComponent', () => {
  let fixture: ComponentFixture<UsersAdminComponent>;
  let userAdmin: {
    list: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    deactivate: ReturnType<typeof vi.fn>;
    reactivate: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    userAdmin = {
      list: vi.fn(() =>
        of([
          {
            id: '1',
            nom: 'Admin',
            email: 'admin@test.fr',
            role: UserRole.ADMINISTRATEUR,
            actif: true,
            dateCreation: '2026-07-01T10:00:00.000Z',
          },
          {
            id: '2',
            nom: 'Validateur',
            email: 'validateur@test.fr',
            role: UserRole.VALIDATEUR,
            actif: true,
            dateCreation: '2026-07-02T10:00:00.000Z',
          },
          {
            id: '3',
            nom: 'Operateur',
            email: 'ops@test.fr',
            role: UserRole.UTILISATEUR,
            actif: true,
            dateCreation: '2026-07-03T10:00:00.000Z',
          },
        ]),
      ),
      create: vi.fn(() => of({})),
      update: vi.fn(() => of({})),
      deactivate: vi.fn(() => of({ id: '2', actif: false })),
      reactivate: vi.fn(() => of({ id: '2', actif: true })),
    };

    await TestBed.configureTestingModule({
      imports: [UsersAdminComponent],
      providers: [
        ConfirmationService,
        { provide: UserAdminService, useValue: userAdmin },
        {
          provide: AuthService,
          useValue: {
            currentUser: {
              id: '1',
              nom: 'Admin',
              email: 'admin@test.fr',
              role: UserRole.ADMINISTRATEUR,
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UsersAdminComponent);
    fixture.detectChanges();
  });

  it('exposes Utilisateur in role options with backend enum value', () => {
    const roles = fixture.componentInstance.roles;
    expect(roles).toContain(UserRole.UTILISATEUR);
    expect(roles).toEqual([
      UserRole.UTILISATEUR,
      UserRole.VALIDATEUR,
      UserRole.AUDITEUR,
      UserRole.ADMINISTRATEUR,
    ]);
    expect(fixture.componentInstance.roleOptions.map((o) => o.label)).toEqual([
      'Utilisateur',
      'Validateur',
      'Auditeur',
      'Administrateur',
    ]);
  });

  it('filters users by role including Utilisateur', () => {
    expect(fixture.componentInstance.filteredUsers()).toHaveLength(3);

    fixture.componentInstance.roleFilter.set(UserRole.UTILISATEUR);
    fixture.detectChanges();

    const filtered = fixture.componentInstance.filteredUsers();
    expect(filtered).toHaveLength(1);
    expect(filtered[0].email).toBe('ops@test.fr');
  });

  it('asks confirmation before deactivation and blocks self-deactivation', () => {
    const componentConfirmation = fixture.debugElement.injector.get(ConfirmationService);
    const confirmSpy = vi.spyOn(componentConfirmation, 'confirm');
    const user = fixture.componentInstance.users().find((u) => u.id === '2')!;

    fixture.componentInstance.confirmDeactivate(user);

    expect(confirmSpy).toHaveBeenCalled();
    expect(confirmSpy.mock.calls[0][0].message).toContain('décisions et son historique');
    expect(confirmSpy.mock.calls[0][0].acceptLabel).toBe('Désactiver');

    fixture.componentInstance.confirmDeactivate(fixture.componentInstance.users()[0]);
    expect(fixture.componentInstance.error()).toContain('propre compte');
  });

  it('does not mention Groq admin panel', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).not.toMatch(/Groq/i);
    expect(text).not.toContain('Consensus OpenRouter');
  });
});
