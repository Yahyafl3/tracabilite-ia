import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let auth: { login: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    sessionStorage.clear();

    auth = {
      login: vi.fn(() => of({ token: 'token', user: { role: 'UTILISATEUR' } })),
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([{ path: 'decisions', children: [] }]),
        { provide: AuthService, useValue: auth },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  });

  it('renders the split-screen login layout with form', () => {
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.login-page')).toBeTruthy();
    expect(el.querySelector('.login-split')).toBeTruthy();
    expect(el.querySelector('.login-side')).toBeTruthy();
    expect(el.querySelector('.login-main')).toBeTruthy();
    expect(el.querySelector('form.login-form')).toBeTruthy();
    expect(el.querySelector('video.login-background-video')).toBeNull();
  });

  it('starts with an empty form and no admin credentials', () => {
    const value = fixture.componentInstance.loginForm.getRawValue();
    expect(value.email).toBe('');
    expect(value.password).toBe('');
    expect(value.email).not.toContain('admin@tracabilite.ia');
    expect(value.password).not.toBe('admin123');

    const el = fixture.nativeElement as HTMLElement;
    const emailInput = el.querySelector('#login-email') as HTMLInputElement;
    expect(emailInput.value).toBe('');
    expect(el.textContent).not.toContain('admin@tracabilite.ia');
    expect(el.querySelector('#rememberMe')).toBeNull();
  });

  it('keeps form empty after a logical re-init (refresh simulation)', () => {
    fixture.componentInstance.loginForm.setValue({
      email: 'admin@tracabilite.ia',
      password: 'admin123',
    });
    fixture.destroy();

    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    const value = fixture.componentInstance.loginForm.getRawValue();
    expect(value.email).toBe('');
    expect(value.password).toBe('');
  });

  it('never stores password in localStorage', () => {
    fixture.componentInstance.loginForm.setValue({
      email: 'user@test.fr',
      password: 'secret1',
    });
    fixture.componentInstance.onSubmit();

    expect(localStorage.getItem('password')).toBeNull();
    expect(JSON.stringify(localStorage)).not.toContain('secret1');
  });

  it('maps 401 to a professional incorrect-credentials message', () => {
    auth.login.mockReturnValue(
      throwError(() => new Error(AuthService.resolveLoginErrorMessage({ status: 401 }))),
    );

    fixture.componentInstance.loginForm.setValue({
      email: 'user@test.fr',
      password: 'wrongpass',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage()).toBe(
      'Adresse email ou mot de passe incorrect.',
    );
  });

  it('maps 403 to authorization message', () => {
    auth.login.mockReturnValue(
      throwError(() => new Error(AuthService.resolveLoginErrorMessage({ status: 403 }))),
    );

    fixture.componentInstance.loginForm.setValue({
      email: 'user@test.fr',
      password: 'secret1',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage()).toBe(
      'Votre compte est désactivé ou vous ne disposez pas des autorisations nécessaires.',
    );
  });

  it('maps 500 to unavailable service message', () => {
    auth.login.mockReturnValue(
      throwError(() => new Error(AuthService.resolveLoginErrorMessage({ status: 500 }))),
    );

    fixture.componentInstance.loginForm.setValue({
      email: 'user@test.fr',
      password: 'secret1',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage()).toBe(
      'Le service est temporairement indisponible. Veuillez réessayer plus tard.',
    );
  });

  it('keeps login form usable when videoFailed is set', () => {
    expect(fixture.componentInstance.videoFailed()).toBe(false);
    fixture.componentInstance.onVideoError();
    fixture.detectChanges();

    expect(fixture.componentInstance.videoFailed()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).querySelector('form.login-form')).toBeTruthy();
  });

  it('disables submit and shows loading while authenticating', () => {
    const pending = new Subject<unknown>();
    auth.login.mockReturnValue(pending.asObservable());

    fixture.componentInstance.loginForm.setValue({
      email: 'user@test.fr',
      password: 'secret1',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.isLoading()).toBe(true);
    const submit = (fixture.nativeElement as HTMLElement).querySelector(
      'button[type="submit"]',
    ) as HTMLButtonElement;
    expect(submit.disabled).toBe(true);

    pending.next({});
    pending.complete();
    fixture.detectChanges();
    expect(fixture.componentInstance.isLoading()).toBe(false);
  });

  it('redirects after successful login without changing JWT submit flow', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.componentInstance.loginForm.setValue({
      email: 'user@test.fr',
      password: 'secret1',
    });
    fixture.componentInstance.onSubmit();

    expect(auth.login).toHaveBeenCalledWith(
      expect.objectContaining({
        email: 'user@test.fr',
        password: 'secret1',
      }),
    );
    // Default post-login route (unchanged JWT / redirect logic).
    expect(navigateSpy).toHaveBeenCalledWith(['/decisions']);
  });

  it('keeps Traçabilité IA branding without OpenRouter marketing labels', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Traçabilité IA');
    expect(text).toContain('Décisions assistées et auditables');
    expect(text).not.toContain('Consensus OpenRouter');
    expect(text).not.toContain('Réponses OpenRouter');
  });

  it('does not show Google, Microsoft or social divider', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).not.toContain('Google');
    expect(text).not.toContain('Microsoft');
    expect(text).not.toContain('Ou continuer avec');
    expect((fixture.nativeElement as HTMLElement).querySelector('.login-social')).toBeNull();
    expect((fixture.nativeElement as HTMLElement).querySelector('.login-divider')).toBeNull();
  });

  it('keeps forgot-password and support links with a usable email/password form', () => {
    const el = fixture.nativeElement as HTMLElement;
    const text = el.textContent ?? '';

    expect(text).toContain('Mot de passe oublié');
    expect(text).toContain('Contactez le support');
    expect(el.querySelector('.forgot-link')).toBeTruthy();
    expect(el.querySelector('a[href="/support"]')).toBeTruthy();
    expect(el.querySelector('#login-email')).toBeTruthy();
    expect(el.querySelector('p-password')).toBeTruthy();
    expect(el.querySelector('button[type="submit"]')).toBeTruthy();
  });
});

describe('AuthService.resolveLoginErrorMessage', () => {
  it('maps HTTP statuses to professional French messages', () => {
    expect(AuthService.resolveLoginErrorMessage({ status: 400 })).toBe(
      'Adresse email ou mot de passe incorrect.',
    );
    expect(AuthService.resolveLoginErrorMessage({ status: 401 })).toBe(
      'Adresse email ou mot de passe incorrect.',
    );
    expect(AuthService.resolveLoginErrorMessage({ status: 403 })).toBe(
      'Votre compte est désactivé ou vous ne disposez pas des autorisations nécessaires.',
    );
    expect(AuthService.resolveLoginErrorMessage({ status: 500 })).toBe(
      'Le service est temporairement indisponible. Veuillez réessayer plus tard.',
    );
    expect(AuthService.resolveLoginErrorMessage({ status: 0 })).toBe(
      'Le service est temporairement indisponible. Veuillez réessayer plus tard.',
    );
  });
});
