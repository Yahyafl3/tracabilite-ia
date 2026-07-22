import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { ResetPasswordComponent } from './reset-password.component';
import { AuthService } from '../../../core/services/auth.service';

describe('ResetPasswordComponent', () => {
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let auth: { resetPassword: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    auth = {
      resetPassword: vi.fn(() =>
        of({ message: 'Votre mot de passe a été réinitialisé. Vous pouvez maintenant vous connecter.' }),
      ),
    };

    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent],
      providers: [
        provideRouter([{ path: 'auth/login', children: [] }]),
        { provide: AuthService, useValue: auth },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => 'valid-token' } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    fixture.detectChanges();
  });

  it('submits new password with token from query params', () => {
    fixture.componentInstance.form.setValue({
      newPassword: 'secret1',
      confirmPassword: 'secret1',
    });
    fixture.componentInstance.onSubmit();
    fixture.detectChanges();

    expect(auth.resetPassword).toHaveBeenCalledWith({
      token: 'valid-token',
      newPassword: 'secret1',
      confirmPassword: 'secret1',
    });
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('réinitialisé');
  });
});
