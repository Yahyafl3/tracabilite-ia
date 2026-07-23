import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { AuthService } from '../../../core/services/auth.service';
import { LoginCredentials, UserRole } from '../../../core/models/auth.models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    InputText,
    Password,
    Button,
    Message,
    ProgressSpinner,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  loginForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  /** Blocks aggressive Chrome autofill until the user focuses the field. */
  readonly emailReadonly = signal(true);
  /** UI-only: hide broken video and keep CSS gradient fallback. */
  readonly videoFailed = signal(false);
  returnUrl = '/decisions';

  constructor() {
    this.loginForm = this.fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/decisions';
  }

  onVideoError(): void {
    this.videoFailed.set(true);
  }

  unlockEmailField(): void {
    this.emailReadonly.set(false);
  }

  onSubmit(): void {
    this.errorMessage.set(null);

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    const { email, password } = this.loginForm.getRawValue();
    const credentials: LoginCredentials = {
      email: email.trim(),
      password,
    };

    this.authService.login(credentials).subscribe({
      next: (response) => {
        const role = response.user?.role;
        let defaultUrl = '/decisions';
        if (role === UserRole.AUDITEUR) defaultUrl = '/audit';
        if (role === UserRole.VALIDATEUR) defaultUrl = '/validation';
        const target = this.route.snapshot.queryParams['returnUrl'] || defaultUrl;
        void this.router.navigate([target]);
      },
      error: (error: Error & { status?: number }) => {
        this.isLoading.set(false);
        this.errorMessage.set(
          error?.message && !this.looksTechnical(error.message)
            ? error.message
            : AuthService.resolveLoginErrorMessage(error),
        );
      },
      complete: () => {
        this.isLoading.set(false);
      },
    });
  }

  hasError(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  getErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);

    if (field?.hasError('required')) {
      return 'Ce champ est requis';
    }
    if (field?.hasError('email')) {
      return 'Adresse email invalide';
    }
    if (field?.hasError('minlength')) {
      const minLength = field.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} caractères`;
    }
    return '';
  }

  private looksTechnical(message: string): boolean {
    return /http|stack|exception|at\s+\w+\.|localhost:\d+/i.test(message);
  }
}
