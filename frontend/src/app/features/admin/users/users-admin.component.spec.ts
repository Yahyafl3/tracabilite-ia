import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ConfirmationService } from 'primeng/api';
import { UsersAdminComponent } from './users-admin.component';
import { UserAdminService } from '../../../core/services/user-admin.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/models/auth.models';

describe('UsersAdminComponent', () => {
  let fixture: ComponentFixture<UsersAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsersAdminComponent],
      providers: [
        ConfirmationService,
        {
          provide: UserAdminService,
          useValue: {
            list: () =>
              of([
                {
                  id: '1',
                  nom: 'Admin',
                  email: 'admin@test.fr',
                  role: UserRole.ADMINISTRATEUR,
                  dateCreation: '2026-07-01T10:00:00.000Z',
                },
                {
                  id: '2',
                  nom: 'Validateur',
                  email: 'validateur@test.fr',
                  role: UserRole.VALIDATEUR,
                  dateCreation: '2026-07-02T10:00:00.000Z',
                },
                {
                  id: '3',
                  nom: 'Operateur',
                  email: 'ops@test.fr',
                  role: UserRole.UTILISATEUR,
                  dateCreation: '2026-07-03T10:00:00.000Z',
                },
              ]),
            create: () => of({}),
            update: () => of({}),
            delete: () => of(void 0),
          },
        },
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

  it('filters users by role', () => {
    expect(fixture.componentInstance.filteredUsers()).toHaveLength(3);

    fixture.componentInstance.roleFilter.set(UserRole.VALIDATEUR);
    fixture.detectChanges();

    const filtered = fixture.componentInstance.filteredUsers();
    expect(filtered).toHaveLength(1);
    expect(filtered[0].email).toBe('validateur@test.fr');
  });

  it('asks confirmation before sensitive delete', () => {
    const componentConfirmation = fixture.debugElement.injector.get(ConfirmationService);
    const confirmSpy = vi.spyOn(componentConfirmation, 'confirm');
    const user = fixture.componentInstance.users().find((u) => u.id === '2')!;

    fixture.componentInstance.confirmDelete(user);

    expect(confirmSpy).toHaveBeenCalled();
    expect(confirmSpy.mock.calls[0][0].message).toContain('validateur@test.fr');
  });

  it('does not mention Groq admin panel', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).not.toMatch(/Groq/i);
    expect(text).not.toContain('Consensus OpenRouter');
  });
});
