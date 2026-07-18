import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ShellComponent } from './shell.component';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../shared/theme.service';
import { UserRole } from '../../core/models/auth.models';

describe('ShellComponent', () => {
  let fixture: ComponentFixture<ShellComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShellComponent],
      providers: [
        provideRouter([
          { path: 'dashboard', component: ShellComponent },
          { path: 'decisions', component: ShellComponent },
        ]),
        {
          provide: AuthService,
          useValue: {
            currentUser: {
              id: '1',
              nom: 'Admin Test',
              email: 'admin@test.fr',
              role: UserRole.ADMINISTRATEUR,
            },
            logout: vi.fn(),
          },
        },
        {
          provide: ThemeService,
          useValue: {
            theme: () => 'light',
            toggle: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(ShellComponent);
    fixture.detectChanges();
  });

  it('builds admin navigation including audit and users', () => {
    const items = fixture.componentInstance.navItems();
    const labels = items.map((item) => item.label);

    expect(labels).toContain('Tableau de bord');
    expect(labels).toContain('Audit');
    expect(labels).toContain('Utilisateurs');
  });

  it('marks active route with aria-current in sidebar', async () => {
    await router.navigateByUrl('/dashboard');
    fixture.detectChanges();

    const activeLink = fixture.nativeElement.querySelector('.nav-link.active') as HTMLElement;
    expect(activeLink).toBeTruthy();
    expect(activeLink.getAttribute('aria-current')).toBe('page');
  });

  it('toggles mobile sidebar visibility', () => {
    expect(fixture.componentInstance.mobileSidebarOpen()).toBe(false);
    fixture.componentInstance.toggleMobileSidebar();
    expect(fixture.componentInstance.mobileSidebarOpen()).toBe(true);
    fixture.componentInstance.closeMobileSidebar();
    expect(fixture.componentInstance.mobileSidebarOpen()).toBe(false);
  });

  it('exposes integrity chip with accessible status label', () => {
    const chip = fixture.nativeElement.querySelector('.integrity-chip') as HTMLElement;
    expect(chip.getAttribute('aria-label')).toContain('SHA-256');
  });
});
