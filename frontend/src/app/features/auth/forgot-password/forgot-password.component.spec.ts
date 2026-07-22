import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ForgotPasswordComponent } from './forgot-password.component';
import { AuthService } from '../../../core/services/auth.service';

describe('ForgotPasswordComponent', () => {
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let auth: { requestPasswordReset: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    auth = {
      requestPasswordReset: vi.fn(() =>
        of({
          message:
            'Si un compte correspond à cette adresse, un lien de réinitialisation a été envoyé.',
        }),
      ),
    };

    await TestBed.configureTestingModule({
      imports: [ForgotPasswordComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    }).compileComponents();

    fixture = TestBed.createComponent(ForgotPasswordComponent);
    fixture.detectChanges();
  });

  it('shows generic success message after submit', () => {
    fixture.componentInstance.form.setValue({ email: 'user@test.fr' });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(auth.requestPasswordReset).toHaveBeenCalledWith({ email: 'user@test.fr' });
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Si un compte correspond à cette adresse',
    );
  });
});
