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

  it('renders fullscreen background video with muted autoplay loop', () => {
    const video = (fixture.nativeElement as HTMLElement).querySelector(
      'video.login-background-video',
    ) as HTMLVideoElement;
    expect(video).toBeTruthy();
    expect(video.hasAttribute('autoplay')).toBe(true);
    expect(video.muted || video.hasAttribute('muted')).toBe(true);
    expect(video.loop || video.hasAttribute('loop')).toBe(true);
    expect(video.hasAttribute('playsinline')).toBe(true);
    expect(video.getAttribute('aria-hidden')).toBe('true');

    const sources = Array.from(video.querySelectorAll('source')).map((s) => s.getAttribute('src'));
    expect(sources).toContain('/assets/videos/ai-traceability-bg.mp4');
    expect(sources).toContain('/assets/videos/ai-traceability-bg.webm');

    expect((fixture.nativeElement as HTMLElement).querySelector('.login-video-overlay')).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).querySelector('form.login-form')).toBeTruthy();
  });

  it('hides video without crashing when playback fails', () => {
    expect(fixture.componentInstance.videoFailed()).toBe(false);
    fixture.componentInstance.onVideoError();
    fixture.detectChanges();

    expect(fixture.componentInstance.videoFailed()).toBe(true);
    const video = (fixture.nativeElement as HTMLElement).querySelector(
      'video.login-background-video',
    ) as HTMLVideoElement;
    expect(video.classList.contains('video-hidden')).toBe(true);
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
