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
    auth = {
      login: vi.fn(() => of({ accessToken: 'token' })),
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
      email: 'admin@test.fr',
      password: 'secret1',
      rememberMe: false,
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

  it('shows an error message when login fails', () => {
    auth.login.mockReturnValue(throwError(() => ({ message: 'Identifiants invalides' })));

    fixture.componentInstance.loginForm.setValue({
      email: 'admin@test.fr',
      password: 'secret1',
      rememberMe: false,
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage()).toBe('Identifiants invalides');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Identifiants invalides');
  });

  it('redirects to returnUrl after successful login', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.componentInstance.returnUrl = '/dashboard';
    fixture.componentInstance.loginForm.setValue({
      email: 'admin@test.fr',
      password: 'secret1',
      rememberMe: true,
    });
    fixture.componentInstance.onSubmit();

    expect(auth.login).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  });

  it('keeps Traçabilité IA branding without OpenRouter marketing labels', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Traçabilité IA');
    expect(text).toContain('Décisions assistées et auditables');
    expect(text).not.toContain('Consensus OpenRouter');
    expect(text).not.toContain('Réponses OpenRouter');
  });
});
