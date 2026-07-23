import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { SupportComponent } from './support.component';
import { SupportService } from '../../core/services/support.service';

describe('SupportComponent', () => {
  let fixture: ComponentFixture<SupportComponent>;
  let support: { submitMessage: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    support = {
      submitMessage: vi.fn(() =>
        of({ message: 'Votre demande a été envoyée. Notre équipe vous répondra dès que possible.' }),
      ),
    };

    await TestBed.configureTestingModule({
      imports: [SupportComponent],
      providers: [provideRouter([]), { provide: SupportService, useValue: support }],
    }).compileComponents();

    fixture = TestBed.createComponent(SupportComponent);
    fixture.detectChanges();
  });

  it('renders public support form with navigation links', () => {
    const el = fixture.nativeElement as HTMLElement;
    const text = el.textContent ?? '';
    expect(text).toContain('Contactez le support');
    expect(text).toContain('Revenir à la connexion');
    expect(text).toContain('Revenir à l’accueil');
    expect(el.querySelector('#support-name')).toBeTruthy();
    expect(el.querySelector('#support-email')).toBeTruthy();
    expect(el.querySelector('#support-subject')).toBeTruthy();
    expect(el.querySelector('#support-message')).toBeTruthy();
  });

  it('validates required fields before submit', () => {
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();
    expect(support.submitMessage).not.toHaveBeenCalled();
    expect(fixture.componentInstance.form.invalid).toBe(true);
  });

  it('shows loading while submitting', () => {
    const pending = new Subject<unknown>();
    support.submitMessage.mockReturnValue(pending.asObservable());

    fixture.componentInstance.form.setValue({
      name: 'Jane Doe',
      email: 'jane@example.com',
      subject: 'Problème de connexion',
      message: 'Je n’arrive pas à me connecter depuis ce matin.',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.isLoading()).toBe(true);
    const submit = (fixture.nativeElement as HTMLElement).querySelector(
      'button[type="submit"]',
    ) as HTMLButtonElement;
    expect(submit.disabled).toBe(true);

    pending.next({ message: 'ok' });
    pending.complete();
    fixture.detectChanges();
    expect(fixture.componentInstance.isLoading()).toBe(false);
  });

  it('shows success message after submit', () => {
    fixture.componentInstance.form.setValue({
      name: 'Jane Doe',
      email: 'jane@example.com',
      subject: 'Problème de connexion',
      message: 'Je n’arrive pas à me connecter depuis ce matin.',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.successMessage()).toContain('Votre demande a été envoyée');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Votre demande a été envoyée',
    );
  });

  it('shows generic error on failure', () => {
    support.submitMessage.mockReturnValue(throwError(() => ({ message: 'Erreur réseau' })));
    fixture.componentInstance.form.setValue({
      name: 'Jane Doe',
      email: 'jane@example.com',
      subject: 'Problème de connexion',
      message: 'Je n’arrive pas à me connecter depuis ce matin.',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(fixture.componentInstance.errorMessage()).toBeTruthy();
  });
});
